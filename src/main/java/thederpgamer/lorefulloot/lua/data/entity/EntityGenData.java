package thederpgamer.lorefulloot.lua.data.entity;

import thederpgamer.lorefulloot.lua.LuaCallable;
import thederpgamer.lorefulloot.lua.LuaData;
import thederpgamer.lorefulloot.lua.data.item.ItemStack;

public class EntityGenData extends LuaData {

	private String bpName;
	private String entityName;
	private ItemStack[] loot;

	public EntityGenData(String bpName, String entityName, ItemStack[] loot) {
		this.bpName = bpName;
		this.entityName = entityName;
		this.loot = loot;
	}

	public EntityGenData(String bpName, String entityName) {
		this.bpName = bpName;
		this.entityName = entityName;
		loot = new ItemStack[0];
	}

	@LuaCallable
	public String getBpName() {
		return bpName;
	}

	@LuaCallable
	public void setBpName(String bpName) {
		this.bpName = bpName;
	}

	@LuaCallable
	public String getEntityName() {
		return entityName;
	}

	@LuaCallable
	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}

	@LuaCallable
	public ItemStack[] getLoot() {
		return loot;
	}

	@LuaCallable
	public void setLoot(ItemStack[] loot) {
		this.loot = loot;
	}
}
