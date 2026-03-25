package videogoose.lorefulloot.data.generation;

import java.util.List;

public class GenerationRule {

	private String bpName;
	private String entityName;
	private int factionId = 0;
	private boolean isWreck = true;
	private boolean activateAi = false;
	private float spawnChance = 100.0f; // 0.0 to 100.0
	private int minLootRolls = -1;
	private int maxLootRolls = -1;
	private int minDistance = -1;
	private int maxDistance = -1;
	private int[] minSector;
	private int[] maxSector;
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

	public int getFactionId() {
		return factionId;
	}

	public void setFactionId(int factionId) {
		this.factionId = factionId;
	}

	public boolean isWreck() {
		return isWreck;
	}

	public void setWreck(boolean isWreck) {
		this.isWreck = isWreck;
	}

	public boolean isActivateAi() {
		return activateAi;
	}

	public void setActivateAi(boolean activateAi) {
		this.activateAi = activateAi;
	}

	public float getSpawnChance() {
		return spawnChance;
	}

	public void setSpawnChance(float spawnChance) {
		this.spawnChance = spawnChance;
	}

	public int getMinLootRolls() {
		return minLootRolls;
	}

	public void setMinLootRolls(int minLootRolls) {
		this.minLootRolls = minLootRolls;
	}

	public int getMaxLootRolls() {
		return maxLootRolls;
	}

	public void setMaxLootRolls(int maxLootRolls) {
		this.maxLootRolls = maxLootRolls;
	}

	public int getMinDistance() {
		return minDistance;
	}

	public void setMinDistance(int minDistance) {
		this.minDistance = minDistance;
	}

	public int getMaxDistance() {
		return maxDistance;
	}

	public void setMaxDistance(int maxDistance) {
		this.maxDistance = maxDistance;
	}

	public int[] getMinSector() {
		return minSector;
	}

	public void setMinSector(int[] minSector) {
		this.minSector = minSector;
	}

	public int[] getMaxSector() {
		return maxSector;
	}

	public void setMaxSector(int[] maxSector) {
		this.maxSector = maxSector;
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
