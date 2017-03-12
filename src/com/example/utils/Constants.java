package com.example.utils;

public class Constants {
	public static String ACTIVITY_TITLE = "activity_title";
	public static String LOCALE_PIC_ADDR = "/sdcard/GalleryDemoTest/pic";
	
	public static final float one = 1.0f; // OpenGL ES的one单位大小
	// 使用贴图的纹理坐标
	public static final float[] TEXCOORDS_BMP = new float[] {
			0, one, // 左上
			one, one, // 右上
			one, 0, // 右下
			0, 0 // 左下
	};
	public static final short[] INDICES = new short[] { 0, 1, 2, 3 };
}
