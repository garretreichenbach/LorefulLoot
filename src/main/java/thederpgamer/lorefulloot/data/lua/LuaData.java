package thederpgamer.lorefulloot.data.lua;

import org.luaj.vm2.LuaUserdata;

/**
 * [Description]
 *
 * @author Garret Reichenbach
 */
public abstract class LuaData extends LuaUserdata {

	protected LuaData(Object... args) {
		super(args);
		checkArgs(args);
	}

	public final void checkArgs(Object... args) {
		Class<?>[] argTypes = getArgTypes();
		if(args.length != argTypes.length) {
			throw new IllegalArgumentException("Invalid number of arguments: expected " + argTypes.length + ", got " + args.length);
		}
		for(int i = 0; i < args.length; i++) {
			if(!argTypes[i].isInstance(args[i])) {
				throw new IllegalArgumentException("Invalid argument type at index " + i + ": expected " + argTypes[i].getName() + ", got " + args[i].getClass().getName());
			}
		}
	}

	public abstract Class<?>[] getArgTypes();
}
