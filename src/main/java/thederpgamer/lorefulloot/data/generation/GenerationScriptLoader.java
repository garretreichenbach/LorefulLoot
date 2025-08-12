package thederpgamer.lorefulloot.data.generation;

import com.google.common.io.Files;
import org.luaj.vm2.*;
import org.luaj.vm2.compiler.LuaC;
import org.luaj.vm2.lib.*;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JseBaseLib;
import org.luaj.vm2.lib.jse.JseMathLib;
import thederpgamer.lorefulloot.LorefulLoot;
import thederpgamer.lorefulloot.lua.data.LuaCallable;
import thederpgamer.lorefulloot.lua.data.entity.EntityGenData;
import thederpgamer.lorefulloot.lua.data.entity.PirateGenData;
import thederpgamer.lorefulloot.lua.data.item.ItemStack;
import thederpgamer.lorefulloot.lua.data.item.meta.LogBook;
import thederpgamer.lorefulloot.lua.data.item.meta.MetaItem;
import thederpgamer.lorefulloot.lua.data.misc.LuaVector3f;
import thederpgamer.lorefulloot.lua.data.misc.LuaVector4f;
import thederpgamer.lorefulloot.lua.utils.ItemUtils;
import thederpgamer.lorefulloot.manager.ConfigManager;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Manages creation, loading, and execution of generation scripts.
 *
 * @author TheDerpGamer
 */
public class GenerationScriptLoader {

	public static final ArrayList<Class<?>> classes = new ArrayList<Class<?>>() {
		{
			add(ItemStack.class);
			add(MetaItem.class);
			add(LogBook.class);
			add(EntityGenData.class);
			add(PirateGenData.class);
			add(LuaVector3f.class);
			add(LuaVector4f.class);
			add(ItemUtils.class);
		}
	};

	/**
	 * Initializes the Lua environment with the necessary libraries and settings.
	 *
	 * @return The initialized Lua environment.
	 */
	public static Globals initializeLuaEnvironment() {
		Globals globals = new Globals();
		globals.load(new JseBaseLib());
		globals.load(new PackageLib());
		globals.load(new StringLib());
		globals.load(new TableLib());
		globals.load(new JseMathLib());
		globals.load(new Bit32Lib());
		LuaC.install(globals);

		LuaString.s_metatable = new ReadOnlyLuaTable(LuaString.s_metatable);
		if(ConfigManager.getMainConfig().getBoolean("restrict-lua-libs")) {
			ArrayList<String> whitelistedLibs = ConfigManager.getMainConfig().getList("whitelisted-lua-libs");
			for(LuaValue key : globals.keys()) {
				LuaValue value = globals.get(key);
				if(value instanceof LuaTable) {
					LuaTable table = (LuaTable) value;
					if(table.getmetatable() != null) {
						table.setmetatable(new ReadOnlyLuaTable(table.getmetatable()));
					}
				}
				boolean whitelisted = false;
				for(String lib : whitelistedLibs) {
					if(key.tojstring().equals(lib)) {
						whitelisted = true;
						break;
					}
				}
				if(!whitelisted) {
					globals.set(key, LuaValue.NIL);
				}
			}
		}

		for(final Class<?> clazz : classes) {
			LuaTable classTable = new LuaTable();
			LuaTable metaTable = new LuaTable();
			try {
				for(final Constructor<?> constructor : clazz.getConstructors()) {
					if(constructor.isAnnotationPresent(LuaCallable.class)) {
						VarArgFunction constructorFunction = new VarArgFunction() {
							@Override
							public Varargs invoke(Varargs args) {
								if(args.narg() < 2) {
									return error("Not enough arguments for constructor of class: " + clazz.getSimpleName());
								}
								LuaValue[] luaArgs = new LuaValue[args.narg() - 1];
								for(int i = 0; i < luaArgs.length; i++) {
									luaArgs[i] = args.arg(i + 2); // Skip the first argument which is the class itself
								}
								Object[] javaArgs = new Object[args.narg() - 1];
								for(int i = 0; i < luaArgs.length; i++) {
									if(luaArgs[i].isuserdata()) {
										javaArgs[i] = luaArgs[i].touserdata();
									} else if(luaArgs[i].isfunction()) {
										javaArgs[i] = luaArgs[i].checkfunction(); // Convert Lua function to Java
									} else if(luaArgs[i].isnil()) {
										javaArgs[i] = null; // Convert Lua nil to Java null
									} else if(luaArgs[i].isthread()) {
										javaArgs[i] = luaArgs[i].checkthread(); // Convert Lua thread to Java
									} else if(luaArgs[i].istable()) {
										javaArgs[i] = luaArgs[i].checktable();
									} else if(luaArgs[i].isuserdata() && luaArgs[i].checkuserdata() instanceof Enum) {
										javaArgs[i] = luaArgs[i].checkuserdata(Enum.class); // Convert Lua enum to Java
									} else if(luaArgs[i].isint()) {
										javaArgs[i] = luaArgs[i].toint(); // Convert Lua number to Java
									} else if(luaArgs[i].islong()) {
										javaArgs[i] = luaArgs[i].tolong(); // Convert Lua number to Java long
									} else if(luaArgs[i].isboolean()) {
										javaArgs[i] = luaArgs[i].toboolean(); // Convert Lua boolean to Java
									} else if(luaArgs[i].isnumber()) {
										javaArgs[i] = luaArgs[i].tofloat(); // Convert Lua number to Java
									} else if(luaArgs[i].isstring()) {
										javaArgs[i] = luaArgs[i].tojstring(); // Convert Lua string to Java
									} else {
										javaArgs[i] = luaArgs[i]; // Use LuaValue directly for other types
									}
								}
								try {
									Object instance = constructor.newInstance(javaArgs);
									LuaValue luaInstance = CoerceJavaToLua.coerce(instance);
									luaInstance.setmetatable(getClassMetaTable(clazz));
									return CoerceJavaToLua.coerce(luaInstance);
								} catch(Exception exception) {
									return error("Failed to create instance of class: " + clazz.getSimpleName() + " with arguments: " + args.tojstring() + "\n" + Arrays.toString(exception.getStackTrace()));
								}
							}

							/**
							 * Retrieves the metatable for a given class, only registering methods that are annotated with {@link LuaCallable}.
							 * @param clazz The class to retrieve the metatable for.
							 * @return The metatable for the class, containing
							 */
							private LuaValue getClassMetaTable(Class<?> clazz) {
								LuaTable metaTable = new LuaTable();
								for(final Method method : clazz.getMethods()) {
									if(method.isAnnotationPresent(LuaCallable.class)) {
										VarArgFunction methodFunction = new VarArgFunction() {
											@Override
											public Varargs invoke(Varargs args) {
												if(args.narg() < 1) {
													return error("Not enough arguments for method: " + method.getName());
												}
												Object[] javaArgs = new Object[args.narg() - 1];
												for(int i = 0; i < javaArgs.length; i++) {
													javaArgs[i] = args.arg(i + 2).tojstring(); // Convert LuaValue to String
												}
												try {
													Object result = method.invoke(null, javaArgs);
													return CoerceJavaToLua.coerce(result);
												} catch(Exception exception) {
													return error("Failed to invoke method: " + method.getName() + " with arguments: " + args.tojstring() + "\n" + Arrays.toString(exception.getStackTrace()));
												}
											}
										};
										metaTable.set(method.getName(), methodFunction);
									}
								}
								return metaTable;
							}
						};
						metaTable.set("__call", constructorFunction);
						classTable.set("new", constructorFunction);
						//Allow for static methods to be called without instantiation
						for(final Method method : clazz.getMethods()) {
							if(method.getDeclaringClass().equals(clazz) && method.isAnnotationPresent(LuaCallable.class)) {
								VarArgFunction methodFunction = new VarArgFunction() {
									@Override
									public Varargs invoke(Varargs args) {
										if(args.narg() < 1) {
											return error("Not enough arguments for method: " + method.getName());
										}
										Object[] javaArgs = new Object[args.narg() - 1];
										for(int i = 0; i < javaArgs.length; i++) {
											javaArgs[i] = args.arg(i + 2).tojstring(); // Convert LuaValue to String
										}
										try {
											Object result = method.invoke(null, javaArgs);
											return CoerceJavaToLua.coerce(result);
										} catch(Exception exception) {
											return error("Failed to invoke method: " + method.getName() + " with arguments: " + args.tojstring() + "\n" + Arrays.toString(exception.getStackTrace()));
										}
									}
								};
								classTable.set(method.getName(), methodFunction);
							}
						}
					}
				}
			} catch(Exception exception) {
				LorefulLoot.getInstance().logException("Failed to register class: " + clazz.getSimpleName(), exception);
			}
			classTable.setmetatable(metaTable);
			globals.set(clazz.getSimpleName(), classTable);
		}
		return globals;
	}

	public static LuaValue loadScript(File scriptFile) throws IOException {
		String scriptName = Files.getNameWithoutExtension(scriptFile.getName());
		Globals globals = initializeLuaEnvironment();
		String rawScript = Files.toString(scriptFile, java.nio.charset.StandardCharsets.UTF_8);
		LuaValue script = globals.load(rawScript);
		if(script.isnil()) {
			LorefulLoot.getInstance().logWarning("Script " + scriptName + " is empty or failed to load.");
			return LuaValue.NIL;
		}
		return script;
	}

	private static class ReadOnlyLuaTable extends LuaTable {

		public ReadOnlyLuaTable(LuaValue table) {
			presize(table.length(), 0);
			for(Varargs n = table.next(NIL); !n.arg1().isnil(); n = table.next(n.arg1())) {
				LuaValue key = n.arg1();
				LuaValue value = n.arg(2);
				super.rawset(key, value.istable() ? new ReadOnlyLuaTable(value) : value);
			}
		}

		@Override
		public LuaValue setmetatable(LuaValue metatable) {
			return error("table is read-only");
		}

		@Override
		public void set(int key, LuaValue value) {
			error("table is read-only");
		}

		@Override
		public void rawset(int key, LuaValue value) {
			error("table is read-only");
		}

		@Override
		public void rawset(LuaValue key, LuaValue value) {
			error("table is read-only");
		}

		@Override
		public LuaValue remove(int pos) {
			return error("table is read-only");
		}
	}
}
