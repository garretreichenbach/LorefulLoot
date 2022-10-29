package thederpgamer.lorefulloot.data.generation;

import thederpgamer.lorefulloot.data.ItemStack;

import java.util.Random;

/**
 * [Description]
 *
 * @author TheDerpGamer (MrGoose#0027)
 */
public class EntitySpawn {

	private String name;
	private String bpName;
	private float weight;
	private int factionId;
	private String factionName;
	private String loreName;
	private ItemStack[] itemStacks;
	private EntityLore entityLore;

	public EntitySpawn(String name, String bpName, float weight) {
		this.name = name;
		this.bpName = bpName;
		this.weight = weight;
		this.factionId = 0;
		this.factionName = "Unknown";
		this.loreName = name + "_" + (new Random().nextInt(99999) + 10000);
		this.itemStacks = new ItemStack[0];
	}

	public EntitySpawn(String name, String bpName, float weight, ItemStack[] itemStacks) {
		this.name = name;
		this.bpName = bpName;
		this.weight = weight;
		this.factionId = factionId;
		this.factionName = factionName;
		this.loreName = loreName;
		this.itemStacks = itemStacks;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBpName() {
		return bpName;
	}

	public void setBpName(String bpName) {
		this.bpName = bpName;
	}

	public float getWeight() {
		return weight;
	}

	public void setWeight(float weight) {
		this.weight = weight;
	}

	public int getFactionId() {
		return factionId;
	}

	public void setFactionId(int factionId) {
		this.factionId = factionId;
	}

	public String getFactionName() {
		if(factionName == null) factionName = "Unknown";
		return factionName;
	}

	public void setFactionName(String factionName) {
		this.factionName = factionName;
	}

	public String getLoreName() {
		//if(loreName == null) loreName = name + "_" + (new Random().nextInt(9999) + 1000);
		//return loreName;
		return name + "_" + (new Random().nextInt(9999) + 1000);
	}

	public void setLoreName(String loreName) {
		this.loreName = loreName;
	}

	public ItemStack[] getItems() {
		return itemStacks;
	}

	public void setItems(ItemStack[] itemStacks) {
		this.itemStacks = itemStacks;
	}

	public EntityLore getEntityLore() {
		return entityLore;
	}

	public void setEntityLore(EntityLore entityLore) {
		this.entityLore = entityLore;
	}
}
