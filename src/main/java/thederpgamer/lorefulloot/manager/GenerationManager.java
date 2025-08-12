package thederpgamer.lorefulloot.manager;

import api.listener.events.entity.SegmentControllerOverheatEvent;
import com.bulletphysics.linearmath.Transform;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.schema.game.common.controller.ManagedUsableSegmentController;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SectorInformation;
import org.schema.game.server.data.ServerConfig;
import thederpgamer.lorefulloot.LorefulLoot;
import thederpgamer.lorefulloot.data.generation.GenerationScriptLoader;
import thederpgamer.lorefulloot.lua.data.entity.EntityGenData;
import thederpgamer.lorefulloot.lua.data.entity.PirateGenData;
import thederpgamer.lorefulloot.lua.data.misc.LuaVector3f;
import thederpgamer.lorefulloot.lua.data.misc.LuaVector4f;
import thederpgamer.lorefulloot.utils.DataUtils;

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
				LuaValue script = GenerationScriptLoader.loadScript(scriptFile);
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
				LuaValue result = script.call(args);
				if(result.isfunction()) {
					LuaFunction function = result.checkfunction();
					result = function.call(args);
				}
				if(result.istable()) {
					LuaTable entities = result.checktable();
					for(int i = 1; i <= entities.length(); i++) {
						LuaValue entityData = entities.get(i);
						if(entityData.isuserdata(PirateGenData.class)) {
							PirateGenData pirateGenData = (PirateGenData) entityData.checkuserdata(PirateGenData.class);
							pirateGenData.spawnEntity(new LuaVector3f(sector.pos.x, sector.pos.y, sector.pos.z));
						} else if(entityData.isuserdata(EntityGenData.class)) {
							EntityGenData entityGenData = (EntityGenData) entityData.checkuserdata(EntityGenData.class);
							entityGenData.spawnEntity(new LuaVector3f(sector.pos.x, sector.pos.y, sector.pos.z));
						}
					}
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
				if(!entity.getRealName().startsWith("[Wreckage] ")) {
					entity.setRealName("[Wreckage] " + entity.getRealName());
					entity.setFactionId(0);
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
