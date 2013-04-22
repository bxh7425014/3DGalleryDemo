package com.demo.render;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.concurrent.ThreadPoolExecutor.DiscardOldestPolicy;
import java.util.jar.Attributes.Name;

import javax.microedition.khronos.opengles.GL10;

import android.Manifest.permission;
import android.R.bool;
import android.R.integer;
import android.R.string;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.EventLogTags.Description;

import com.example.utils.Constants;
import com.example.utils.FloatUtils;
import com.example.utils.LogExt;
import com.example.utils.Texture;
import com.ophone.ogles.lib.AppConfig;
import com.ophone.ogles.lib.Matrix4f;
import com.ophone.ogles.lib.PickFactory;
import com.ophone.ogles.lib.Ray;
import com.ophone.ogles.lib.Rotate4f;
import com.ophone.ogles.lib.Vector3f;
import com.ophone.ogles.lib.Vector4f;

public class GridQuad{
	public static final int VERTEX_BUFFER = 0;
	public static final int TEXTURE_BUFFER = 1;
	public static final float one = 1.0f; // 设置OpenGL ES的one单位大小
//	private float QUAD_WIDTH = 1.5f;
//	private float QUAD_HEIGHT = 2.0f;
	private float QUAD_WIDTH;
	private float QUAD_HEIGHT;
	private float[] vertices;
	private Texture[] mTextures;
	private boolean isIntersect;	//grid是否与射线相交
//	// 使用贴图的纹理坐标
//	private float[] texCoords = new float[] {
//			0, one, // 左上
//			one, one, // 右上
//			one, 0, // 右下
//			0, 0 // 左下
//	};
//	// 使用cdt纹理的纹理坐标
//	private float[] texCoords = new float[] {
//			0, 0, // 左上
//			one, 0, // 右上
//			one, one, // 右下
//			0, one // 左下
//	};
	private float[] texCoords;
	private short[] indices = Constants.INDICES;//new short[] { 0, 1, 2, 3 };
//	private short[] indices = new short[] { 0, 1, 2, 3, 4, 5, 6, 7};

	private GridLocation mSrcLocation = new GridLocation();
	private GridLocation mDesLocation = new GridLocation();
	private Matrix4f matRot = new Matrix4f();
	private Matrix4f matTrans = new Matrix4f();
	private Vector3f mOffsetPosition = new Vector3f(0,0,0);
	private float mOffsetAngle = 0;
	
	public GridQuad(float width, float height, float[] texcoords, Vector3f offsetPosition, float offsetAngle, Texture[] textures) {
		QUAD_WIDTH = width;
		QUAD_HEIGHT = height;
		vertices = new float[] { 
				-QUAD_WIDTH / 2, -QUAD_HEIGHT / 2, 0, // 左下顶点
				QUAD_WIDTH / 2, -QUAD_HEIGHT / 2, 0, // 右下顶点
				QUAD_WIDTH / 2, QUAD_HEIGHT / 2, 0, // 右上顶点
				-QUAD_WIDTH / 2, QUAD_HEIGHT / 2, 0, // 左上顶点
				// test code
				-QUAD_WIDTH / 4, -QUAD_HEIGHT / 4, 0.1f, // 左下顶点
				QUAD_WIDTH / 4, -QUAD_HEIGHT / 4, 0.1f, // 右下顶点
				QUAD_WIDTH / 4, QUAD_HEIGHT / 4, 0.1f, // 右上顶点
				-QUAD_WIDTH / 4, QUAD_HEIGHT / 4, 0.1f, // 左上顶点
		};
		texCoords = texcoords;
		mOffsetPosition.set(offsetPosition);
		mOffsetAngle = offsetAngle;
		mTextures = textures;
	}
	
	/**
	 * 模型矩阵
	 */
	private Matrix4f gMatModel = new Matrix4f();
	
	public GridLocation getSrcLocation() {
		return mSrcLocation;
	}
	
	public void setSrcLocation(GridLocation location) {
		// 目标位置加上偏移位置
		location.getPosition().add(mOffsetPosition);
		// 目标角度加上偏移角度(转角的参考轴不变)
		location.getRotate().addOffsetAngle(mOffsetAngle);
		// 设置计算后的位置
		mSrcLocation = location;
	}
	
	public GridLocation getDesLocation() {
		return mDesLocation;
	}
	
	public void setDesLocation(GridLocation location) {
		// 目标位置加上偏移位置
		location.getPosition().add(mOffsetPosition);
		// 目标角度加上偏移角度(转角的参考轴不变)
		location.getRotate().addOffsetAngle(mOffsetAngle);
		// 设置计算后的位置
		mDesLocation = location;
	}
	
	public Matrix4f getMatModel() {
		return gMatModel;
	}
	// 获得坐标的缓存对象
	public FloatBuffer getCoordinate(int coord_id) {
		switch (coord_id) {
		case VERTEX_BUFFER:
			return getDirectBuffer(vertices);
		case TEXTURE_BUFFER:
			return getDirectBuffer(texCoords);
		default:
			throw new IllegalArgumentException();
		}
	}

	// 获得三角形描述顺序
	public ShortBuffer getIndices() {
		return ShortBuffer.wrap(indices);
	}

	public static float[] getFloatArray(FloatBuffer fb) {
		float[] fa = new float[fb.capacity()];
		fb.position(0);
		for (int index = 0; index < fa.length; index++) {
			fa[index] = fb.get(index);
		}
		fb.position(0);
		return fa;
	}

	public static FloatBuffer getDirectBuffer(float[] buffer) {
		final int FLOAT_SIZE = 4;
		FloatBuffer directBuffer = ByteBuffer.allocateDirect(buffer.length * FLOAT_SIZE).order(ByteOrder.nativeOrder()).asFloatBuffer();
		directBuffer.put(buffer);
		directBuffer.position(0);
		return directBuffer;
	}

	private Vector4f mIntersectLocation = new Vector4f();

	// 获取射线拾取点的位置
	public Vector4f getIntersectLocation() {
		return mIntersectLocation;
	}
	
	/**
	 * 射线与模型的精确碰撞检测
	 * 
	 * @param ray
	 *            - 转换到模型空间中的射线
	 * @param trianglePosOut
	 *            - 返回的拾取后的三角形顶点位置
	 * @return 如果相交，返回true
	 */
	public boolean intersect(Ray ray, Vector3f[] trianglePosOut) {
		boolean bFound = false;
		// 存储着射线原点与三角形相交点的距离
		// 我们最后仅仅保留距离最近的那一个
		float closeDis = 0.0f;

		Vector3f v0, v1, v2;
		isIntersect = false;
		// 每个面两个三角形
		for (int index = 0; index < 2; index ++) {
			// 分割四边形为两个三角形，取顶点数据
			if (index == 0) {
				v0 = getVector3f(0);
				v1 = getVector3f(1);
				v2 = getVector3f(2);
			} else {
				v0 = getVector3f(0);
				v1 = getVector3f(2);
				v2 = getVector3f(3);
			}
			// 进行射线和三角行的碰撞检测
			if (ray.intersectTriangle(v0, v1, v2, mIntersectLocation)) {
				// 如果发生了相交
				if (!bFound) {
					// 如果是初次检测到，需要存储射线原点与三角形交点的距离值
					bFound = true;
					closeDis = mIntersectLocation.w;
					if (trianglePosOut != null) {
						trianglePosOut[0].set(v0);
						trianglePosOut[1].set(v1);
						trianglePosOut[2].set(v2);
					}
				} else {
					// 如果之前已经检测到相交事件，则需要把新相交点与之前的相交数据相比较
					// 最终保留离射线原点更近的
					if (closeDis > mIntersectLocation.w) {
						closeDis = mIntersectLocation.w;
						if (trianglePosOut != null) {
							trianglePosOut[0].set(v0);
							trianglePosOut[1].set(v1);
							trianglePosOut[2].set(v2);
						}
					}
				}
				LogExt.LogD(this, Thread.currentThread().getStackTrace(),
						"bFound = " + bFound + ", closeDis = " + closeDis + ", " + this);
			}
		}
		isIntersect = bFound;
		return bFound;
	}
	
	public void drawFocusGrid(GL10 gl) {
		gl.glPushMatrix();
		// 由于返回的拾取三角形数据是出于模型坐标系中
		// 因此需要经过模型变换，将它们变换到世界坐标系中进行渲染
		// 设置模型变换矩阵
		gl.glMultMatrixf(getMatModel().asFloatBuffer());
		// 设置三角形颜色，alpha为0.7
		gl.glColor4f(1.0f, 1.0f, 0.0f, 0.5f);
		// 开启Blend混合模式
//		gl.glEnable(GL10.GL_BLEND);
//		gl.glBlendFunc(GL10.GL_SRC_ALPHA,
//				GL10.GL_ONE_MINUS_SRC_ALPHA);
		// 禁用无关属性，仅仅使用纯色填充
		gl.glDisable(GL10.GL_DEPTH_TEST);
		gl.glDisable(GL10.GL_TEXTURE_2D);
		// 开始绑定渲染顶点数据
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0,
				getCoordinate(GridQuad.VERTEX_BUFFER));
		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0,
				getCoordinate(GridQuad.TEXTURE_BUFFER)); // 设置矩形纹理顶点
		gl.glDrawElements(GL10.GL_TRIANGLE_FAN, 4,
				GL10.GL_UNSIGNED_SHORT, getIndices()); // 绘制
		// 重置相关属性
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnable(GL10.GL_DEPTH_TEST);
//		gl.glDisable(GL10.GL_BLEND);
		gl.glPopMatrix();
	}
	
	/**
	 * @return true GridQuad与射线相交 false GridQuad与射线不相交
	 */
	public boolean getIntersectFlag() {
		return isIntersect;
	}
	
	private Vector3f getVector3f(int start) {
		return new Vector3f(vertices[3 * start], vertices[3 * start + 1],
				vertices[3 * start + 2]);
	}
	
	/**
	 * @author bianxh
	 * 存放Grid的位置信息
	 */
	public static class GridLocation {
		private Vector3f mPosition = new Vector3f(0, 0, 0);
//		private Rotate4f mRotate = new Rotate4f((float) Math.PI / 2, 0, 1, 0); // (x,y,z,angle)
		private Rotate4f mRotate = new Rotate4f(0, 0, 1, 0); // (angle,x,y,z)
		public void SetPosition(Vector3f vector3f) {
			mPosition.set(vector3f);
		}
		
		public Vector3f getPosition() {
			return mPosition;
		}
		
		public void SetRotate(Rotate4f vector4f) {
			mRotate.set(vector4f);
		}
		
		public Rotate4f getRotate() {
			return mRotate;
		}
		
		private boolean equals(GridLocation desLocation) {
			final float THRESOLD = (float)10e-8;
			Vector3f vector3f = desLocation.getPosition();
			Rotate4f vector4f = desLocation.getRotate();
			if (((mPosition.x - vector3f.x) <= THRESOLD)
				&& ((mPosition.y - vector3f.y) <= THRESOLD) 
				&& ((mPosition.z - vector3f.z) <= THRESOLD) 
				&& ((mRotate.angle - vector4f.angle) <= THRESOLD)
				&& ((mRotate.rx - vector4f.rx) <= THRESOLD) 
				&& ((mRotate.ry - vector4f.ry) <= THRESOLD)
				&& ((mRotate.rz - vector4f.rz) <= THRESOLD)) {
				return true;
			} else {
				return false;
			}
		}
	}

	/**
	 * @param glRender
	 * @param mFrameInterval
	 * @return true 已经移动到目标位置 false 还未移动到目标位置  
	 */
	public boolean update(float mFrameInterval) {
		if (!haveTexture()) {
			return true;
		}
		GridLocation srcLocation = getSrcLocation();
		GridLocation desLocation = getDesLocation();
		FloatUtils.animate(srcLocation.getPosition(), desLocation.getPosition(), mFrameInterval);
		FloatUtils.animate(srcLocation.getRotate(), desLocation.getRotate(), mFrameInterval);
		boolean isDirty = false;
		if (!srcLocation.equals(desLocation)) {
			isDirty = true;
		}
		return isDirty;
	}
	
	/**
	 * 判断当前GridQuad是否含有texture纹理
	 * @return
	 */
	private boolean haveTexture() {
		if ((mTextures == null) || (mTextures.length == 0)) {
			return false;
		}
		boolean drawFlag = false;
		for (int index = 0; index < mTextures.length; index++) {
			if (mTextures[index].getState() == Texture.STATE_LOADED) {
				drawFlag = true;
			} 
		}
		return drawFlag;
	}

	public void draw(GL10 gl) {
		if (!haveTexture()) {
			return;
		}
		for (int index = 0; index < mTextures.length; index++) {
			Texture texture = mTextures[index];
			if (texture == null) {
				continue;
			}
			if (texture.getState() == Texture.STATE_LOADED) {
				int textureIndex = GL10.GL_TEXTURE0 + index;
				if (textureIndex > GL10.GL_TEXTURE31) {
					LogExt.LogD(this, Thread.currentThread().getStackTrace(), "Error, textureIndex = " + textureIndex);
					return;
				}
				gl.glActiveTexture(textureIndex);
				gl.glClientActiveTexture(textureIndex);
				gl.glEnable(GL10.GL_TEXTURE_2D);
				//绑定纹理
				gl.glBindTexture(GL10.GL_TEXTURE_2D, texture.mId);
				//开启纹理
				gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
				//设置矩形纹理顶点
				gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, getCoordinate(GridQuad.TEXTURE_BUFFER));
			} 
		}
		matRot.setIdentity();
		matTrans.setIdentity();
		getMatModel().setIdentity();
		gl.glPushMatrix();
		Vector3f trans3f = getSrcLocation().getPosition();
		matTrans.setTranslation(trans3f.x, trans3f.y, trans3f.z);
		getMatModel().mul(matTrans);
		Rotate4f rotate4f= getSrcLocation().getRotate();
		matRot.glRotatef(rotate4f.angle, rotate4f.rx, rotate4f.ry, rotate4f.rz);
		getMatModel().mul(matRot);
		gl.glMultMatrixf(getMatModel().asFloatBuffer());
		// 设置默认颜色
		gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		// Set vertex data
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);	//允许设置顶点		
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, getCoordinate(GridQuad.VERTEX_BUFFER));
		gl.glDrawElements(GL10.GL_TRIANGLE_FAN, 4,  GL10.GL_UNSIGNED_SHORT, getIndices());	//绘制
		gl.glDisable(GL10.GL_TEXTURE_2D);
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);	// 取消顶点设置
		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);	// 关闭纹理
		gl.glPopMatrix();
//		if (getState() == Texture.STATE_LOADED) {
//			matRot.setIdentity();
//			matTrans.setIdentity();
//			getMatModel().setIdentity();
//			gl.glPushMatrix();
//			Vector3f trans3f = getSrcLocation().getLocation();
//			matTrans.setTranslation(trans3f.x, trans3f.y, trans3f.z);
//			getMatModel().mul(matTrans);
//			Vector4f rotate4f= getSrcLocation().getRotate();
//			matRot.glRotatef(rotate4f.x, rotate4f.y, rotate4f.z, rotate4f.w);
//			getMatModel().mul(matRot);
//			gl.glMultMatrixf(getMatModel().asFloatBuffer());
//			// 设置默认颜色
//			gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
//			for (int textureIndex = GL10.GL_TEXTURE0; textureIndex < GL10.GL_TEXTURE1; textureIndex++) {
//				gl.glActiveTexture(textureIndex);
//				gl.glClientActiveTexture(textureIndex);
//				gl.glEnable(GL10.GL_TEXTURE_2D);
//				//绑定纹理
//				gl.glBindTexture(GL10.GL_TEXTURE_2D, texture);
//				//开启纹理
//				gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
//				//设置矩形纹理顶点
//				gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, getCoordinate(GridQuad.TEXTURE_BUFFER));
//			}
//			// Set vertex data
//			gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);	//允许设置顶点		
//			gl.glVertexPointer(3, GL10.GL_FLOAT, 0, getCoordinate(GridQuad.VERTEX_BUFFER));
//			gl.glDrawElements(GL10.GL_TRIANGLE_FAN, 4,  GL10.GL_UNSIGNED_SHORT, getIndices());	//绘制
//			gl.glDisable(GL10.GL_TEXTURE_2D);
//			gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);	// 取消顶点设置
//			gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);	// 关闭纹理
//		}
//		gl.glPopMatrix();
	}
	

}
