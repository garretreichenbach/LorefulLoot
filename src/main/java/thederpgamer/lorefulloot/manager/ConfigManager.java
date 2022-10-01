package thederpgamer.lorefulloot.manager;

import api.mod.config.FileConfiguration;
import thederpgamer.lorefulloot.LorefulLoot;

public class ConfigManager {

	private static FileConfiguration mainConfig;
	private static FileConfiguration lootConfig;
	private static final String[] defaultMainConfig = {
			"debug-mode: false",
			"max-world-logs: 5"
	};
	private static final String[] defaultLootConfig = {
		//TODO: Add default loot config
	};

	public static void initialize(LorefulLoot instance) {
		mainConfig = instance.getConfig("config");
		mainConfig.saveDefault(defaultMainConfig);

		lootConfig = instance.getConfig("loot");
		lootConfig.saveDefault(defaultLootConfig);
	}

	public static FileConfiguration getMainConfig() {
		return mainConfig;
	}

	public static FileConfiguration getLootConfig() {
		return lootConfig;
	}
}
