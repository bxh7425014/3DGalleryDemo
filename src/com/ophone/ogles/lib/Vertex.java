package com.ophone.ogles.lib;

/**
 * �Զ���Ķ����ʽ
 * @author Yong
 *
 */
public class Vertex {
	/**
	 * λ��
	 */
	public Vector3f p = new Vector3f();
	/**
	 * ����
	 */
	public Vector3f n = new Vector3f();
	/**
	 * ��ɫ
	 */
	public Vector4f c = new Vector4f();
	/**
	 * ��������
	 */
	public Vector3f t = new Vector3f();
	
	public void set(Vertex v) {
		p.set(v.p);
		n.set(v.n);
		c.set(v.c);
		t.set(v.t);
	}
}
