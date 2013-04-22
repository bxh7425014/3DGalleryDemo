package com.demo.render;

import java.io.InputStream;
import java.util.ArrayList;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.demo.Interface.ILoadTexImage;
import com.demo.Interface.LoadTexImage_Bitmap;
import com.demo.render.GridQuad.GridLocation;
import com.example.gallerydemo.R;
import com.example.gallerydemo.RenderView;
import com.example.utils.Texture;
import com.ophone.ogles.lib.Rotate4f;
import com.ophone.ogles.lib.Vector3f;

public class GridDrawable {
	public static void initDisplayItem(final RenderView renderView) {
		final DisPlayList disPlayList = DisPlayList.getInstance(); 
		final int ITEM_NUM = 5;	// 目前暂时列出5张图片框架
		for (int index = 0; index < ITEM_NUM; index++) {
			DisplayItem displayItem = new DisplayItem();
			ArrayList<GridQuad> gridQuads = displayItem.getGridQuads();
			
			Texture[] textures = new Texture[1];
			textures[0] = new Texture() {
				public ILoadTexImage load() {
					Bitmap bmp = BitmapFactory.decodeStream(renderView.getResources().openRawResource(R.raw.bg));
					return new LoadTexImage_Bitmap(bmp);
				}
			};
			renderView.prime(textures[0], true);
			gridQuads.add(new FrameGrid(textures));	// 每张图片框架里面目前只放一张图片
			
			for (int index2 = 0; index2 < gridQuads.size(); index2++) {
				GridQuad gQuad = gridQuads.get(index2);
				GridLocation srcLocation = new GridLocation();
				srcLocation.SetPosition(new Vector3f(0, 10, 0));
				GridLocation desLocation = new GridLocation();
				desLocation.SetPosition(new Vector3f((float)index-1.0f, 0.0f, -(float)index-2.0f));
				desLocation.SetRotate(new Rotate4f(0, 0.0f, 0.0f, 1.0f));
				gQuad.setSrcLocation(srcLocation);
				gQuad.setDesLocation(desLocation);
			}
			disPlayList.add(displayItem);
			gridQuads = null;
		}
	}
	
	public static void freshDesLocation() {
		freshDesLocation(DisPlayList.getInstance().getFocusItem());
	}
	
	private static void freshDesLocation(int focusItem) {
		DisPlayList disPlayList = DisPlayList.getInstance();
		if ((focusItem < 0) || (focusItem > disPlayList.size() - 1)) {
			return;
		}
		for (int index = 0; index < disPlayList.size(); index++) {
			DisplayItem displayItem = disPlayList.get(index);
			ArrayList<GridQuad> gridQuads = displayItem.getGridQuads();
			for (int index2 = 0; index2 < gridQuads.size(); index2++) {
				int positionIndex = index - focusItem;
				if (positionIndex < 0) {
					positionIndex += disPlayList.size();
				}
				GridQuad gQuad = gridQuads.get(index2);
				GridLocation desLocation = new GridLocation();
				desLocation.SetPosition(new Vector3f((float)positionIndex-1.0f, 0.0f, -(float)positionIndex-2.0f));
				desLocation.SetRotate(new Rotate4f(0, 0.0f, 0.0f, 1.0f));
				gQuad.setDesLocation(desLocation);
			}
		}
	}
}
