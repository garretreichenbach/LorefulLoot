package thederpgamer.lorefulloot.lua.data.misc;

import thederpgamer.lorefulloot.lua.LuaData;

public class LuaVector4f extends LuaData {

	private float x;
	private float y;
	private float z;
	private float w;

	public LuaVector4f(float x, float y, float z, float w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}

	@Override
	public String typename() {
		return "Vector4f";
	}

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}

	public float getZ() {
		return z;
	}

	public void setZ(float z) {
		this.z = z;
	}

	public float getW() {
		return w;
	}

	public void setW(float w) {
		this.w = w;
	}
}
