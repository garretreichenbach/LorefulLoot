package thederpgamer.lorefulloot.data.generation;

import com.google.common.io.Files;
import org.luaj.vm2.*;
import org.luaj.vm2.compiler.LuaC;
import org.luaj.vm2.lib.Bit32Lib;
import org.luaj.vm2.lib.PackageLib;
import org.luaj.vm2.lib.StringLib;
import org.luaj.vm2.lib.TableLib;
import org.luaj.vm2.lib.jse.JseBaseLib;
import org.luaj.vm2.lib.jse.JseMathLib;
import thederpgamer.lorefulloot.lua.data.item.ItemStack;
import thederpgamer.lorefulloot.lua.data.item.LootTable;
import thederpgamer.lorefulloot.manager.ConfigManager;
import thederpgamer.lorefulloot.utils.DataUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * Manages creation, loading, and execution of generation scripts.
 *
 * @author TheDerpGamer
 */
public class GenerationScriptLoader {

	private static Class<?>[] classes = new Class[] {
			ItemStack.class,
			LootTable.class
	};

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

		loadScriptFunctions(globals);
		return globals;
	}

	/**
	 * Loads custom generation-related script functions into the Lua environment.
	 * @param globals The Lua environment to load the functions into.
	 */
	public static void loadScriptFunctions(Globals globals) {
		for(Class<?> clazz : classes) {
			try {
				InputStream stream = clazz.getResourceAsStream("/" + clazz.getName().replace(".", "/") + ".class");
				if(stream == null) {
					throw new RuntimeException("Class resource not found: " + clazz.getName());
				}
				LuaValue luaClass = LuaValue.userdataOf(clazz);
				globals.set(clazz.getSimpleName(), luaClass);
			} catch(Exception e) {
				throw new RuntimeException("Failed to load class: " + clazz.getName(), e);
			}
		}
	}

	/**
	 * Loads and executes a Lua script.
	 *
	 * @param script The Lua script to load and execute.
	 * @return The result of the script execution.
	 */
	public static LuaValue loadScript(String script, LuaTable args) throws IOException {
		Globals globals = initializeLuaEnvironment();
		globals.set("args", args);
		String rawScript = Files.toString(new File(DataUtils.getResourcesPath() + "/scripts/" + script + ".lua"), StandardCharsets.UTF_8);
		LuaValue chunk = globals.load(rawScript);
		if(chunk.isfunction()) return chunk.call();
		else return chunk;
	}

	public static ArrayList<LuaValue> getAllScripts() {
		ArrayList<LuaValue> scripts = new ArrayList<>();
		Globals globals = initializeLuaEnvironment();
		for(LuaValue key : globals.keys()) {
			LuaValue value = globals.get(key);
			scripts.add(value);
		}
		return scripts;
	}
}
