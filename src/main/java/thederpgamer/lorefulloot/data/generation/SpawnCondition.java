package thederpgamer.lorefulloot.data.generation;

/**
 * [Description]
 *
 * @author TheDerpGamer (MrGoose#0027)
 */
public class SpawnCondition {

	private String name;
	private String value;

	public SpawnCondition(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
