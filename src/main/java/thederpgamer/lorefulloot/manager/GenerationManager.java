package thederpgamer.lorefulloot.manager;

import api.listener.events.entity.SegmentControllerOverheatEvent;
import com.bulletphysics.linearmath.Transform;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.schema.game.common.controller.ManagedUsableSegmentController;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SectorInformation;
import org.schema.game.server.data.ServerConfig;
import thederpgamer.lorefulloot.LorefulLoot;
import thederpgamer.lorefulloot.data.generation.GenerationScriptLoader;
import thederpgamer.lorefulloot.utils.DataUtils;

import javax.vecmath.Vector3f;
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
				InputStream inputStream = GenerationManager.class.getResourceAsStream("default_scripts.zip");
				if(inputStream != null) {
					File defaultGenerationFile = new File(scriptsFolder, "default_scripts.zip");
					DataUtils.unzip(inputStream, scriptsFolder);
					LorefulLoot.getInstance().logInfo("Default generation scripts copied to: " + defaultGenerationFile.getAbsolutePath());
				} else {
					LorefulLoot.getInstance().logWarning("Default generation scripts resource not found!");
				}
			} catch(Exception exception) {
				LorefulLoot.getInstance().logException("Failed to copy default generation script!", exception);
			}
		}
		//Load default blueprints
		File blueprintsFolder = new File(DataUtils.getWorldDataPath(), "blueprints");
		if(!blueprintsFolder.exists() || blueprintsFolder.listFiles() == null || Objects.requireNonNull(blueprintsFolder.listFiles()).length == 0) {
			blueprintsFolder.mkdirs();
			try {
				InputStream inputStream = GenerationManager.class.getResourceAsStream("default_blueprints.zip");
				if(inputStream != null) {
					DataUtils.unzip(inputStream, blueprintsFolder);
					LorefulLoot.getInstance().logInfo("Default blueprints copied to: " + blueprintsFolder.getAbsolutePath());
				} else {
					LorefulLoot.getInstance().logWarning("Default blueprints resource not found!");
				}
			} catch(Exception exception) {
				LorefulLoot.getInstance().logException("Failed to copy default blueprint!", exception);
			}
		}
	}

	public static void generateForSector(Sector sector, SectorInformation.SectorType sectorType, boolean force) {
		try {
			for(LuaValue value : GenerationScriptLoader.getAllScripts()) {
				if(value.isstring()) {
					String scriptName = value.toString();
					LorefulLoot.getInstance().logInfo("Executing generation script: " + scriptName);
					LuaValue script = GenerationScriptLoader.loadScript(scriptName);
					if(script != null) {
						LuaTable args = new LuaTable();
						args.set("sector_pos", LuaValue.valueOf(sector.pos.toString()));
						args.set("sector_type", LuaValue.valueOf(sectorType.name()));
						args.set("forced", LuaValue.valueOf(force));
						script.call(args);
					} else {
						LorefulLoot.getInstance().logWarning("Failed to load generation script: " + scriptName);
					}
				}
			}
		} catch(Exception exception) {
			LorefulLoot.getInstance().logException("Failed to generate entities for sector " + sector.pos + "!", exception);
		}
	}

	/*private static void createEntity(final GenerationConfigOld config, Sector sector) {
		SegmentControllerOutline<?> scOutline = null;
		try {
			scOutline = BluePrintController.active.loadBluePrint(GameServerState.instance, config.getName(), config.genName(), getRandomTransformInSector(), -1, 0, sector.pos, "LorefulLoot", PlayerState.buffer, null, false, new ChildStats(false));
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
				new Thread() {
					@Override
					public void run() {
						try {
							sleep(1000);
							controller.getSegmentBuffer().restructBB();
							MiscUtils.fillInventories((Ship) controller, config.genItemStacks());
							MiscUtils.wreckShip((Ship) controller);
						} catch(Exception exception) {
							exception.printStackTrace();
						}
					}
				}.start();
			} catch(Exception exception) {
				LorefulLoot.getInstance().logException("Failed to spawn entity for sector " + sector.pos + "!", exception);
			}
		}
	}*/

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
				if(!entity.getRealName().contains("[Wreckage]")) {
					entity.setRealName(entity.getRealName() + " [Wreckage]");
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
