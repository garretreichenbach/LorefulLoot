package thederpgamer.lorefulloot.lua;

import org.luaj.vm2.LuaValue;
import thederpgamer.lorefulloot.data.generation.GenerationScriptLoader;

public abstract class LuaData extends LuaValue {
	@Override
	public int type() {
		return GenerationScriptLoader.classes.indexOf(getClass());
	}

	@Override
	public String typename() {
		return getClass().getSimpleName();
	}
}
