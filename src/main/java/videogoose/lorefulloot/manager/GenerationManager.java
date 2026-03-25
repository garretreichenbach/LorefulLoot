package videogoose.lorefulloot.manager;

import api.listener.events.entity.SegmentControllerOverheatEvent;
import com.bulletphysics.linearmath.Transform;
import com.google.gson.Gson;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.ManagedUsableSegmentController;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SectorInformation;
import org.schema.game.common.data.world.StellarSystem;
import org.schema.game.server.data.ServerConfig;
import videogoose.lorefulloot.LorefulLoot;
import videogoose.lorefulloot.data.generation.EntitySpawner;
import videogoose.lorefulloot.data.generation.GenerationRule;
import videogoose.lorefulloot.utils.DataUtils;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.io.File;
import java.io.FileReader;
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
		File jsonFolder = new File(DataUtils.getWorldDataPath(), "json");
		//Load default scripts
		if(!jsonFolder.exists() || jsonFolder.listFiles() == null || Objects.requireNonNull(jsonFolder.listFiles()).length == 0) {
			jsonFolder.mkdirs();
			try {
				InputStream inputStream = LorefulLoot.getInstance().getJarResource("default_json.zip");
				if(inputStream != null) {
					DataUtils.unzip(inputStream, jsonFolder);
					LorefulLoot.getInstance().logInfo("Default generation scripts copied to: " + jsonFolder.getAbsolutePath());
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
	}

	public static void generateForSector(Sector sector, SectorInformation.SectorType sectorType, Vector4f starColor, boolean forced) {
		try {
			File jsonFolder = new File(DataUtils.getWorldDataPath(), "json");
			if(!jsonFolder.exists() || jsonFolder.listFiles() == null || Objects.requireNonNull(jsonFolder.listFiles()).length == 0) {
				LorefulLoot.getInstance().logWarning("No json found in: " + jsonFolder.getAbsolutePath());
				return;
			}
			Gson gson = new Gson();
			for(File scriptFile : Objects.requireNonNull(jsonFolder.listFiles())) {
				if(!scriptFile.getName().endsWith(".json")) continue; //Only process json
				String scriptName = scriptFile.getName().substring(0, scriptFile.getName().length() - 5); //Remove .json extension
				if(scriptName.isEmpty()) continue; //Skip empty names
				LorefulLoot.getInstance().logInfo("Loading script: " + scriptName);

				try(FileReader reader = new FileReader(scriptFile)) {
					GenerationRule[] rules = gson.fromJson(reader, GenerationRule[].class);
					if(rules == null) continue;

					for(GenerationRule rule : rules) {
						if(!forced) {
							if(rule.getSpawnChance() < 100.0f) {
								float chance = (float) (Math.random() * 100.0f);
								if(chance > rule.getSpawnChance()) {
									continue;
								}
							}
							if(rule.getAllowedSectorTypes() != null && !rule.getAllowedSectorTypes().isEmpty()) {
								if(!rule.getAllowedSectorTypes().contains(sectorType.name())) {
									continue;
								}
							}

							if(rule.getMinSector() != null && rule.getMinSector().length == 3) {
								if(sector.pos.x < rule.getMinSector()[0] || sector.pos.y < rule.getMinSector()[1] || sector.pos.z < rule.getMinSector()[2]) {
									continue;
								}
							}
							if(rule.getMaxSector() != null && rule.getMaxSector().length == 3) {
								if(sector.pos.x > rule.getMaxSector()[0] || sector.pos.y > rule.getMaxSector()[1] || sector.pos.z > rule.getMaxSector()[2]) {
									continue;
								}
							}

							double distance = 0;
							try {
								StellarSystem system = sector._getSystem();
								if(system != null) {
									int sysSize = 16;
									double sunX = system.getPos().x * sysSize;
									double sunY = system.getPos().y * sysSize;
									double sunZ = system.getPos().z * sysSize;
									distance = Math.sqrt(Math.pow(sector.pos.x - sunX, 2) + Math.pow(sector.pos.y - sunY, 2) + Math.pow(sector.pos.z - sunZ, 2));
								}
							} catch(Exception ignored) {
							}

							if(rule.getMinDistance() >= 0 && distance < rule.getMinDistance()) {
								continue;
							}

							if(rule.getMaxDistance() >= 0 && distance > rule.getMaxDistance()) {
								continue;
							}

							if(rule.getAllowedStarColors() != null && !rule.getAllowedStarColors().isEmpty()) {
								if(starColor != null) {
									String colorString = String.format(java.util.Locale.ENGLISH, "%.1f, %.1f, %.1f", starColor.x, starColor.y, starColor.z);
									if(!rule.getAllowedStarColors().contains(colorString)) {
										continue;
									}
								} else {
									continue; // Require matching star color, but none available
								}
							}
						}

						EntitySpawner.spawnEntity(rule, new Vector3i(sector.pos.x, sector.pos.y, sector.pos.z));
					}
				} catch(Exception e) {
					LorefulLoot.getInstance().logException("Failed to load/execute JSON generation rule: " + scriptName, e);
				}
			}
		} catch(Exception exception) {
			LorefulLoot.getInstance().logException("Failed to generate entities for sector " + sector.pos + "!", exception);
		}
	}

	public static Transform getRandomTransformInSector() {
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
				if(!WreckageManager.getWreckages().containsKey(entity.getUniqueIdentifier())) {
					entity.setRealName(entity.getRealName() + "[Wreckage]");
					entity.setFactionId(0);
					entity.setMarkedForDeletePermanentIncludingDocks(false);
					entity.setMarkedForDeleteVolatileIncludingDocks(false);
					entity.stopCoreOverheating();
					WreckageManager.addWreckage(entity, "combat");
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
