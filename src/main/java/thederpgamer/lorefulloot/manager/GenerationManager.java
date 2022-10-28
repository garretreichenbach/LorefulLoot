package thederpgamer.lorefulloot.manager;

import com.bulletphysics.linearmath.Transform;
import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.rails.RailRelation;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
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
import thederpgamer.lorefulloot.data.ItemStack;
import thederpgamer.lorefulloot.data.generation.EntitySpawn;
import thederpgamer.lorefulloot.data.generation.GenerationConfig;
import thederpgamer.lorefulloot.data.generation.SpawnCondition;
import thederpgamer.lorefulloot.data.generation.SpawnGroup;
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
			"B110-11", "B130-7", "B150-18", "B190-21", "C120-12", "C140-9"
	};
	private static final String[] defaultConfigs = {
			"/config/generation/shipwrecks.json"
	};

	private static Short[] deprecatedElements;

	public static void genDefaults() {
		GenerationConfig config = new GenerationConfig();
		config.setName("shipwrecks");
		SpawnGroup[] spawnGroups = new SpawnGroup[1];
		EntitySpawn[] asteroidSpawns = new EntitySpawn[defaultBps.length];
		for(int i = 0; i < defaultBps.length; i++) asteroidSpawns[i] = new EntitySpawn(defaultBps[i] + " [Derelict]", defaultBps[i], 0.0025f, generateRandomItemStacks(5, 10));
		spawnGroups[0] = new SpawnGroup("asteroids", new SpawnCondition[]{new SpawnCondition("sector-type", SectorInformation.SectorType.ASTEROID.name())}, asteroidSpawns);
		config.setSpawnGroups(spawnGroups);
		try {
			File file = new File("C:/Users/garre/OneDrive - Arizona State University/Documents/GitHub/LorefulLoot/src/main/resources/config/generation/shipwrecks.json");
			if(file.exists()) file.delete();
			file.createNewFile();
			FileUtils.writeStringToFile(file, new Gson().toJson(config));
		} catch(Exception exception) {
			exception.printStackTrace();
			LorefulLoot.log.log(Level.WARNING, "Failed to generate default config file!", exception);
		}
		configMap.put(config.getName(), config);
	}

	private static ItemStack[] generateRandomItemStacks(int min, int max) {
		Random random = new Random();
		int amount = random.nextInt(max - min) + min;
		ItemStack[] itemStacks = new ItemStack[amount];
		for(int i = 0; i < amount; i++) itemStacks[i] = new ItemStack(getRandomItem(), random.nextInt(1000), random.nextFloat());
		return itemStacks;
	}

	private static short getRandomItem() {
		try {
			Random random = new Random();
			ElementInformation[] items = ElementKeyMap.getInfoArray();
			ElementInformation item = items[random.nextInt(items.length)];
			if(item.isDeprecated() || ! item.isShoppable()) return getRandomItem();
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
		File generationFolder = new File(DataUtils.getWorldDataPath(), "generation");
		if(! generationFolder.exists()) generationFolder.mkdirs();
		return generationFolder;
	}

	public static void generateForSector(Sector sector, boolean force) {
		try {
			switch(sector.getSectorType()) {
				case ASTEROID:
					for(GenerationConfig config : configMap.values()) {
						for(SpawnGroup spawnGroup : config.getSpawnGroups()) {
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
					break;
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
