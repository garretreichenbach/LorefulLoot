package thederpgamer.lorefulloot.data.lua.item;

import thederpgamer.lorefulloot.data.lua.LuaCallable;
import thederpgamer.lorefulloot.data.lua.LuaData;

/**
 * Represents an ItemStack in Lua.
 */
public class ItemStack extends LuaData {

	public ItemStack(short id, int count) {
		super(id, count);
	}

	@Override
	public Class<?>[] getArgTypes() {
		return new Class<?>[] {short.class, int.class};
	}

	@Override
	public String toString() {
		return "ItemStack{id=" + getId() + ", count=" + getCount() + "}";
	}

	@Override
	public boolean equals(Object o) {
		if(this == o) return true;
		if(!(o instanceof ItemStack)) return false;
		ItemStack other = (ItemStack) o;
		return getId() == other.getId() && getCount() == other.getCount();
	}

	@LuaCallable
	public short getId() {
		return get(1).toshort();
	}

	@LuaCallable
	public int getCount() {
		return get(2).toint();
	}

	@LuaCallable
	public void setId(short id) {
		set(1, valueOf(id));
	}

	@LuaCallable
	public void setCount(int count) {
		set(2, valueOf(count));
	}
}
