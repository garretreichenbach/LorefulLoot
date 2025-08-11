package thederpgamer.lorefulloot.lua.utils;

import org.schema.game.common.data.element.ElementCategory;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import thederpgamer.lorefulloot.lua.LuaData;
import thederpgamer.lorefulloot.lua.data.item.ItemStack;

import java.util.ArrayList;

public class ItemUtils extends LuaData {

	public static ItemStack[] getRandomStacks(int min, int max, String category) {
		ArrayList<ItemStack> stacks = new ArrayList<>();
		for(ElementInformation info : ElementKeyMap.infoArray) {
			if(info == null || !ElementKeyMap.isValidType(info.id)) {
				continue;
			}
			if(info.isDeprecated() || !info.isInRecipe() || !info.isShoppable()) {
				continue;
			}
			ElementCategory rootCategory = ElementKeyMap.getCategoryHirarchy();
			if(!rootCategory.getCategory().equals(category)) {
				continue;
			}
			ElementCategory cat = rootCategory.find(category);
			if(cat == null || cat.find(info) == null) {
				continue;
			}
			int amount = (int) (Math.random() * (max - min + 1)) + min;
			ItemStack stack = new ItemStack(info.idName, amount);
			stacks.add(stack);
		}

		//Pick a random subset of stacks
		int size = (int) (Math.random() * stacks.size()) + 1;
		ItemStack[] randomStacks = new ItemStack[size];
		for(int i = 0; i < size; i++) {
			int index = (int) (Math.random() * stacks.size());
			randomStacks[i] = stacks.get(index);
			stacks.remove(index); // Remove to avoid duplicates
		}
		return randomStacks;
	}
}
