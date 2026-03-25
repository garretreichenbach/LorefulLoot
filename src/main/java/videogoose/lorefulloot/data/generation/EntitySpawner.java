package videogoose.lorefulloot.data.generation;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.ManagedUsableSegmentController;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.server.controller.BluePrintController;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.blueprint.ChildStats;
import org.schema.game.server.data.blueprint.SegmentControllerOutline;
import org.schema.game.server.data.blueprint.SegmentControllerSpawnCallbackDirect;
import videogoose.lorefulloot.LorefulLoot;
import videogoose.lorefulloot.data.item.ItemStack;
import videogoose.lorefulloot.manager.GenerationManager;
import videogoose.lorefulloot.manager.WreckageManager;
import videogoose.lorefulloot.utils.MiscUtils;

public class EntitySpawner {

	public static void spawnEntity(GenerationRule rule, Vector3i sectorPos) {
		SegmentControllerOutline<?> scOutline = null;

		int factionId = rule.isPirate() ? -1 : 0;
		String spawnName = rule.isPirate() ? rule.getEntityName() + "_" + System.currentTimeMillis() : rule.getEntityName() + " [Wreckage]";

		try {
			scOutline = BluePrintController.active.loadBluePrint(GameServerState.instance, rule.getBpName(), spawnName, GenerationManager.getRandomTransformInSector(), -1, factionId, sectorPos, "LorefulLoot", PlayerState.buffer, null, false, new ChildStats(false));
		} catch(Exception exception) {
			LorefulLoot.getInstance().logException("Failed to create entity for sector " + sectorPos + "!", exception);
		}

		if(scOutline != null) {
			try {
				SegmentController controller = scOutline.spawn(sectorPos, false, new ChildStats(false), new SegmentControllerSpawnCallbackDirect(GameServerState.instance, sectorPos) {
					@Override
					public void onNoDocker() {
					}
				});

				(new Thread(() -> {
					try {
						controller.getSegmentBuffer().restructBB();
						Thread.sleep(5000);
						if(!controller.isFullyLoadedWithDock()) {
							return;
						}

						ItemStack[] lootArray = new ItemStack[0];
						if(rule.getLoot() != null && !rule.getLoot().isEmpty()) {
							lootArray = new ItemStack[rule.getLoot().size()];
							for(int i = 0; i < rule.getLoot().size(); i++) {
								LootRule lootRule = rule.getLoot().get(i);
								try {
									lootArray[i] = new ItemStack(lootRule.getItemName(), lootRule.getCount());
								} catch(Exception exception) {
									LorefulLoot.getInstance().logWarning("Invalid item data for entity: " + rule.getBpName() + " in sector: " + sectorPos);
								}
							}
						}

						if(!rule.isPirate()) {
							MiscUtils.wreckEntity((ManagedUsableSegmentController<?>) controller);
							WreckageManager.addWreckage(controller, "generated");
						} else if(controller instanceof Ship) {
							Ship ship = (Ship) controller;
							ship.activateAI(true, true);
						}

						if(!controller.isMarkedForPermanentDelete() && lootArray.length != 0) {
							MiscUtils.fillInventories((ManagedUsableSegmentController<?>) controller, lootArray);
						}
					} catch(Exception exception) {
						LorefulLoot.getInstance().logException("Failed to create entity for sector " + sectorPos + "!", exception);
					}
				})).start();
			} catch(Exception exception) {
				LorefulLoot.getInstance().logException("Failed to spawn entity for sector " + sectorPos + "!", exception);
			}
		}
	}
}
