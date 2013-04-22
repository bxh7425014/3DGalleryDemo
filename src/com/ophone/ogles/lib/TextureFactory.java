package com.ophone.ogles.lib;

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;

public class TextureFactory {
	public static int getTexture(Context context, GL10 gl, int resID) {
		return getTexture(context, gl, resID, GL10.GL_CLAMP_TO_EDGE,
				GL10.GL_CLAMP_TO_EDGE, GL10.GL_REPLACE);
	}

	public static int getTexture(Context context, GL10 gl, int resID, int texEnvMode) {
		return getTexture(context, gl, resID, GL10.GL_CLAMP_TO_EDGE,
				GL10.GL_CLAMP_TO_EDGE, texEnvMode);
	}
	
	/**
	 * 创建一个纹理对象
	 * @param context - 应用程序环境
	 * @param gl - opengl es对象
	 * @param resID - R.java中的资源ID
	 * @param wrap_s_mode - 纹理环绕S模式
	 * @param wrap_t_mode - 纹理环绕T模式
	 * @return 申请好的纹理ID
	 */
	public static int getTexture(Context context, GL10 gl, int resID,
			int wrap_s_mode, int wrap_t_mode, int texEnvMode) {
		//申请一个纹理对象ID
		int[] textures = new int[1];
		gl.glGenTextures(1, textures, 0);
		//绑定这个申请来的ID为当前纹理操作对象
		int textureID = textures[0];
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureID);
		//设置当前纹理对象的过滤模式
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,
				GL10.GL_LINEAR);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER,
				GL10.GL_LINEAR);
		//设置环绕模式
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S,
				wrap_s_mode);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T,
				wrap_t_mode);
		//设置纹理环境模式
		gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE,
				texEnvMode);
		
		//开始载入纹理
		InputStream is = context.getResources().openRawResource(resID);
		Bitmap bitmap;
		try {
			bitmap = BitmapFactory.decodeStream(is);
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				// Ignore.
			}
		}
		
		//绑定到纹理
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
		bitmap.recycle();

		return textureID;
	}
}
