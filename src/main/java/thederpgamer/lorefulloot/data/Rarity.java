package thederpgamer.lorefulloot.data;

/**
 * Enum for item rarity.
 *
 * @author TheDerpGamer
 */
public enum Rarity {
	COMMON(0.75f),
	UNCOMMON(0.5f),
	RARE(0.1f),
	EPIC(0.05f),
	LEGENDARY(0.01f);

	private final float weight;

	Rarity(float weight) {
		this.weight = weight;
	}

	public float getWeight() {
		return weight;
	}
}
