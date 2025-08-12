package thederpgamer.lorefulloot.lua.data.entity;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.ManagedUsableSegmentController;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.server.controller.BluePrintController;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.blueprint.ChildStats;
import org.schema.game.server.data.blueprint.SegmentControllerOutline;
import org.schema.game.server.data.blueprint.SegmentControllerSpawnCallbackDirect;
import thederpgamer.lorefulloot.LorefulLoot;
import thederpgamer.lorefulloot.lua.data.LuaCallable;
import thederpgamer.lorefulloot.lua.data.item.ItemStack;
import thederpgamer.lorefulloot.lua.data.misc.LuaVector3f;
import thederpgamer.lorefulloot.manager.GenerationManager;
import thederpgamer.lorefulloot.utils.MiscUtils;

public class PirateGenData extends EntityGenData {

	@LuaCallable
	public PirateGenData(String bpName, String entityName, LuaTable loot) {
		super(bpName, entityName, loot);
	}

	@Override
	public void spawnEntity(LuaVector3f sector) {
		SegmentControllerOutline<?> scOutline = null;
		final Vector3i sectorPos = new Vector3i(sector.getX(), sector.getY(), sector.getZ());
		try {
			scOutline = BluePrintController.active.loadBluePrint(GameServerState.instance, getBpName(), getEntityName() + "_" + System.currentTimeMillis(), GenerationManager.getRandomTransformInSector(), -1, -1, sectorPos, "LorefulLoot", PlayerState.buffer, null, false, new ChildStats(false));
		} catch(Exception exception) {
			LorefulLoot.getInstance().logException("Failed to create entity for sector " + sectorPos + "!", exception);
		}
		if(scOutline != null) {
			try {
				final SegmentController controller = scOutline.spawn(sectorPos, false, new ChildStats(false), new SegmentControllerSpawnCallbackDirect(GameServerState.instance, sectorPos) {
					@Override
					public void onNoDocker() {

					}
				});
				(new Thread() {
					@Override
					public void run() {
						try {
							controller.getSegmentBuffer().restructBB();
							sleep(5000);
							if(!controller.isFullyLoadedWithDock()) {
								return;
							}
							if(getLoot() == null || getLoot().length() == 0) {
								LorefulLoot.getInstance().logWarning("No loot defined for entity: " + getBpName() + " in sector: " + sectorPos);
								return;
							}
							ItemStack[] lootArray = new ItemStack[getLoot().length()];
							for(int i = 1; i <= getLoot().length(); i++) {
								LuaValue itemData = getLoot().get(i);
								if(itemData.isuserdata(ItemStack.class)) {
									ItemStack itemStack = (ItemStack) itemData.checkuserdata(ItemStack.class);
									lootArray[i - 1] = itemStack;
								} else {
									LorefulLoot.getInstance().logWarning("Invalid item data at index " + i + " for entity: " + getBpName() + " in sector: " + sectorPos);
								}
							}
							if(lootArray.length != 0) {
								MiscUtils.fillInventories((ManagedUsableSegmentController<?>) controller, lootArray);
							}
						} catch(Exception exception) {
							exception.printStackTrace();
						}
					}
				}).start();
			} catch(Exception exception) {
				LorefulLoot.getInstance().logException("Failed to spawn entity for sector " + sectorPos + "!", exception);
			}
		}
	}
}
