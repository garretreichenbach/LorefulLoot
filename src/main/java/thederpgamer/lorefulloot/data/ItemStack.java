package thederpgamer.lorefulloot.data;

import org.schema.game.common.data.player.inventory.Inventory;

/**
 * [Description]
 *
 * @author TheDerpGamer (MrGoose#0027)
 */
public class ItemStack {

	private short id;
	private int count;
	private float weight;

	public ItemStack(short id, int count, float weight) {
		this.id = id;
		this.count = count;
		this.weight = weight;
	}

	public short getId() {
		return id;
	}

	public void setId(short id) {
		this.id = id;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public float getWeight() {
		return weight;
	}

	public void setWeight(float weight) {
		this.weight = weight;
	}

	public void addTo(Inventory inventory) {
		inventory.putNextFreeSlot(id, count, 0);
	}
}
