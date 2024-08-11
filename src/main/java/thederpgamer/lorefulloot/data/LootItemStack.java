package thederpgamer.lorefulloot.data;

import org.json.JSONObject;

/**
 * LootItemStack data class.
 *
 * @author TheDerpGamer
 */
public class LootItemStack implements JSONSerializable {

	private short id;
	private int amount;
	private float weight;

	public LootItemStack(JSONObject json) {
		fromJSON(json);
	}

	public LootItemStack(short id, int amount, float weight) {
		this.id = id;
		this.amount = amount;
		this.weight = weight;
	}

	@Override
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		json.put("id", id);
		json.put("amount", amount);
		json.put("weight", weight);
		return json;
	}

	@Override
	public void fromJSON(JSONObject json) {
		id = (short) json.getInt("id");
		amount = json.getInt("amount");
		weight = (float) json.getDouble("weight");
	}

	public short getId() {
		return id;
	}

	public void setId(short id) {
		this.id = id;
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public float getWeight() {
		return weight;
	}

	public void setWeight(float weight) {
		this.weight = weight;
	}
}
