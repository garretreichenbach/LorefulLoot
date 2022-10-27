package thederpgamer.lorefulloot.manager;

import api.mod.config.FileConfiguration;
import thederpgamer.lorefulloot.LorefulLoot;

public class ConfigManager {

	private static FileConfiguration mainConfig;
	private static final String[] defaultMainConfig = {
			"debug-mode: false",
			"max-world-logs: 5",
			"generate-shipwrecks-from-combat: true"
	};

	public static void initialize(LorefulLoot instance) {
		mainConfig = instance.getConfig("config");
		mainConfig.saveDefault(defaultMainConfig);
	}

	public static FileConfiguration getMainConfig() {
		return mainConfig;
	}
}
