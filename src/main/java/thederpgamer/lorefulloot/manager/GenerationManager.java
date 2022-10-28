package thederpgamer.lorefulloot.manager;

import com.bulletphysics.linearmath.Transform;
import com.google.gson.Gson;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.apache.commons.io.FileUtils;
import org.schema.game.common.controller.ManagedUsableSegmentController;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.inventory.Inventory;
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
import thederpgamer.lorefulloot.utils.DataUtils;

import javax.vecmath.Vector3f;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

/**
 * [Description]
 *
 * @author TheDerpGamer (MrGoose#0027)
 */
public class GenerationManager {
	private static final String[] defaultConfigs = {
			"/config/generation/shipwrecks.json"
	};
	public static final HashMap<String, GenerationConfig> configMap = new HashMap<>();

	public static void main(String[] args) {
		GenerationConfig config = new GenerationConfig();
		config.setName("shipwrecks");
		config.setSpawnGroups(new SpawnGroup[] {
				new SpawnGroup("shipwreck-01", new SpawnCondition[] {
						new SpawnCondition("sector-type", SectorInformation.SectorType.ASTEROID.name())
				}, new EntitySpawn[] {
						new EntitySpawn("shipwreck-01", "shipwreck-01", 0.035f, new ItemStack[] {
								new ItemStack(ElementKeyMap.REACTOR_MAIN, 194, 0.45f),
								new ItemStack(ElementKeyMap.REACTOR_STABILIZER, 144, 0.4f),
								new ItemStack(ElementKeyMap.CORE_ID, 35, 0.37f),
						})
				})
		});
		try {
			FileUtils.writeStringToFile(new File("C:/Users/garre/OneDrive - Arizona State University/Documents/GitHub/LorefulLoot/src/main/resources/config/generation/shipwrecks.json"), new Gson().toJson(config));
		} catch(Exception exception) {
			exception.printStackTrace();
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
			throw new RuntimeException(exception);
		}
	}

	private static File getGenerationFolder() {
		File generationFolder = new File(DataUtils.getWorldDataPath(), "generation");
		if(!generationFolder.exists()) generationFolder.mkdirs();
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
			LorefulLoot.log.log(java.util.logging.Level.WARNING, "Failed to generate for sector " + sector.pos + "!");
		}
	}

	private static void createEntity(EntitySpawn entitySpawn, Sector sector) {
		SegmentControllerOutline<?> scOutline = null;
		try {
			scOutline = BluePrintController.active.loadBluePrint(
					GameServerState.instance,
					entitySpawn.getBpName(),
					entitySpawn.getLoreName(),
					getRandomTransformInSector(sector),
					-1,
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
			ObjectArrayList<Inventory> inventories = ((ManagedUsableSegmentController<?>) segmentController).getInventories().inventoriesList;
			for(Inventory inventory : inventories) {
				try {
					for(ItemStack item : entitySpawn.getItems()) {
						Random random = new Random();
						if(random.nextFloat() <= item.getWeight()) item.addTo(inventory);
					}
				} catch(Exception exception) {
					LorefulLoot.log.log(java.util.logging.Level.WARNING, "Failed to add items to entity " + entitySpawn.getName() + " in sector " + sector.pos + "!");
				}
			}
		}
	}

	private static Transform getRandomTransformInSector(Sector sector) {
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
