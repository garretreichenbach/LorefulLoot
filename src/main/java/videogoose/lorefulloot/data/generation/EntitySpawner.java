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

		int factionId = rule.getFactionId();
		String spawnName = rule.isWreck() ? rule.getEntityName() + " [Wreckage]" : rule.getEntityName() + "_" + System.currentTimeMillis();

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

						java.util.List<ItemStack> rolledLoot = new java.util.ArrayList<>();
						if(rule.getLoot() != null && !rule.getLoot().isEmpty()) {
							if (rule.getMinLootRolls() > 0 && rule.getMaxLootRolls() >= rule.getMinLootRolls()) {
								int rolls = rule.getMinLootRolls() + (int)(Math.random() * (rule.getMaxLootRolls() - rule.getMinLootRolls() + 1));
								int totalWeight = 0;
								for (LootRule lr : rule.getLoot()) {
									totalWeight += lr.getWeight();
								}
								if (totalWeight > 0) {
									for (int i = 0; i < rolls; i++) {
										int roll = (int)(Math.random() * totalWeight);
										int currentWeight = 0;
										for (LootRule lr : rule.getLoot()) {
											currentWeight += lr.getWeight();
											if (roll < currentWeight) {
												int count = lr.getCount() > 0 ? lr.getCount() : lr.getMinCount() + (int)(Math.random() * (lr.getMaxCount() - lr.getMinCount() + 1));
												try {
													rolledLoot.add(new ItemStack(lr.getItemName(), count));
												} catch(Exception exception) {
													LorefulLoot.getInstance().logWarning("Invalid item data for entity: " + rule.getBpName() + " in sector: " + sectorPos);
												}
												break;
											}
										}
									}
								}
							} else {
								for (LootRule lr : rule.getLoot()) {
									int count = lr.getCount() > 0 ? lr.getCount() : lr.getMinCount() + (int)(Math.random() * (lr.getMaxCount() - lr.getMinCount() + 1));
									try {
										rolledLoot.add(new ItemStack(lr.getItemName(), count));
									} catch(Exception exception) {
										LorefulLoot.getInstance().logWarning("Invalid item data for entity: " + rule.getBpName() + " in sector: " + sectorPos);
									}
								}
							}
						}
						ItemStack[] lootArray = rolledLoot.toArray(new ItemStack[0]);

						if(rule.isWreck()) {
							MiscUtils.wreckEntity((ManagedUsableSegmentController<?>) controller);
							WreckageManager.addWreckage(controller, "generated");
						}
						
						if(rule.isActivateAi() && controller instanceof Ship) {
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
