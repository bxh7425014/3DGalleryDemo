package com.demo.render;

import java.util.ArrayList;
import javax.microedition.khronos.opengles.GL10;
import com.ophone.ogles.lib.AppConfig;

public final class DisPlayList extends ArrayList<DisplayItem> {
	//焦点的显示方式
	public static final int DISPLAY_FOCUS_NULL = -1;	//不显示焦点
	public static final int DISPLAY_FOCUS_ON_INTERSECT_ITEM = 0;	//显示射线拾取到的所有item焦点
	public static final int DISPLAY_FOCUS_ON_PICKED_ITEM = 1;	//显示射线拾取的最前方的item焦点
	private int mFocusItemIndex = DisplayItem.FOCUS_NULL;
	private boolean isIntersect;	//item是否与射线相交
	private static DisPlayList mDisPlayList = new DisPlayList();
	private DisPlayList() {
	}
	
	public synchronized static DisPlayList getInstance() {
		return mDisPlayList;
	}
	
	public void draw(GL10 gl) {
		for (int index = 0; index < mDisPlayList.size(); index++) {
			mDisPlayList.get(index).draw(gl);
		}
	}
	
	/**
	 * @return true item与射线相交 false item与射线不相交
	 */
	public boolean getIntersectFlag() {
		return isIntersect;
	}
	
	public boolean update(float mFrameInterval) {
		boolean isDirty = false;
		for (int index = 0; index < mDisPlayList.size(); index++) {
			DisplayItem displayItem = mDisPlayList.get(index);
			isDirty |= !displayItem.update(mFrameInterval);
		}
		return isDirty;
	}
	
	public void processPick(GL10 gl, float gScreenX, float gScreenY, int focusOnItemType, int focusOnGridType) {
		if (AppConfig.gbNeedPick) {
			AppConfig.gbNeedPick = false;
		} else {
			return;
		}
		updatePick(gScreenX, gScreenY);
		drawPickedItem(gl, focusOnItemType, focusOnGridType);
		GridDrawable.freshDesLocation();
	}
	
	/**
	 * 更新射线的拾取数据
	 * @param gScreenY 
	 * @param gScreenX 
	 */
	private void updatePick(float gScreenX, float gScreenY) {
		if (mDisPlayList != null) {
			isIntersect = false;
			for (int index = 0; index < mDisPlayList.size(); index++) {
				isIntersect |= mDisPlayList.get(index).updatePick(gScreenX, gScreenY);
			}
			float closeDis = Float.MAX_VALUE;
			// 计算射线拾取的最新的displayItem索引
			for (int index = 0; index < mDisPlayList.size(); index++) {
				DisplayItem displayItem = mDisPlayList.get(index);
				if (displayItem.getIntersectFlag()) {
					if (closeDis > displayItem.getCloseDis()) {
						closeDis = displayItem.getCloseDis();
						mFocusItemIndex = index;
					}
				}
			}
		}
	}
	
	private void drawPickedItem(GL10 gl, int focusOnItemType, int focusOnGridType) {
		if (!getIntersectFlag()) {
			return;
		}
		if (focusOnItemType == DISPLAY_FOCUS_NULL) {
			return;
		}
		if (mDisPlayList != null) {
			if (focusOnItemType == DISPLAY_FOCUS_ON_INTERSECT_ITEM) {
				for (int index = 0; index < mDisPlayList.size(); index++) {
					DisplayItem displayItem = mDisPlayList.get(index);
					if (displayItem.getIntersectFlag()) {
						displayItem.drawPickedGrid(gl, focusOnGridType);
					}
				}
			} else if (focusOnItemType == DISPLAY_FOCUS_ON_PICKED_ITEM) {
				if ((mFocusItemIndex != DisplayItem.FOCUS_NULL) && (mFocusItemIndex <= mDisPlayList.size() - 1)) {
					DisplayItem displayItem = mDisPlayList.get(mFocusItemIndex);
					displayItem.drawPickedGrid(gl, focusOnGridType);
				}
			}
		}
	}
	
	public int getFocusItem() {
		return mFocusItemIndex;
	}
}
