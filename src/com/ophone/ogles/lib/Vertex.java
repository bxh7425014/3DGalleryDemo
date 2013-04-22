package com.ophone.ogles.lib;

/**
 * 自定义的顶点格式
 * @author Yong
 *
 */
public class Vertex {
	/**
	 * 位置
	 */
	public Vector3f p = new Vector3f();
	/**
	 * 法线
	 */
	public Vector3f n = new Vector3f();
	/**
	 * 颜色
	 */
	public Vector4f c = new Vector4f();
	/**
	 * 纹理坐标
	 */
	public Vector3f t = new Vector3f();
	
	public void set(Vertex v) {
		p.set(v.p);
		n.set(v.n);
		c.set(v.c);
		t.set(v.t);
	}
}
