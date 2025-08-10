package thederpgamer.lorefulloot.lua;

import org.luaj.vm2.*;
import org.luaj.vm2.lib.TwoArgFunction;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class LuaData extends LuaUserdata {

	private static final LuaTable metaTable = new LuaTable();
	private static final Map<Class<? extends LuaData>, Map<String, WrapMethod>> methodWraps = new HashMap<>();
	private static final Map<Class<? extends LuaData>, Map<String, LuaFunction>> apiMethods = new HashMap<>();
	private static final Map<Class<? extends LuaData>, Set<String>> methods = new HashMap<>();

	private static final LuaFunction function = new TwoArgFunction() {
		@Override
		public LuaValue call(LuaValue udi, LuaValue key) {
			if(!(udi instanceof LuaData) || !key.isstring()) {
				throw new LuaError("LuaMade userdatum must be indexed by string.");
			}
			LuaData ud = (LuaData) udi;
			String methodName = key.tojstring();
			LuaFunction apiMethod = getAPIMethod(ud.getClass(), methodName);

			if(!methodWraps.containsKey(ud.getClass())) {
				methodWraps.put(ud.getClass(), new HashMap<String, WrapMethod>());
			}
			if(!methods.containsKey(ud.getClass())) {
				methods.put(ud.getClass(), WrapUtils.listMethods(ud.getClass()));
			}

			if(apiMethod != null) {
				return apiMethod;
			} else if(!methods.get(ud.getClass()).contains(methodName)) {
				throw new LuaError(String.format("LuaData '%s' has no such method '%s'.", udi.getClass(), methodName));
			}

			Map<String, WrapMethod> methods = methodWraps.get(ud.getClass());
			if(!methods.containsKey(methodName)) {
				methods.put(methodName, new WrapMethod(methodName, ud.getClass()));
			}
			return methods.get(key.tojstring());
		}
	};

	protected LuaData() {
		super(new Object(), getMeta());
	}

	private static LuaTable getMeta() {
		metaTable.set(INDEX, function);
		return metaTable;
	}

	/**
	 * Returns a grafted Lua method for the given class.
	 * @param clazz The given class.
	 * @param name The name of the method.
	 * @return The grafted Lua method, or null if none is find.
	 */
	private static LuaFunction getAPIMethod(Class<? extends LuaData> clazz, String name) {
		for(Class<?> c = clazz; LuaData.class.isAssignableFrom(c); c = c.getSuperclass()) {
			if(apiMethods.containsKey(c)) {
				if(apiMethods.get(c).containsKey(name)) return apiMethods.get(c).get(name);
			}
		}
		return null;
	}

	/**
	 * Grafts a method onto an existing class.
	 * @param clazz The class to register the method for.
	 * @param function The method to register.
	 */
	public static void graftMethod(Class<? extends LuaData> clazz, String name, LuaFunction function) {
		if(!apiMethods.containsKey(clazz)) {
			apiMethods.put(clazz, new HashMap<String, LuaFunction>());
		}
		apiMethods.get(clazz).put(name, function);
	}
}
