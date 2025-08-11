package thederpgamer.lorefulloot.manager;

import api.listener.events.entity.SegmentControllerOverheatEvent;
import com.bulletphysics.linearmath.Transform;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.schema.game.common.controller.ManagedUsableSegmentController;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SectorInformation;
import org.schema.game.server.controller.BluePrintController;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.ServerConfig;
import org.schema.game.server.data.blueprint.ChildStats;
import org.schema.game.server.data.blueprint.SegmentControllerOutline;
import org.schema.game.server.data.blueprint.SegmentControllerSpawnCallbackDirect;
import thederpgamer.lorefulloot.LorefulLoot;
import thederpgamer.lorefulloot.data.generation.GenerationScriptLoader;
import thederpgamer.lorefulloot.lua.data.entity.EntityGenData;
import thederpgamer.lorefulloot.lua.data.item.ItemStack;
import thederpgamer.lorefulloot.lua.data.misc.LuaVector3f;
import thederpgamer.lorefulloot.lua.data.misc.LuaVector4f;
import thederpgamer.lorefulloot.utils.DataUtils;
import thederpgamer.lorefulloot.utils.MiscUtils;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Objects;

/**
 * Manager class for handling generation of entities and loot.
 *
 * @author TheDerpGamer
 */
public class GenerationManager {

	public static final HashMap<SegmentController, Long> overheatMap = new HashMap<>();

	public static void initialize() {
		File scriptsFolder = new File(DataUtils.getWorldDataPath(), "scripts");
		//Load default scripts
		if(!scriptsFolder.exists() || scriptsFolder.listFiles() == null || Objects.requireNonNull(scriptsFolder.listFiles()).length == 0) {
			scriptsFolder.mkdirs();
			try {
				InputStream inputStream = LorefulLoot.getInstance().getJarResource("default_scripts.zip");
				if(inputStream != null) {
					DataUtils.unzip(inputStream, scriptsFolder);
					LorefulLoot.getInstance().logInfo("Default generation scripts copied to: " + scriptsFolder.getAbsolutePath());
				} else {
					LorefulLoot.getInstance().logWarning("Default generation script not found!");
				}
			} catch(Exception exception) {
				LorefulLoot.getInstance().logException("Failed to copy default generation scripts!", exception);
			}
		}
		//Load default blueprints
		File blueprintsFolder = new File(DataUtils.getWorldDataPath(), "blueprints");
		if(!blueprintsFolder.exists() || blueprintsFolder.listFiles() == null || Objects.requireNonNull(blueprintsFolder.listFiles()).length == 0) {
			blueprintsFolder.mkdirs();
			try {
				InputStream inputStream = LorefulLoot.getInstance().getJarResource("default_blueprints.zip");
				if(inputStream != null) {
					DataUtils.unzip(inputStream, blueprintsFolder);
					LorefulLoot.getInstance().logInfo("Default blueprints copied to: " + blueprintsFolder.getAbsolutePath());
				} else {
					LorefulLoot.getInstance().logWarning("Default blueprints not found!");
				}
			} catch(Exception exception) {
				LorefulLoot.getInstance().logException("Failed to copy default blueprint!", exception);
			}
		}
		GenerationScriptLoader.initialize();
	}

	public static void generateForSector(Sector sector, SectorInformation.SectorType sectorType, Vector4f starColor, boolean forced) {
		try {
			File scriptsFolder = new File(DataUtils.getWorldDataPath(), "scripts");
			if(!scriptsFolder.exists() || scriptsFolder.listFiles() == null || Objects.requireNonNull(scriptsFolder.listFiles()).length == 0) {
				LorefulLoot.getInstance().logWarning("No scripts found in: " + scriptsFolder.getAbsolutePath());
				return;
			}
			for(File scriptFile : Objects.requireNonNull(scriptsFolder.listFiles())) {
				if(!scriptFile.getName().endsWith(".lua")) continue; //Only process Lua
				String scriptName = scriptFile.getName().substring(0, scriptFile.getName().length() - 4); //Remove .lua extension
				if(scriptName.isEmpty()) continue; //Skip empty names
				LorefulLoot.getInstance().logInfo("Loading script: " + scriptName);
				LuaValue script = GenerationScriptLoader.getGenerationScript(scriptName);
				if(script == null || script.isnil()) {
					LorefulLoot.getInstance().logWarning("Script " + scriptName + " returned nil or failed to load.");
					continue;
				}
				LorefulLoot.getInstance().logInfo("Executing script: " + scriptName);

				LuaTable args = new LuaTable();
				args.set("sector", new LuaVector3f(sector.pos.x, sector.pos.y, sector.pos.z));
				args.set("sectorType", LuaValue.valueOf(sectorType.name()));
				args.set("starColor", new LuaVector4f(starColor.x, starColor.y, starColor.z, starColor.w));
				args.set("forced", LuaValue.valueOf(forced));
				script.call(args);

				/*if(script.isfunction()) {
					LuaValue result = script.call();
					if(result.istable()) {
						LuaTable entities = result.checktable();
						if(entities.length() == 0) {
							LorefulLoot.getInstance().logInfo("Script returned an empty table of entities to spawn: " + scriptName);
							continue;
						}
						for(int i = 1; i <= entities.length(); i++) {
							EntityGenData entityData = (EntityGenData) entities.get(i).checkuserdata(EntityGenData.class);
							if(entityData != null) {
								createEntity(entityData, sector);
							} else {
								LorefulLoot.getInstance().logWarning("Entity data is null for index: " + i + " in script: " + scriptName);
							}
						}
					} else {
						LorefulLoot.getInstance().logWarning("Script did not return a table of entities to spawn: " + scriptName);
					}
				}*/
			}
		} catch(Exception exception) {
			LorefulLoot.getInstance().logException("Failed to generate entities for sector " + sector.pos + "!", exception);
		}
	}

	private static void createEntity(final EntityGenData config, final Sector sector) {
		SegmentControllerOutline<?> scOutline = null;
		try {
			scOutline = BluePrintController.active.loadBluePrint(GameServerState.instance, config.getBpName(), "[Wreckage] " + config.getEntityName() + "_" + System.currentTimeMillis(), getRandomTransformInSector(), -1, 0, sector.pos, "LorefulLoot", PlayerState.buffer, null, false, new ChildStats(false));
		} catch(Exception exception) {
			LorefulLoot.getInstance().logException("Failed to create entity for sector " + sector.pos + "!", exception);
		}

		if(scOutline != null) {
			try {
				final SegmentController controller = scOutline.spawn(sector.pos, false, new ChildStats(false), new SegmentControllerSpawnCallbackDirect(GameServerState.instance, sector.pos) {
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
							LuaTable lootTable = config.getLoot();
							if(lootTable == null || lootTable.length() == 0) {
								LorefulLoot.getInstance().logWarning("No loot defined for entity: " + config.getBpName() + " in sector: " + sector.pos);
								return;
							}
							ItemStack[] lootArray = new ItemStack[lootTable.length()];
							for(int i = 1; i <= lootTable.length(); i++) {
								LuaValue itemData = lootTable.get(i);
								if(itemData.isuserdata(ItemStack.class)) {
									ItemStack itemStack = (ItemStack) itemData.checkuserdata(ItemStack.class);
									lootArray[i - 1] = itemStack;
								} else {
									LorefulLoot.getInstance().logWarning("Invalid item data at index " + i + " for entity: " + config.getBpName() + " in sector: " + sector.pos);
								}
							}
							MiscUtils.wreckShip((Ship) controller, lootArray);
						} catch(Exception exception) {
							exception.printStackTrace();
						}
					}
				}).start();
			} catch(Exception exception) {
				LorefulLoot.getInstance().logException("Failed to spawn entity for sector " + sector.pos + "!", exception);
			}
		}
	}

	private static Transform getRandomTransformInSector() {
		//Gen Random Position
		int sectorSize = (Integer) ServerConfig.SECTOR_SIZE.getCurrentState();
		int x = (int) (Math.random() * sectorSize);
		int y = (int) (Math.random() * sectorSize);
		int z = (int) (Math.random() * sectorSize);
		Vector3f posInSector = new Vector3f(x, y, z);

		//Gen random rotation
		float yaw = (float) (Math.random() * 360);
		float pitch = (float) (Math.random() * 360);
		float roll = (float) (Math.random() * 360);

		//Create Transform
		Transform transform = new Transform();
		transform.setIdentity();
		transform.origin.set(posInSector);
		//Set the transforms matrix
		transform.basis.rotX(pitch);
		transform.basis.rotY(yaw);
		transform.basis.rotZ(roll);
		return transform; //Hope and pray it doesn't collide with anything
	}

	public static void createShipWreckFromCombat(SegmentControllerOverheatEvent event) {
		SegmentController entity = event.getEntity();
		if(!overheatMap.containsKey(entity)) {
			if(entity.getCoreOverheatingTimeLeftMS(System.currentTimeMillis()) > 15000) {
				overheatMap.put(entity, entity.getCoreOverheatingTimeLeftMS(System.currentTimeMillis()));
			} else {
				if(!entity.getRealName().startsWith("[Wreckage] ")) {
					entity.setRealName("[Wreckage] " + entity.getRealName() + "_" + System.currentTimeMillis());
					entity.setFactionId(0);
					entity.setScrap(false);
					entity.setMinable(true);
					entity.setMarkedForDeletePermanentIncludingDocks(false);
					entity.setMarkedForDeleteVolatileIncludingDocks(false);
					entity.stopCoreOverheating();
					if(entity instanceof ManagedUsableSegmentController<?>) {
						ManagedUsableSegmentController<?> managedUsableSegmentController = (ManagedUsableSegmentController<?>) entity;
						managedUsableSegmentController.getManagerContainer().getPowerInterface().requestRecalibrate();
					}
				}
				overheatMap.remove(entity);
			}
		}
	}
}
