package com.demo.Interface;

import java.lang.ref.ReferenceQueue;

import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.opengles.GL11Ext;

import com.example.gallerydemo.RenderView;
import com.example.utils.DirectLinkedList;
import com.example.utils.LogExt;
import com.example.utils.Shared;
import com.example.utils.Texture;
import com.example.utils.TextureReference;
import com.example.utils.Utils;

import android.graphics.Bitmap;
import android.opengl.GLUtils;

public class LoadTexImage_Bitmap implements ILoadTexImage{
	private Bitmap mBitmap;
	public LoadTexImage_Bitmap(Bitmap bmp) {
		mBitmap = bmp;
	}

	@Override
	public void loadTextureAsync(Texture texture) {
		try {
			Bitmap bitmap = mBitmap;
			if (bitmap != null) {
				bitmap = Utils.resizeBitmap(bitmap, 1024);
				// bitmap = Utils.resizeBitmap(bitmap, 512);
				int width = bitmap.getWidth();
				int height = bitmap.getHeight();
				texture.mWidth = width;
				texture.mHeight = height;
				// Create a padded bitmap if the natural size is not a power of
				// 2.
				if (!Shared.isPowerOf2(width) || !Shared.isPowerOf2(height)) {
					int paddedWidth = Shared.nextPowerOf2(width);
					int paddedHeight = Shared.nextPowerOf2(height);
					Bitmap.Config config = bitmap.getConfig();
					if (config == null)
						config = Bitmap.Config.RGB_565;
					if (width * height >= 512 * 512)
						config = Bitmap.Config.RGB_565;
					// Store normalized width and height for use in texture
					// coordinates.
					texture.mNormalizedWidth = (float) width
							/ (float) paddedWidth;
					texture.mNormalizedHeight = (float) height
							/ (float) paddedHeight;
				} else {
					texture.mNormalizedWidth = 1.0f;
					texture.mNormalizedHeight = 1.0f;
				}
			}
			mBitmap = bitmap;
		} catch (Exception e) {
			mBitmap = null;
		} catch (OutOfMemoryError eMem) {
			LogExt.LogD(this, Thread.currentThread().getStackTrace(),
					"Bitmap power of 2 creation fail, outofmemory");
			handleLowMemory();
		}
	}

	@Override
	public void handleLowMemory() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void uploadTexture(GL11 gl, Texture texture, int[] textureId,
			DirectLinkedList<TextureReference> mActiveTextureList, 
			ReferenceQueue unRefQueue, RenderView renderView) {
		Bitmap bitmap = mBitmap;
		int glError = GL11.GL_NO_ERROR;
		if (bitmap != null) {
			final int width = texture.mWidth;
			final int height = texture.mHeight;

			// Define a vertically flipped crop rectangle for OES_draw_texture.
			int[] cropRect = { 0, height, width, -height };

			// Upload the bitmap to a new texture.
			gl.glGenTextures(1, textureId, 0);
			gl.glBindTexture(GL11.GL_TEXTURE_2D, textureId[0]);
			gl.glTexParameteriv(GL11.GL_TEXTURE_2D,
					GL11Ext.GL_TEXTURE_CROP_RECT_OES, cropRect, 0);
			gl.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S,
					GL11.GL_CLAMP_TO_EDGE);
			gl.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T,
					GL11.GL_CLAMP_TO_EDGE);
//			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_REPEAT);	
//			gl.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
			gl.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER,
					GL11.GL_LINEAR);
			gl.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER,
					GL11.GL_LINEAR);
			GLUtils.texImage2D(GL11.GL_TEXTURE_2D, 0, bitmap, 0);
//			InputStream input = getResources().openRawResource(R.raw.test);
//			LoadCDTTexture(input, gl);
//			LoadETC1Texture(input, gl);
			glError = gl.glGetError();
			bitmap.recycle();
			if (glError == GL11.GL_OUT_OF_MEMORY) {
				handleLowMemory();
			}
			if (glError != GL11.GL_NO_ERROR) {
				// There was an error, we need to retry this texture at some
				// later time
				LogExt.LogD(this, Thread.currentThread().getStackTrace(),
						"Texture creation fail, glError " + glError);
				texture.mId = 0;
				mBitmap = null;
				texture.mState = Texture.STATE_UNLOADED;
			} else {
				// Update texture state.
				mBitmap = null;
				texture.mId = textureId[0];
				texture.mState = Texture.STATE_LOADED;

				// Add to the active list.
				final TextureReference textureRef = new TextureReference(
						texture, gl, unRefQueue, textureId[0]);
				mActiveTextureList.add(textureRef.activeListEntry);
				texture.bindTextureReference(textureRef);				
				renderView.requestRender();
			}
		} else {
			texture.mState = Texture.STATE_ERROR;
		}
	}

	@Override
	public void recycle() {
		if (mBitmap != null) {
			mBitmap.recycle();
			mBitmap = null;
		}
	}
}
