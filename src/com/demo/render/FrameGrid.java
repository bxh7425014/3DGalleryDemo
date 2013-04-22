package com.demo.render;

import com.demo.render.GridQuad;
import com.example.utils.Constants;
import com.example.utils.Texture;
import com.ophone.ogles.lib.Vector3f;

public class FrameGrid extends GridQuad {
	private static final Vector3f mOffsetPosition = new Vector3f(0, 0, 0);
	public FrameGrid(Texture[] textures) {
		super(4.28f/2, 2.8f/2, Constants.TEXCOORDS_BMP, mOffsetPosition, 0, textures);
	}
}
