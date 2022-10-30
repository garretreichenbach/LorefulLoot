package thederpgamer.lorefulloot.data.generation;

/**
 * [Description]
 *
 * @author TheDerpGamer (MrGoose#0027)
 */
public class LoreCategory {

	private String name;
	private EntityLore[] values;

	public LoreCategory(String name, EntityLore[] values) {
		this.name = name;
		this.values = values;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public EntityLore[] getValues() {
		return values;
	}

	public void setValues(EntityLore[] values) {
		this.values = values;
	}
}