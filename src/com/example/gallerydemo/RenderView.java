package com.example.gallerydemo;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.ReferenceQueue;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL10Ext;
import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.opengles.GL11Ext;
import javax.microedition.khronos.opengles.GL11ExtensionPack;

import com.demo.Interface.ILoadTexImage;
import com.demo.render.DisPlayList;
import com.demo.render.DisplayItem;
import com.demo.render.GridDrawable;
import com.example.utils.Deque;
import com.example.utils.DirectLinkedList;
import com.example.utils.LogExt;
import com.example.utils.Texture;
import com.example.utils.TextureReference;
import com.ophone.ogles.lib.AppConfig;
import com.ophone.ogles.lib.IBufferFactory;
import com.ophone.ogles.lib.Matrix4f;
import com.ophone.ogles.lib.PickFactory;
import com.ophone.ogles.lib.Ray;
import com.ophone.ogles.lib.Vector3f;
import com.ophone.ogles.lib.Vector4f;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.opengl.ETC1;
import android.opengl.ETC1Util;
import android.opengl.ETC1Util.ETC1Texture;
import android.opengl.GLES10;
import android.opengl.GLES10Ext;
import android.opengl.GLES11;
import android.opengl.GLES11Ext;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.GLUtils;
import android.opengl.GLSurfaceView.Renderer;
import android.os.Process;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;

public class RenderView extends GLSurfaceView implements Renderer {
	private GL11 mGL;
	// Frame time in milliseconds and delta since last frame in seconds. Uses
	// SystemClock.getUptimeMillis().
	private long mFrameTime = 0;
	private float mFrameInterval = 0.0f;
	// 观察者、中心和上方
	private Vector3f mvEye = new Vector3f(0, 0, 4.5f), mvCenter = new Vector3f(0,
			0, 0), mvUp = new Vector3f(0, 1, 0);
	private Matrix4f matInvertModel = new Matrix4f();
	private Ray transformedRay = new Ray();
	// 存放拾取的三角形定点位置
	private Vector3f[] mpTriangle = { new Vector3f(), new Vector3f(),
			new Vector3f() };
	private int mLoadingCount = 0;
	private static final int MAX_LOADING_COUNT = 8;
	private static final Deque<Texture> sLoadInputQueue = new Deque<Texture>();
	private static final Deque<Texture> sLoadOutputQueue = new Deque<Texture>();
	private final ReferenceQueue mUnreferencedTextureQueue = new ReferenceQueue();
	// 存放活动的纹理id
	private final DirectLinkedList<TextureReference> mActiveTextureList = new DirectLinkedList<TextureReference>();
	final static int[] textureId = new int[1];
	private Context mContext;
	private static TextureLoadThread sTextureLoadThread = null;
	public RenderView(Context context) {
		super(context);
		mContext = context;
		setBackgroundDrawable(null);
		setFocusable(true);
        // We want an 8888 pixel format because that's required for
        // a translucent window.
        // And we want a depth buffer.
		setEGLConfigChooser(8, 8, 8, 8, 16, 0);
//		setEGLConfigChooser(true);
		getHolder().setFormat(PixelFormat.TRANSLUCENT);
		requestFocus();
		setFocusableInTouchMode(true);
		setRenderer(this);
		TextureLoadThread thread = new TextureLoadThread();
		sTextureLoadThread = thread;
		thread.start();
		
		GridDrawable.initDisplayItem(this);
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		// Upload new textures.
		processTextures(false);
		// Update the current time and frame time interval.
		long now = SystemClock.uptimeMillis();
		final float dt = 0.001f * Math.min(50, now - mFrameTime);
		mFrameInterval = dt;
		mFrameTime = now;

		// 场景初始化
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT); // 清除屏幕和深度缓存
		gl.glLoadIdentity(); // 重置当前的模型观察矩阵
		// 紧接着设置模型视图矩阵
		setUpCamera(gl);
		
		// 渲染坐标系
		drawCoordinateSystem(gl);
		
		DisPlayList disPlayList = DisPlayList.getInstance();
		// 更新所有海报的位置数据
		boolean isDirty = disPlayList.update(mFrameInterval);
		if (isDirty) {
			requestRender();
		}
		LogExt.LogD(this, Thread.currentThread().getStackTrace(), "isDirty = " + isDirty);
		// 绘制海报
		disPlayList.draw(gl);
		// 绘制选中的焦点海报
		disPlayList.processPick(gl, AppConfig.gScreenX, AppConfig.gScreenY, 
				DisPlayList.DISPLAY_FOCUS_ON_PICKED_ITEM, DisplayItem.DISPLAY_FOCUS_ON_ITEM);
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		LogExt.LogD(this, Thread.currentThread().getStackTrace());
		// 设置视口
		gl.glViewport(0, 0, width, height);
		AppConfig.gpViewport[0] = 0;
		AppConfig.gpViewport[1] = 0;
		AppConfig.gpViewport[2] = width;
		AppConfig.gpViewport[3] = height;

		// 设置投影矩阵
		float ratio = (float) width / height;// 屏幕宽高比
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		// GLU.gluPerspective(gl, 45.0f, ratio, 1, 5000);系统提供
		Matrix4f.gluPersective(45.0f, ratio, 1, 25, AppConfig.gMatProject);
		gl.glLoadMatrixf(AppConfig.gMatProject.asFloatBuffer());
		AppConfig.gMatProject.fillFloatArray(AppConfig.gpMatrixProjectArray);
		// 每次修改完GL_PROJECTION后，最好将当前矩阵模型设置回GL_MODELVIEW
		gl.glMatrixMode(GL10.GL_MODELVIEW);
	}

	@Override
	public void onSurfaceCreated(GL10 gl1, EGLConfig config) {
		LogExt.LogD(this, Thread.currentThread().getStackTrace());
		GL11 gl = (GL11) gl1;
		if (mGL == null) {
			mGL = gl;
		} else {
			// The GL Object has changed.
			LogExt.LogD(this, Thread.currentThread().getStackTrace(),
					"GLObject has changed from " + mGL + " to " + gl);
			mGL = gl;
		}
		setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
		// 全局性设置
		gl.glEnable(GL10.GL_DITHER);

		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
		// 设置清屏背景颜色
		gl.glClearColor(0, 0, 0, 0);
//		// 设置灰色半透明
//		gl.glClearColor(0.5f, 0.5f, 0.5f, 0.5f);
		// 设置着色模型为平滑着色
		gl.glShadeModel(GL10.GL_SMOOTH);

		// 启用背面剪裁
		gl.glEnable(GL10.GL_CULL_FACE);
		gl.glCullFace(GL10.GL_BACK);
		// 启用深度测试
		gl.glEnable(GL10.GL_DEPTH_TEST);
		// 禁用光照
		gl.glDisable(GL10.GL_LIGHTING);
		// 开启混合
		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

		// 启用纹理映射
		gl.glClearDepthf(1.0f);
		// 允许2D贴图,纹理
		gl.glEnable(GL10.GL_TEXTURE_2D);
	}
	
	/**
	 * 设置相机矩阵
	 * 
	 * @param gl
	 */
	private void setUpCamera(GL10 gl) {
		// 设置模型视图矩阵
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
		// GLU.gluLookAt(gl, mfEyeX, mfEyeY, mfEyeZ, mfCenterX, mfCenterY,
		// mfCenterZ, 0, 1, 0);//系统提供
		Matrix4f.gluLookAt(mvEye, mvCenter, mvUp, AppConfig.gMatView);
		gl.glLoadMatrixf(AppConfig.gMatView.asFloatBuffer());
	}
	
	/**
	 * 渲染坐标系
	 */
	private void drawCoordinateSystem(GL10 gl) {
		// 暂时禁用深度测试
		gl.glDisable(GL10.GL_DEPTH_TEST);
		// 设置点和线的宽度
		gl.glLineWidth(2.0f);
		// 仅仅启用顶点数据
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

		FloatBuffer fb = IBufferFactory.newFloatBuffer(3 * 2);
		fb.put(new float[] { 0, 0, 0, 1.4f, 0, 0 });
		fb.position(0);

		// 渲染X轴
		gl.glColor4f(1.0f, 0.0f, 0.0f, 1.0f);// 设置红色
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, fb);
		// 提交渲染
		gl.glDrawArrays(GL10.GL_LINES, 0, 2);

		fb.clear();
		fb.put(new float[] { 0, 0, 0, 0, 1.4f, 0 });
		fb.position(0);
		// 渲染Y轴
		gl.glColor4f(0.0f, 1.0f, 0.0f, 1.0f);// 设置绿色
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, fb);
		// 提交渲染
		gl.glDrawArrays(GL10.GL_LINES, 0, 2);

		fb.clear();
		fb.put(new float[] { 0, 0, 0, 0, 0, 1.4f });
		fb.position(0);
		// 渲染Z轴
		gl.glColor4f(0.0f, 0.0f, 1.0f, 1.0f);// 设置蓝色
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, fb);
		// 提交渲染
		gl.glDrawArrays(GL10.GL_LINES, 0, 2);

		// 重置
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glLineWidth(1.0f);
		gl.glEnable(GL10.GL_DEPTH_TEST);
	}
	
	public void prime(Texture texture, boolean highPriority) {
		if (texture != null && texture.mState == Texture.STATE_UNLOADED
				&& (highPriority || mLoadingCount < MAX_LOADING_COUNT)) {
			queueLoad(texture, highPriority);
		}
	}
	
	private void queueLoad(final Texture texture, boolean highPriority) {
		// Allow the texture to defer queuing.
		if (!texture.shouldQueue()) {
			return;
		}

		// Change the texture state to loading.
		texture.mState = Texture.STATE_LOADING;
		// Push the texture onto the load input queue.
		Deque<Texture> inputQueue = sLoadInputQueue;
		synchronized (inputQueue) {
			if (highPriority) {
				inputQueue.addFirst(texture);
			} else {
				inputQueue.addLast(texture);
			}
			inputQueue.notify();
		}
		++mLoadingCount;
	}
	
	/** Uploads at most one texture to GL. */
	private void processTextures(boolean processAll) {
		// Destroy any textures that are no longer referenced.
//		GL11 gl = mGL;
		TextureReference textureReference;
		while ((textureReference = (TextureReference) mUnreferencedTextureQueue
				.poll()) != null) {
			removeTexture(textureReference);
//			textureId[0] = textureReference.textureId;
//			GL11 glOld = textureReference.gl;
//			if (glOld == gl) {
//				gl.glDeleteTextures(1, textureId, 0);
//				TCLLog.Log(this, Thread.currentThread().getStackTrace(),
//						"textureId[0] = " + textureId[0]);
//			}
//			mActiveTextureList.remove(textureReference.activeListEntry);
		}
		Deque<Texture> outputQueue = sLoadOutputQueue;
		Texture texture;
		do {
			// Upload loaded textures to the GPU one frame at a time.
			synchronized (outputQueue) {
				texture = outputQueue.pollFirst();
			}
			if (texture != null) {
				// Extract the bitmap from the texture.
				uploadTexture(texture, textureId);

				// Decrement the loading count.
				--mLoadingCount;
			} else {
				break;
			}
		} while (processAll);
	}
	
	private void removeTexture(TextureReference textureReference) {
		GL11 gl = mGL;
		textureId[0] = textureReference.textureId;
		GL11 glOld = textureReference.gl;
		if (glOld == gl) {
			gl.glDeleteTextures(1, textureId, 0);
			LogExt.LogD(this, Thread.currentThread().getStackTrace(),
					"textureId[0] = " + textureId[0]);
		}
		mActiveTextureList.remove(textureReference.activeListEntry);
	}
	
	private void uploadTexture(Texture texture, int[] textureId) {
		texture.mILoadTexImage.uploadTexture(mGL, texture, textureId, mActiveTextureList, mUnreferencedTextureQueue, this);
	}

	/**
	 * 注：暂时不处理以后添加处理方法
	 */
	private void handleLowMemory() {
		// TODO Auto-generated method stub
	}
	
	private final class TextureLoadThread extends Thread {
		public boolean mIsLoading;

		public TextureLoadThread() {
			super("TextureLoad");
		}

		public void run() {
			Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
			Deque<Texture> inputQueue = sLoadInputQueue;
			Deque<Texture> outputQueue = sLoadOutputQueue;
			try {
				for (;;) {
					// Pop the next texture from the input queue.
					Texture texture = null;
					synchronized (inputQueue) {
						while ((texture = inputQueue.pollFirst()) == null) {
							inputQueue.wait();
						}
					}
					// Load the texture bitmap.
					load(texture);
					mIsLoading = false;
					// TCLLog.Log(this, Thread.currentThread().getStackTrace(),
					// "texture = " + texture);
					// Push the texture onto the output queue.
					synchronized (outputQueue) {
						outputQueue.addLast(texture);
					}
				}
			} catch (InterruptedException e) {
				// Terminate the thread.
			}
		}

		private void load(Texture texture) {
			// Generate the texture bitmap.
			RenderView view = RenderView.this;
			view.loadTextureAsync(texture);
			view.requestRender();
		}
	}
	
	private void loadTextureAsync(Texture texture) {
		ILoadTexImage texImage = texture.load();
		texture.mILoadTexImage = texImage;
		texture.mILoadTexImage.loadTextureAsync(texture);
	}
	
	/**
	 * 响应触屏事件
	 */
	@Override
	public boolean onTouchEvent(MotionEvent e) {
		float x = e.getX();
		float y = e.getY();
		AppConfig.setTouchPosition(x, y);
		switch (e.getAction()) {
		case MotionEvent.ACTION_MOVE:
			break;
		case MotionEvent.ACTION_DOWN:
			AppConfig.gbNeedPick = false;
			break;
		case MotionEvent.ACTION_UP:
			AppConfig.gbNeedPick = true;
			break;
		case MotionEvent.ACTION_CANCEL:
			AppConfig.gbNeedPick = false;
			break;
		}
		requestRender();
		LogExt.LogD(this, Thread.currentThread().getStackTrace());
		return true;
	}
}
