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
	 * ����һ���������
	 * @param context - Ӧ�ó��򻷾�
	 * @param gl - opengl es����
	 * @param resID - R.java�е���ԴID
	 * @param wrap_s_mode - ������Sģʽ
	 * @param wrap_t_mode - ������Tģʽ
	 * @return ����õ�����ID
	 */
	public static int getTexture(Context context, GL10 gl, int resID,
			int wrap_s_mode, int wrap_t_mode, int texEnvMode) {
		//����һ���������ID
		int[] textures = new int[1];
		gl.glGenTextures(1, textures, 0);
		//�������������IDΪ��ǰ�����������
		int textureID = textures[0];
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureID);
		//���õ�ǰ�������Ĺ���ģʽ
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,
				GL10.GL_LINEAR);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER,
				GL10.GL_LINEAR);
		//���û���ģʽ
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S,
				wrap_s_mode);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T,
				wrap_t_mode);
		//����������ģʽ
		gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE,
				texEnvMode);
		
		//��ʼ��������
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
		
		//�󶨵�����
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
		bitmap.recycle();

		return textureID;
	}
}
