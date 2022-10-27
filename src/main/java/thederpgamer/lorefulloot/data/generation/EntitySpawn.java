package thederpgamer.lorefulloot.data.generation;

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

	public EntitySpawn(String name, String bpName, float weight) {
		this.name = name;
		this.bpName = bpName;
		this.weight = weight;
		this.factionId = 0;
		this.factionName = "Unknown";
		this.loreName = name + "_" + (new Random().nextInt(9999) + 1000);
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
		return factionName;
	}

	public void setFactionName(String factionName) {
		this.factionName = factionName;
	}

	public String getLoreName() {
		return loreName;
	}

	public void setLoreName(String loreName) {
		this.loreName = loreName;
	}
}
