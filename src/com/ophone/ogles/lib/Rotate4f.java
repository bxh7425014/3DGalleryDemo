package com.ophone.ogles.lib;

public class Rotate4f {
	public float angle, rx, ry, rz;
	
	public Rotate4f() {
		
	}
	
	public Rotate4f(float angle, float rx, float ry, float rz) {
		this.angle = angle;
		this.rx = rx;
		this.ry = ry;
		this.rz = rz;
	}
	
	public void set(float angle, float rx, float ry, float rz) {
		this.angle = angle;
		this.rx = rx;
		this.ry = ry;
		this.rz = rz;
	}
	
	public void set(Rotate4f v) {
		set(v.angle, v.rx, v.ry, v.rz);
	}
	
	public void addOffsetAngle(float angle) {
		this.angle += angle;
	}
}
