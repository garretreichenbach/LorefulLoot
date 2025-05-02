package thederpgamer.lorefulloot.manager;

import api.listener.events.entity.SegmentControllerOverheatEvent;
import com.bulletphysics.linearmath.Transform;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
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
import thederpgamer.lorefulloot.data.generation.GenerationConfigOld;
import thederpgamer.lorefulloot.utils.DataUtils;
import thederpgamer.lorefulloot.utils.MiscUtils;

import javax.vecmath.Vector3f;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

/**
 * Manager class for handling generation of entities and loot.
 *
 * @author TheDerpGamer
 */
public class GenerationManager {

	public static final HashMap<String, GenerationConfigOld> configMap = new HashMap<>();
	public static final HashMap<SegmentController, Long> overheatMap = new HashMap<>();

	public static void initialize() {
		File generationFolder = getGenerationFolder();
		if(generationFolder.listFiles() == null || Objects.requireNonNull(generationFolder.listFiles()).length == 0) {
			LorefulLoot.getInstance().logException("No generation configs found! You must add configs to the moddata/config folder", new Exception("No generation configs found!"));
		}
		try {
			for(File file : Objects.requireNonNull(generationFolder.listFiles())) {
				if(file.getName().endsWith(".json")) {
					FileInputStream fileInputStream = new FileInputStream(file);
					GenerationConfigOld config = new GenerationConfigOld(new JSONObject(IOUtils.toString(fileInputStream, StandardCharsets.UTF_8)));
					configMap.put(file.getName().replace(".json", ""), config);
				}
			}
		} catch(Exception exception) {
			LorefulLoot.getInstance().logException("Failed to load generation configs! You must add configs to the moddata/config/ folder", exception);
		}
		LorefulLoot.getInstance().logInfo("Loaded " + configMap.size() + " generation configs.");
	}

	private static File getGenerationFolder() {
		File generationFolder = new File(DataUtils.getResourcesPath(), "config");
		if(!generationFolder.exists()) generationFolder.mkdirs();
		return generationFolder;
	}

	public static void generateForSector(Sector sector, SectorInformation.SectorType sectorType, boolean force) {
		try {
			ArrayList<GenerationConfigOld> possibleGens = getPossibleGens(sector, sectorType);
			if(!possibleGens.isEmpty()) {
				for(GenerationConfigOld config : possibleGens) {
					if(force || Math.random() <= config.getWeight()) createEntity(config, sector);
				}
			} else {
				LorefulLoot.getInstance().logWarning("No generation configs found for sector type " + sectorType.name() + "!");
			}
		} catch(Exception exception) {
			LorefulLoot.getInstance().logException("Failed to generate entities for sector " + sector.pos + "!", exception);
		}
	}

	private static ArrayList<GenerationConfigOld> getPossibleGens(Sector sector, SectorInformation.SectorType sectorType) {

		return null;
	}

	private static void createEntity(final GenerationConfigOld config, Sector sector) {
		SegmentControllerOutline<?> scOutline = null;
		try {
			scOutline = BluePrintController.active.loadBluePrint(
					GameServerState.instance,
					config.getName(),
					config.genName(),
					getRandomTransformInSector(),
					-1,
					0,
					sector.pos,
					"LorefulLoot",
					PlayerState.buffer,
					null,
					false,
					new ChildStats(false)
			);
		} catch(Exception exception) {
			LorefulLoot.getInstance().logException("Failed to create entity for sector " + sector.pos + "!", exception);
		}

		if(scOutline != null) {
			try {
				final SegmentController controller = scOutline.spawn(
						sector.pos,
						false,
						new ChildStats(false),
						new SegmentControllerSpawnCallbackDirect(GameServerState.instance, sector.pos) {
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
			if(entity.getCoreOverheatingTimeLeftMS(System.currentTimeMillis()) > 15000) overheatMap.put(entity, entity.getCoreOverheatingTimeLeftMS(System.currentTimeMillis()));
			else {
				if(!entity.getRealName().contains("[Wreckage]")) {
					entity.setRealName(entity.getRealName() + " [Wreckage]");
					entity.setFactionId(0);
					entity.setScrap(true);
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
