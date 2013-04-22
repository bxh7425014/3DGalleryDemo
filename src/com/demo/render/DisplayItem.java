package com.demo.render;

import java.util.ArrayList;
import javax.microedition.khronos.opengles.GL10;
import com.example.utils.LogExt;
import com.ophone.ogles.lib.Matrix4f;
import com.ophone.ogles.lib.PickFactory;
import com.ophone.ogles.lib.Ray;
import com.ophone.ogles.lib.Vector3f;
import com.ophone.ogles.lib.Vector4f;

public class DisplayItem {
	//焦点的显示方式
	public static final int DISPLAY_FOCUS_NULL = -1;	//不显示焦点
	public static final int DISPLAY_FOCUS_ON_INTERSECT_GRID = 0;	//显示射线拾取到的所有grid焦点
	public static final int DISPLAY_FOCUS_ON_PICKED_GRID = 1;	//显示射线拾取的最前方的grid焦点
	public static final int DISPLAY_FOCUS_ON_ITEM = 2;	//显示整个item的焦点
	
	public static final int FOCUS_NULL = -1;
	private float mCloseDis = Float.MAX_VALUE;
	private int mFocusGridIndex = FOCUS_NULL;
	private Matrix4f matInvertModel = new Matrix4f();
	private Ray transformedRay = new Ray();
	private ArrayList<GridQuad> mGridQuads = new ArrayList<GridQuad>();
	private boolean isIntersect;	//item是否与射线相交
	// 存放拾取的三角形定点位置
	private Vector3f[] mpTriangle = { new Vector3f(), new Vector3f(),
			new Vector3f() };
	
	public ArrayList<GridQuad> getGridQuads() {
		return mGridQuads;
	}
	
	/**
	 * @param mFrameInterval
	 * @return true 已经移动到目标位置 false 还未移动到目标位置  
	 */
	public boolean update(float mFrameInterval) {
		boolean isDirty = false;
		if ((mGridQuads != null)) {
			for (int index = 0; index < mGridQuads.size(); index++) {
				GridQuad gridQuad = mGridQuads.get(index);
				isDirty |= !gridQuad.update(mFrameInterval);
			}
		}
		return isDirty;
	}

	public void draw(GL10 gl) {
		if ((mGridQuads != null)) {
			for (int index = 0; index < mGridQuads.size(); index++) {
				mGridQuads.get(index).draw(gl);
			}
		}
	}
	
	/**
	 * 更新拾取事件,获取拾取焦点
	 * @return true 射线跟item相交 false 射线给item不相交
	 */
	public boolean updatePick(float gScreenX, float gScreenY) {
		// 更新最新的拾取射线
		PickFactory.update(gScreenX, gScreenY);
		// 获得最新的拾取射线
		Ray ray = PickFactory.getPickRay();
		final ArrayList<GridQuad> updateList = mGridQuads;
		boolean intersectFlag = false;
		mCloseDis = Float.MAX_VALUE;
		mFocusGridIndex = FOCUS_NULL;
		for (int i = 0; i < updateList.size(); i++) {
			GridQuad gridQuad = updateList.get(i);
			// 如果射线与绑定球发生相交，那么就需要进行精确的三角面级别的相交检测
			// 由于我们的模型渲染数据，均是在模型局部坐标系中
			// 而拾取射线是在世界坐标系中
			// 因此需要把射线转换到模型坐标系中
			// 这里首先计算模型矩阵的逆矩阵
			matInvertModel.set(gridQuad.getMatModel());
			matInvertModel.invert();
			LogExt.LogD(this, Thread.currentThread().getStackTrace(), "i = " + i
					+ ", matInvertModel = " + matInvertModel.toString());
			// 把射线变换到模型坐标系中，把结果存储到transformedRay中
			ray.transform(matInvertModel, transformedRay);
			// 将射线与模型做精确相交检测
			if (gridQuad.intersect(transformedRay, mpTriangle)) {
				Vector4f intersectLocation = gridQuad.getIntersectLocation();
				if (mCloseDis > intersectLocation.w) {
					mCloseDis = intersectLocation.w;
					mFocusGridIndex = i;
				}
				intersectFlag = true;
				LogExt.LogD(this, Thread.currentThread().getStackTrace(), "i = " + i 
						+ ", intersectLocation.w = " + intersectLocation.w);
			} 
		}
		LogExt.LogD(this, Thread.currentThread().getStackTrace(), "mFocusGridIndex = " + mFocusGridIndex);
		isIntersect = intersectFlag;
		return intersectFlag;
	}
	
	
	/**
	 * @return true item与射线相交 false item与射线不相交
	 */
	public boolean getIntersectFlag() {
		return isIntersect;
	}
	
	/**
	 * 获取射线与item中的grid相交的最近距离
	 * @return
	 */
	public float getCloseDis() {
		return mCloseDis;
	}
	/**
	 * 渲染选中的GridQuad
	 */
	public void drawPickedGrid(GL10 gl, int focusOnGridType) {
		if (!getIntersectFlag()) {
			return;
		}
		if (focusOnGridType == DISPLAY_FOCUS_NULL) {
			return;
		} else if (focusOnGridType == DISPLAY_FOCUS_ON_INTERSECT_GRID) {
			for (int index = 0; index < mGridQuads.size(); index++) {
				GridQuad gQuad = mGridQuads.get(index);
				if (gQuad.getIntersectFlag()) {
					gQuad.drawFocusGrid(gl);
				}
			}
		} else if (focusOnGridType == DISPLAY_FOCUS_ON_PICKED_GRID) {
			GridQuad gQuad = mGridQuads.get(mFocusGridIndex);
			if ((mFocusGridIndex != FOCUS_NULL) && (mFocusGridIndex <= mGridQuads.size() - 1)) {
				gQuad.drawFocusGrid(gl);
			}
		} else if (focusOnGridType == DISPLAY_FOCUS_ON_ITEM) {
			for (int index = 0; index < mGridQuads.size(); index++) {
				GridQuad gQuad = mGridQuads.get(index);
				gQuad.drawFocusGrid(gl);
			}
		}
	}
}
