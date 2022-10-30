package thederpgamer.lorefulloot.data.generation;

/**
 * [Description]
 *
 * @author TheDerpGamer (MrGoose#0027)
 */
public class EntityLore {

	private String name;
	private String header;
	private String content;
	private String type;
	private float weight;
	private SpawnCondition[] conditions;

	public EntityLore(String name, String header, String content, String type, float weight) {
		this.name = name;
		this.header = header;
		this.content = content;
		this.type = type;
		this.weight = weight;
		this.conditions = new SpawnCondition[0];
	}

	public EntityLore(String name, String header, String content, String type, float weight, SpawnCondition[] conditions) {
		this.name = name;
		this.header = header;
		this.content = content;
		this.type = type;
		this.weight = weight;
		this.conditions = conditions;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public float getWeight() {
		return weight;
	}

	public void setWeight(float weight) {
		this.weight = weight;
	}

	public SpawnCondition[] getConditions() {
		return conditions;
	}

	public void setConditions(SpawnCondition[] conditions) {
		this.conditions = conditions;
	}
}