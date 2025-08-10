package thederpgamer.lorefulloot.lua.data.item;

import org.luaj.vm2.LuaTable;
import thederpgamer.lorefulloot.lua.LuaCallable;
import thederpgamer.lorefulloot.lua.LuaData;

import java.util.HashMap;

/**
 * Represents an ItemStack loot table.
 */
public class LootTable extends LuaData {

	private LuaTable table;

	public LootTable(LuaTable table) {
		this.table = table;
	}

	@LuaCallable
	public LuaTable getTable() {
		return table;
	}

	@LuaCallable
	public void setTable(LuaTable table) {
		this.table = table;
	}

	public HashMap<ItemStack, Float> getTableAsMap() {
		HashMap<ItemStack, Float> map = new HashMap<>();
		for(Object key : table.keys()) {
			if(key instanceof ItemStack) {
				ItemStack itemStack = (ItemStack) key;
				Float weight = table.get(itemStack).isnumber() ? table.get(itemStack).tofloat() : null;
				if(weight != null) {
					map.put(itemStack, weight);
				} else {
					throw new IllegalArgumentException("Weight for item " + itemStack + " is not a number.");
				}
			}
		}
		return map;
	}
}
