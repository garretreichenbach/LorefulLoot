package thederpgamer.lorefulloot.data.generation;

import api.utils.game.inventory.ItemStack;
import org.json.JSONArray;
import org.json.JSONObject;
import thederpgamer.lorefulloot.data.JSONSerializable;
import thederpgamer.lorefulloot.data.LootItemStack;

import java.util.Random;

/**
 * Data class for configuring an entity's generation.
 *
 * @author TheDerpGamer
 */
public class GenerationConfigOld implements JSONSerializable {

	private String name;
	private float weight;
	private LootItemStack[] itemStacks;

	public GenerationConfigOld(JSONObject json) {
		fromJSON(json);
	}

	public GenerationConfigOld(String path, String name, float weight, LootItemStack... itemStacks) {
		this.name = name;
		this.weight = weight;
		this.itemStacks = itemStacks;
	}

	public String getName() {
		return name;
	}

	public float getWeight() {
		return weight;
	}

	public LootItemStack[] getItemStacks() {
		return itemStacks;
	}

	public ItemStack[] genItemStacks() {
		Random random = new Random();
		int stackCount = 0;
		for(LootItemStack itemStack : itemStacks) {
			if(random.nextFloat() <= itemStack.getWeight()) stackCount ++;
		}
		ItemStack[] stacks = new ItemStack[stackCount];
		int index = 0;
		if(stackCount == 0) return stacks;
		for(LootItemStack itemStack : itemStacks) {
			stacks[index] = new ItemStack(itemStack.getId(), itemStack.getAmount());
			index ++;
		}
		return stacks;
	}

	@Override
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		json.put("name", name);
		json.put("weight", weight);
		JSONArray stacks = new JSONArray();
		for(LootItemStack itemStack : itemStacks) {
			stacks.put(itemStack.toJSON());
		}
		return json;
	}

	@Override
	public void fromJSON(JSONObject json) {
		name = json.getString("name");
		weight = (float) json.getDouble("weight");
		JSONArray stacks = json.getJSONArray("item_stacks");
		itemStacks = new LootItemStack[stacks.length()];
		for(int i = 0; i < stacks.length(); i ++) {
			itemStacks[i] = new LootItemStack(stacks.getJSONObject(i));
		}
	}

	public String genName() {
		return name + "_" + (new Random().nextInt(10000)) + " [Wreckage]";
	}
}
