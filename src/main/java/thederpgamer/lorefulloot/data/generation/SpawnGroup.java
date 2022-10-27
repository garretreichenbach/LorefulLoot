package thederpgamer.lorefulloot.data.generation;

/**
 * [Description]
 *
 * @author TheDerpGamer (MrGoose#0027)
 */
public class SpawnGroup {

	private String name;
	private SpawnCondition[] conditions;
	private EntitySpawn[] spawns;

	public SpawnGroup(String name, SpawnCondition[] conditions, EntitySpawn[] spawns) {
		this.name = name;
		this.conditions = conditions;
		this.spawns = spawns;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public SpawnCondition[] getConditions() {
		return conditions;
	}

	public void setConditions(SpawnCondition[] conditions) {
		this.conditions = conditions;
	}

	public EntitySpawn[] getSpawns() {
		return spawns;
	}

	public void setSpawns(EntitySpawn[] spawns) {
		this.spawns = spawns;
	}
}
