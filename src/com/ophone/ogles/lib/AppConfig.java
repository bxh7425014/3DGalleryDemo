package com.ophone.ogles.lib;

public class AppConfig {
	/**
	 * 投影矩阵
	 */
	public static Matrix4f gMatProject = new Matrix4f();
	/**
	 * 视图矩阵
	 */
	public static Matrix4f gMatView = new Matrix4f();
//	/**
//	 * 模型矩阵
//	 */
////	public static Matrix4f gMatModel = new Matrix4f();
//	public static Matrix4f[] gMatModel = new Matrix4f[GLRender.GRIDQUAD_NUM];
	/**
	 * 视口参数
	 */
	public static int[] gpViewport = new int[4];
	/**
	 * 当前系统的投影矩阵，列序填充
	 */
	public static float[] gpMatrixProjectArray = new float[16];
	/**
	 * 当前系统的视图矩阵，列序填充
	 */
	public static float[] gpMatrixViewArray = new float[16];
	/**
	 * 是否需要进行拾取检测（当触摸事件发生时）
	 */
	public static boolean gbNeedPick = false;
	/**
	 * 是否有三角形被选中
	 */
	public static boolean gbTrianglePicked = false;
	/**
	 * 是否渲染骨骼Helper
	 */
	public static boolean gbShowJoints = false;
	/**
	 * 是否自动播放动画
	 */
	public static boolean gbEnableAnimation = false;
	
	
	
	public static final int IDX_MODEL_BOX = 0;
	public static final int IDX_MODEL_SPHERE = 1;
	public static final int IDX_MODEL_CYLINDER = 2;

	public static final int MODEL_AMOUNT = 3;
	
	public static int gIdxModelSelected = IDX_MODEL_BOX;
	
	public static float gScreenX, gScreenY;
	
	public static void setTouchPosition(float x, float y) {
		gScreenX = x;
		gScreenY = y;
	}
}