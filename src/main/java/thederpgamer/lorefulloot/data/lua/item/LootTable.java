package thederpgamer.lorefulloot.data.lua.item;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import thederpgamer.lorefulloot.data.lua.LuaData;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents an ItemStack loot table.
 */
public class LootTable extends LuaData {

	public LootTable(HashMap<ItemStack, Float> table) {
		super(table);
	}

	@Override
	public Class<?>[] getArgTypes() {
		return new Class[] {HashMap.class};
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("LootTable{");
		for(Map.Entry<ItemStack, Float> entry : getTable().entrySet()) {
			sb.append(entry.getKey()).append(": ").append(entry.getValue()).append(", ");
		}
		sb.setLength(sb.length() - 2); // Remove the last comma and space
		sb.append("}");
		return sb.toString();
	}

	@Override
	public boolean equals(Object o) {
		if(this == o) return true;
		if(!(o instanceof LootTable)) return false;
		LootTable other = (LootTable) o;
		return getTable().equals(other.getTable());
	}

	public HashMap<ItemStack, Float> getTable() {
		HashMap<ItemStack, Float> table = new HashMap<>();
		LuaTable luaTable = (LuaTable) get(1);
		for(LuaValue key : luaTable.keys()) {
			ItemStack itemStack = (ItemStack) key;
			Float value = luaTable.get(key).tofloat();
			table.put(itemStack, value);
		}
		return table;
	}

	public void setTable(HashMap<ItemStack, Float> table) {
		LuaTable luaTable = new LuaTable();
		for(Map.Entry<ItemStack, Float> entry : table.entrySet()) luaTable.set(entry.getKey(), valueOf(entry.getValue()));
		set(1, luaTable);
	}
}
