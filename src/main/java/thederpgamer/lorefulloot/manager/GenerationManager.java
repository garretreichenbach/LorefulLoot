package thederpgamer.lorefulloot.manager;

import com.bulletphysics.linearmath.Transform;
import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.schema.game.common.controller.FloatingRock;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.SpaceStation;
import org.schema.game.common.controller.rails.RailRelation;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.controller.BluePrintController;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.ServerConfig;
import org.schema.game.server.data.blueprint.ChildStats;
import org.schema.game.server.data.blueprint.SegmentControllerOutline;
import org.schema.game.server.data.blueprint.SegmentControllerSpawnCallbackDirect;
import thederpgamer.lorefulloot.LorefulLoot;
import thederpgamer.lorefulloot.data.ItemStack;
import thederpgamer.lorefulloot.data.generation.*;
import thederpgamer.lorefulloot.data.other.EntitySanitizerExecutor;
import thederpgamer.lorefulloot.utils.DataUtils;
import thederpgamer.lorefulloot.utils.MiscUtils;

import javax.vecmath.Vector3f;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

/**
 * [Description]
 *
 * @author TheDerpGamer (MrGoose#0027)
 */
public class GenerationManager {
	public static final HashMap<String, GenerationConfig> configMap = new HashMap<>();
	private static final String[] defaultBps = {
			"Small-Shipwreck-01", "Small-Shipwreck-02", "Small-Shipwreck-03", "Small-Shipwreck-04", "Small-Shipwreck-05", "Medium-Shipwreck-01", "Medium-Shipwreck-02", "Medium-Shipwreck-03", "Medium-Shipwreck-04", "Medium-Shipwreck-05", "Large-Shipwreck-01"
	};
	private static final String[] defaultConfigs = {
			"/config/generation/shipwrecks.json"
	};

	public static void genDefaults() {
		GenerationConfig config = new GenerationConfig();
		config.setName("shipwrecks");
		SpawnGroup[] spawnGroups = new SpawnGroup[1];
		EntitySpawn[] spawns = new EntitySpawn[defaultBps.length];
		for(int i = 0; i < defaultBps.length; i++) spawns[i] = new EntitySpawn(defaultBps[i] + " [Derelict]", defaultBps[i], 0.015f, null);
		spawnGroups[0] = new SpawnGroup("spawns", new SpawnCondition[] {}, spawns);
		config.setSpawnGroups(spawnGroups);
		try {
			File file = new File("C:/Users/garre/OneDrive - Arizona State University/Documents/GitHub/LorefulLoot/src/main/resources/config/generation/shipwrecks.json");
			if(!file.exists()) file.createNewFile();
			FileUtils.writeStringToFile(file, new Gson().toJson(config));
		} catch(Exception exception) {
			exception.printStackTrace();
			LorefulLoot.log.log(Level.WARNING, "Failed to generate default config file!", exception);
		}
		configMap.put(config.getName(), config);
	}

	public static ItemStack[] generateRandomItemStacks(int min, int max) {
		Random random = new Random();
		int amount = random.nextInt(max - min) + min;
		ItemStack[] itemStacks = new ItemStack[amount];
		for(int i = 0; i < amount; i++) {
			short itemId = getRandomItem();
			int stackSize = new Random().nextInt(15000) + 1;
			itemStacks[i] = new ItemStack(itemId, stackSize);
		}
		return itemStacks;
	}

	public static EntityLore generateRandomLore(Sector sector) {
		try {
			InputStream fileInputStream = LorefulLoot.class.getResourceAsStream("/config/lore.json");
			File output = new File(DataUtils.getWorldDataPath() + "/lore.json");
			if(!output.exists()) {
				output.createNewFile();
				assert fileInputStream != null;
				FileUtils.copyInputStreamToFile(fileInputStream, output);
			}
		} catch(Exception exception) {
			exception.printStackTrace();
			LorefulLoot.log.log(Level.WARNING, "Failed to generate default lore file!", exception);
		}

		File file = new File(DataUtils.getWorldDataPath() + "/lore.json");
		try {
			String json = FileUtils.readFileToString(file);
			LoreCategory loreCategory = new Gson().fromJson(json, LoreCategory.class);
			for(EntityLore entityLore : loreCategory.getValues()) {
				if(entityLore.getConditions().length == 0) {
					if(new Random().nextFloat() < entityLore.getWeight()) return entityLore;
				} else {
					for(SpawnCondition spawnCondition : entityLore.getConditions()) {
						switch(spawnCondition.getName()) {
							case "sector-type":
								if(sector.getSectorType().name().equals(spawnCondition.getValue()) && (new Random().nextFloat()) < entityLore.getWeight()) {
									return entityLore;
								}
								break;
							case "sector-contains":
								String value = spawnCondition.getValue();
								String type = value.split(":")[0];
								String factionID = value.split("\\[")[1].replaceAll("\\[", "").replaceAll("]", "");
								int factionId = Integer.parseInt(factionID);
								switch(type) {
									case "entity":
										String entityType = value.split(":")[1];
										switch(entityType) {
											case "station":
												for(SimpleTransformableSendableObject<?> station : sector.getEntities()) {
													if(station instanceof SpaceStation && station.getFactionId() == factionId) return entityLore;
												}
											case "ship":
												for(SimpleTransformableSendableObject<?> ship : sector.getEntities()) {
													if(ship instanceof Ship && ship.getFactionId() == factionId) return entityLore;
												}
											case "asteroid":
												for(SimpleTransformableSendableObject<?> asteroid : sector.getEntities()) {
													if(asteroid instanceof FloatingRock && asteroid.getFactionId() == factionId) return entityLore;
												}
										}
										break;
								}
								break;
						}
					}
				}
			}
		} catch(Exception exception) {
			exception.printStackTrace();
			LorefulLoot.log.log(Level.WARNING, "Failed to load lore file!", exception);
		}
		return null;
	}


	private static short getRandomItem() {
		try {
			Random random = new Random();
			ElementInformation[] items = ElementKeyMap.getInfoArray();
			ElementInformation item = items[random.nextInt(items.length)];
			if(item.isDeprecated() || ! item.isShoppable() || ! item.isInRecipe()) return getRandomItem();
			else return item.getId();
		} catch(Exception exception) {
			exception.printStackTrace();
			return 1;
		}
	}

	public static void initialize() {
		File generationFolder = getGenerationFolder();
		if(generationFolder.listFiles() == null || generationFolder.listFiles().length == 0) {
			loadDefaultConfigs(generationFolder);
			return;
		}
		try {
			for(File file : Objects.requireNonNull(generationFolder.listFiles())) {
				if(file.getName().endsWith(".json")) {
					GenerationConfig config = new Gson().fromJson(FileUtils.readFileToString(file), GenerationConfig.class);
					configMap.put(file.getName().replace(".json", ""), config);
				}
			}
		} catch(Exception exception) {
			LorefulLoot.log.log(java.util.logging.Level.WARNING, "Failed to load generation configs! Loading default configs...");
			loadDefaultConfigs(generationFolder);
		}
		LorefulLoot.log.log(java.util.logging.Level.INFO, "GenerationManager initialized.");
	}

	private static void loadDefaultConfigs(File generationFolder) {
		try {
			for(String path : defaultConfigs) {
				InputStream defaultConfigStream = LorefulLoot.class.getResourceAsStream(path);
				File defaultConfigFile = new File(generationFolder, path.substring(path.lastIndexOf("/") + 1));
				assert defaultConfigStream != null;
				FileUtils.copyInputStreamToFile(defaultConfigStream, defaultConfigFile);
				configMap.put(defaultConfigFile.getName().replace(".json", ""), new Gson().fromJson(FileUtils.readFileToString(defaultConfigFile), GenerationConfig.class));
			}
		} catch(Exception exception) {
			exception.printStackTrace();
		}
	}

	private static File getGenerationFolder() {
		File generationFolder = new File(DataUtils.getWorldDataPath(), "config/generation");
		if(! generationFolder.exists()) generationFolder.mkdirs();
		return generationFolder;
	}

	public static void generateForSector(Sector sector, boolean force) {
		try {
			for(GenerationConfig config : configMap.values()) {
				for(SpawnGroup spawnGroup : config.getSpawnGroups()) {
					if(spawnGroup.getConditions().length == 0) {
						for(EntitySpawn entitySpawn : spawnGroup.getSpawns()) {
							Random random = new Random();
							if(random.nextFloat() <= entitySpawn.getWeight() || force) createEntity(entitySpawn, sector);
						}
					} else {
						for(SpawnCondition spawnCondition : spawnGroup.getConditions()) {
							switch(spawnCondition.getName()) {
								case "sector-type":
									if(sector.getSectorType().name().equals(spawnCondition.getValue())) {
										for(EntitySpawn entitySpawn : spawnGroup.getSpawns()) {
											Random random = new Random();
											if(random.nextFloat() <= entitySpawn.getWeight() || force) createEntity(entitySpawn, sector);
										}
									}
									break;
							}
						}
					}
				}
			}
		} catch(Exception exception) {
			exception.printStackTrace();
			LorefulLoot.log.log(java.util.logging.Level.WARNING, "Failed to generate for sector " + sector.pos + "!");
		}
	}

	private static void createEntity(final EntitySpawn entitySpawn, final Sector sector) {
		SegmentControllerOutline<?> scOutline = null;
		try {
			scOutline = BluePrintController.active.loadBluePrint(
					GameServerState.instance,
					entitySpawn.getBpName(),
					entitySpawn.getLoreName(),
					getRandomTransformInSector(),
					- 1,
					entitySpawn.getFactionId(),
					sector.pos,
					entitySpawn.getFactionName(),
					PlayerState.buffer,
					null,
					false,
					new ChildStats(false)
			);
		} catch(Exception exception) {
			LorefulLoot.log.log(java.util.logging.Level.WARNING, "Failed to create entity " + entitySpawn.getName() + " in sector " + sector.pos + "!");
		}

		SegmentController segmentController = null;
		if(scOutline != null) {
			try {
				segmentController = scOutline.spawn(
						sector.pos,
						false,
						new ChildStats(false),
						new SegmentControllerSpawnCallbackDirect(GameServerState.instance, sector.pos) {
							@Override
							public void onNoDocker() { //in vanilla used to write a debug line.
							}
						});
			} catch(Exception exception) {
				LorefulLoot.log.log(java.util.logging.Level.WARNING, "Failed to spawn entity " + entitySpawn.getName() + " in sector " + sector.pos + "!");
			}
		}

		if(segmentController != null) {
			segmentController.getSegmentBuffer().restructBB();
			MiscUtils.wreckShip(segmentController, entitySpawn);
		}
	}

	public static void sanitizeEntity(final SegmentController entity, PlayerState player) throws ExecutionException, InterruptedException {
		EntitySanitizerExecutor.compute(entity, player);
		for(RailRelation relation : entity.railController.next) sanitizeEntity(relation.docked.getSegmentController(), player);
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
}
