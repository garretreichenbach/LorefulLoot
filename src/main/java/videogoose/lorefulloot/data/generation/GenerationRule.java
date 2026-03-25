package videogoose.lorefulloot.data.generation;

import java.util.List;

public class GenerationRule {

	private String bpName;
	private String entityName;
	private boolean isPirate;
	private float spawnChance = 100.0f; // 0.0 to 100.0
	private List<String> allowedSectorTypes;
	private List<String> allowedStarColors;
	private List<LootRule> loot;

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

	public boolean isPirate() {
		return isPirate;
	}

	public void setPirate(boolean pirate) {
		isPirate = pirate;
	}

	public float getSpawnChance() {
		return spawnChance;
	}

	public void setSpawnChance(float spawnChance) {
		this.spawnChance = spawnChance;
	}

	public List<String> getAllowedSectorTypes() {
		return allowedSectorTypes;
	}

	public void setAllowedSectorTypes(List<String> allowedSectorTypes) {
		this.allowedSectorTypes = allowedSectorTypes;
	}

	public List<String> getAllowedStarColors() {
		return allowedStarColors;
	}

	public void setAllowedStarColors(List<String> allowedStarColors) {
		this.allowedStarColors = allowedStarColors;
	}

	public List<LootRule> getLoot() {
		return loot;
	}

	public void setLoot(List<LootRule> loot) {
		this.loot = loot;
	}
}
