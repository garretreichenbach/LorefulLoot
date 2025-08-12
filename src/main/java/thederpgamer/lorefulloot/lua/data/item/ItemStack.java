package thederpgamer.lorefulloot.lua.data.item;

import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementKeyMap;
import thederpgamer.lorefulloot.lua.LuaData;
import thederpgamer.lorefulloot.lua.data.LuaCallable;
import thederpgamer.lorefulloot.utils.MiscUtils;

/**
 * Represents an ItemStack in Lua.
 */
public class ItemStack extends LuaData {

	private short id;
	private int count;

	@LuaCallable
	public ItemStack(String name, int count) {
		id = MiscUtils.getItemIdFromName(name);
		if(id == Element.TYPE_NONE) {
			throw new IllegalArgumentException("Invalid item name: " + name);
		}
		this.count = count;
	}

	@LuaCallable
	public short getId() {
		return id;
	}

	@LuaCallable
	public void setId(short id) {
		this.id = id;
	}

	@LuaCallable
	public int getCount() {
		return count;
	}

	@LuaCallable
	public void setCount(int count) {
		if(count < 0) {
			throw new IllegalArgumentException("Count cannot be negative: " + count);
		}
		this.count = count;
	}

	@LuaCallable
	public String getIdName() {
		return ElementKeyMap.getInfo(id).idName;
	}

	@LuaCallable
	public String getName() {
		return ElementKeyMap.getInfo(id).name;
	}
}
