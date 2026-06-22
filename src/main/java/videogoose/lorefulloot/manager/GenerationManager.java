package videogoose.lorefulloot.manager;

import api.listener.events.entity.SegmentControllerOverheatEvent;
import com.bulletphysics.linearmath.Transform;
import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.ManagedUsableSegmentController;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SectorInformation;
import org.schema.game.common.data.world.StellarSystem;
import org.schema.game.common.data.world.Universe;
import org.schema.game.server.controller.BluePrintController;
import org.schema.game.server.data.Galaxy;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.ServerConfig;
import videogoose.lorefulloot.LorefulLoot;
import videogoose.lorefulloot.data.generation.EntitySpawner;
import videogoose.lorefulloot.data.generation.GenerationRule;
import videogoose.lorefulloot.utils.DataUtils;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Manager class for handling generation of entities and loot.
 *
 * @author TheDerpGamer
 */
public class GenerationManager {

	public static final HashMap<SegmentController, Long> overheatMap = new HashMap<>();

	//Bump this whenever the bundled default_blueprints.zip changes to force a clean reinstall/repair.
	private static final int DEFAULT_BLUEPRINT_VERSION = 1;

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

		//Install bundled default blueprints into StarMade's blueprints directory.
		//default_blueprints.zip contains complete blueprint FOLDERS at its top level
		//(e.g. "T180-18 mk2/header.smbph", "T180-18 mk2/ATTACHED_0/header.smbph", ...), NOT .sment
		//archives. StarMade aborts loading an entire blueprint if any (docked) header file is missing,
		//dropping it from the catalog - which is exactly what a partial/macOS copy on disk causes.
		File blueprintsFolder = new File("blueprints");
		blueprintsFolder.mkdirs();
		try {
			//Only (re)install when the bundled blueprint version hasn't been installed yet, so we repair
			//stale/incomplete copies once instead of clobbering later user edits on every boot. Bump
			//DEFAULT_BLUEPRINT_VERSION whenever the bundled blueprints change to force a clean reinstall.
			File installMarker = new File(DataUtils.getResourcesPath(), "default_blueprints.version");
			int installedVersion = -1;
			if(installMarker.exists()) {
				try(BufferedReader br = new BufferedReader(new FileReader(installMarker, StandardCharsets.UTF_8))) {
					String line = br.readLine();
					if(line != null) installedVersion = Integer.parseInt(line.trim());
				} catch(Exception ignored) {
				}
			}

			if(installedVersion >= DEFAULT_BLUEPRINT_VERSION) {
				LorefulLoot.getInstance().logInfo("Default blueprints already installed (version " + installedVersion + ").");
			} else {
				InputStream zipStream = LorefulLoot.getInstance().getJarResource("default_blueprints.zip");
				if(zipStream != null) {
					ZipInputStream zip = new ZipInputStream(zipStream);
					ZipEntry entry;
					byte[] buf = new byte[8192];
					Set<String> installed = new HashSet<>();
					int fileCount = 0;
					while((entry = zip.getNextEntry()) != null) {
						String name = entry.getName().replace('\\', '/');
						//Skip macOS AppleDouble / metadata junk that would otherwise pollute the blueprint folder.
						if(name.contains("__MACOSX/") || name.contains("/._") || name.startsWith("._") || name.endsWith(".DS_Store")) {
							zip.closeEntry();
							continue;
						}
						int slash = name.indexOf('/');
						if(slash <= 0) { zip.closeEntry(); continue; } //ignore anything not inside a blueprint folder
						String bpName = name.substring(0, slash);
						//First time we touch a blueprint this run, wipe any pre-existing (possibly broken/partial)
						//copy so we extract a clean, complete folder. Only blueprints shipped in our zip are removed.
						if(!installed.contains(bpName)) {
							installed.add(bpName);
							File bpFolder = new File(blueprintsFolder, bpName);
							if(bpFolder.exists()) {
								try {
									FileUtils.deleteDirectory(bpFolder);
								} catch(Exception ex) {
									LorefulLoot.getInstance().logWarning("Could not remove existing blueprint folder for clean reinstall: " + bpName);
								}
							}
						}
						if(entry.isDirectory()) { zip.closeEntry(); continue; }
						File target = new File(blueprintsFolder, name);
						target.getParentFile().mkdirs();
						try(FileOutputStream fos = new FileOutputStream(target)) {
							int n;
							while((n = zip.read(buf)) != -1) fos.write(buf, 0, n);
						}
						fileCount++;
						zip.closeEntry();
					}
					zip.close();
					LorefulLoot.getInstance().logInfo("Installed " + installed.size() + " default blueprint(s) (" + fileCount + " files) to: " + blueprintsFolder.getAbsolutePath());

					//Record the installed version so we don't repeat this on every boot.
					installMarker.getParentFile().mkdirs();
					try(FileOutputStream fos = new FileOutputStream(installMarker)) {
						fos.write(String.valueOf(DEFAULT_BLUEPRINT_VERSION).getBytes(StandardCharsets.UTF_8));
					}

					//StarMade caches its blueprint catalog and only rescans the directory when marked dirty.
					//Since we just changed the blueprints directory after that cache was built, mark it dirty so
					//loadBluePrint can find them (otherwise: EntityNotFountException "<bp>; blueprints: N").
					if(!installed.isEmpty() && BluePrintController.active != null) {
						BluePrintController.active.setDirty(true);
						LorefulLoot.getInstance().logInfo("Blueprint cache marked dirty to pick up installed blueprints.");
					}
				} else {
					LorefulLoot.getInstance().logWarning("Default blueprints not found!");
				}
			}
		} catch(Exception exception) {
			LorefulLoot.getInstance().logException("Failed to install default blueprints!", exception);
		}
	}

	//Sectors that were created remotely (by AI/fleets/missiles) with no player nearby, awaiting a real load.
	//The sector generation event fires the first time a sector is created - including remote creations that
	//are never activated - so we defer those here and generate them once a player actually arrives.
	private static final Set<String> pendingSectors = Collections.synchronizedSet(new HashSet<>());

	private static String sectorKey(Vector3i pos) {
		return pos.x + "," + pos.y + "," + pos.z;
	}

	/**
	 * Entry point for the sector generation event. Generates immediately if a player is within range,
	 * otherwise defers until a player loads the sector (see {@link #onSectorLoad}).
	 */
	public static void requestGeneration(Universe universe, Sector sector) {
		String key = sectorKey(sector.pos);
		try {
			if(isPlayerNear(sector)) {
				pendingSectors.remove(key);
				generateForSectorAuto(universe, sector);
			} else {
				pendingSectors.add(key);
			}
		} catch(Exception exception) {
			LorefulLoot.getInstance().logException("Failed during generation request for sector " + sector.pos + "!", exception);
		}
	}

	/**
	 * Called when an existing sector is loaded. If it was deferred (created remotely while no player was
	 * near), generate it now that a player is actually loading it.
	 */
	public static void onSectorLoad(Universe universe, Sector sector) {
		if(!pendingSectors.contains(sectorKey(sector.pos))) {
			return;
		}
		requestGeneration(universe, sector);
	}

	private static boolean isPlayerNear(Sector sector) {
		int range = ConfigManager.getMainConfig().getInt("generation-player-range");
		if(range < 0) {
			return true; //Negative range restores legacy behavior: always generate on creation.
		}
		GameServerState state = GameServerState.instance;
		if(state == null) {
			return false;
		}
		Vector3i sp = sector.pos;
		for(PlayerState player : state.getPlayerStatesByName().values()) {
			Vector3i pp = player.getCurrentSector();
			if(pp == null) {
				continue;
			}
			if(Math.abs(pp.x - sp.x) <= range && Math.abs(pp.y - sp.y) <= range && Math.abs(pp.z - sp.z) <= range) {
				return true;
			}
		}
		return false;
	}

	private static void generateForSectorAuto(Universe universe, Sector sector) throws java.io.IOException {
		StellarSystem system = sector._getSystem();
		Galaxy galaxy = universe.getGalaxyFromSystemPos(system.getPos());
		if(galaxy == null) {
			LorefulLoot.getInstance().logWarning("Galaxy not found for sector generation event!");
			return;
		}
		Vector4f starColor = galaxy.getSunColor(system.getPos());
		generateForSector(sector, sector.getSectorType(), starColor, false);
	}

	public static void generateForSector(Sector sector, SectorInformation.SectorType sectorType, Vector4f starColor, boolean forced) {
		generateForSector(sector, sectorType, starColor, forced, null);
	}

	public static void generateForSector(Sector sector, SectorInformation.SectorType sectorType, Vector4f starColor, boolean forced, String configFilter) {
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
				if(configFilter != null && !scriptName.equalsIgnoreCase(configFilter)) continue;
				LorefulLoot.getInstance().logInfo("Loading json: " + scriptName);

				try(FileReader reader = new FileReader(scriptFile, StandardCharsets.UTF_8)) {
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
								//Match case-insensitively: the JSON convention is lowercase (e.g. "planet") while the
								//engine's SectorType.name() is uppercase (e.g. "PLANET"), so a direct contains() never matched.
								String typeName = (sectorType != null) ? sectorType.name() : null;
								boolean typeMatch = false;
								if(typeName != null) {
									for(String allowed : rule.getAllowedSectorTypes()) {
										if(typeName.equalsIgnoreCase(allowed)) {
											typeMatch = true;
											break;
										}
									}
								}
								if(!typeMatch) {
									LorefulLoot.getInstance().logDebug("Skipping rule '" + rule.getEntityName() + "' in sector " + sector.pos + ": type " + typeName + " not in " + rule.getAllowedSectorTypes());
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
					float minMass = (float) ConfigManager.getMainConfig().getDouble("combat-wreck-min-mass");
					if(entity.getTotalPhysicalMass() < minMass) {
						overheatMap.remove(entity);
						return;
					}
					int chance = ConfigManager.getMainConfig().getInt("combat-wreck-chance");
					if((int)(Math.random() * 100) >= chance) {
						overheatMap.remove(entity);
						return;
					}
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
