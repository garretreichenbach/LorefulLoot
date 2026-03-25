package videogoose.lorefulloot.data.item;

import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementKeyMap;
import videogoose.lorefulloot.utils.MiscUtils;

/**
 * Represents an ItemStack.
 */
public class ItemStack {

	private short id;
	private int count;

	public ItemStack(String name, int count) {
		id = MiscUtils.getItemIdFromName(name);
		if(id == Element.TYPE_NONE) {
			throw new IllegalArgumentException("Invalid item name: " + name);
		}
		this.count = count;
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
		if(count < 0) {
			throw new IllegalArgumentException("Count cannot be negative: " + count);
		}
		this.count = count;
	}

	public String getIdName() {
		return ElementKeyMap.getInfo(id).idName;
	}

	public String getName() {
		return ElementKeyMap.getInfo(id).name;
	}
}
