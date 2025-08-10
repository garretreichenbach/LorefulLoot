package thederpgamer.lorefulloot.lua.data.item;

import api.utils.element.Blocks;
import org.schema.game.common.data.element.ElementKeyMap;
import thederpgamer.lorefulloot.lua.LuaCallable;
import thederpgamer.lorefulloot.lua.LuaData;

import java.util.Locale;

/**
 * Represents an ItemStack in Lua.
 */
public class ItemStack extends LuaData {

	private Short id;
	private Integer count;

	public ItemStack(Short id, Integer count) {
		this.id = id;
		this.count = count;
	}

	public ItemStack(String name, Integer count) {
		id = Blocks.valueOf(name.toUpperCase(Locale.ENGLISH).replaceAll(" ", "_")).getId();
		if(!ElementKeyMap.isValidType(id)) {
			throw new IllegalArgumentException("Invalid item name: " + name);
		}
		this.count = count;
	}

	@LuaCallable
	public Short getId() {
		return id;
	}

	@LuaCallable
	public void setId(Short id) {
		if(!ElementKeyMap.isValidType(id)) {
			throw new IllegalArgumentException("Invalid item ID: " + id);
		}
		this.id = id;
	}

	@LuaCallable
	public Integer getCount() {
		return count;
	}

	@LuaCallable
	public void setCount(Integer count) {
		if(count < 0) {
			throw new IllegalArgumentException("Count cannot be negative: " + count);
		}
		this.count = count;
	}
}
