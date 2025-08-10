package thederpgamer.lorefulloot.lua.data.entity;

import org.luaj.vm2.LuaTable;
import thederpgamer.lorefulloot.lua.LuaData;

public class EntityGenData extends LuaData {

	private String bpName;
	private String entityName;
	private LuaTable loot;

	public EntityGenData(String bpName, String entityName, LuaTable loot) {
		this.bpName = bpName;
		this.entityName = entityName;
		this.loot = loot;
	}

	public String getBpName() {
		return bpName;
	}

	public void setBpName(String bpName) {
		this.bpName = bpName;
	}

	public String getEntityName() {
		return entityName;
	}

	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}

	public LuaTable getLoot() {
		return loot;
	}

	public void setLoot(LuaTable loot) {
		this.loot = loot;
	}
}
