package thederpgamer.lorefulloot.manager;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.InventoryMap;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.game.common.data.player.inventory.NoSlotFreeException;
import thederpgamer.lorefulloot.LorefulLoot;

import java.util.logging.Level;

public class LootManager {

	/**
	 * Fills a segment controller with loot.
	 *
	 * @param segmentController Segment controller to fill with loot.
	 */
	public static void generateLoot(SegmentController segmentController) throws NoSlotFreeException {
		switch(segmentController.getType()) {
			case SPACE_STATION:
				if(ConfigManager.getLootConfig().getConfigurableValue("[ALL_STATIONS]", "") != null) {
					String lootString = ConfigManager.getLootConfig().getConfigurableValue("[ALL_STATIONS]", "");
					String[] values = lootString.split("; ");
					fill(segmentController, convertToIntArray(values[0]), convertToIntArray(values[1]), convertToIntArray(values[2]));
					LorefulLoot.log.log(Level.INFO, "Loot generated for " + segmentController.getName());
				} else {
					//Todo: Go by entity name
				}
				break;
		}
	}

	private static int[] convertToIntArray(String value) {
		String[] values = value.split(", ");
		int[] intArray = new int[values.length];
		for(int i = 0; i < values.length; i++) {
			intArray[i] = Integer.parseInt(values[i]);
		}
		return intArray;
	}

	private static void fill(SegmentController segmentController, int[] ids, int[] amounts, int[] chances) throws NoSlotFreeException {
		ManagerContainer<?> managerContainer = ((ManagedSegmentController) segmentController).getManagerContainer();
		InventoryMap inventoryMap = managerContainer.getInventories();
		for(Inventory inventory : inventoryMap.inventoriesList) {
			for(int i = 0; i < ids.length; i++) if(Math.random() * 100 < chances[i]) inventory.getFreeSlot(ids[i], amounts[i]);
			inventory.sendAll();
		}
	}
}