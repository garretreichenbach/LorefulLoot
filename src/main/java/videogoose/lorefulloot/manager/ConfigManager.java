package videogoose.lorefulloot.manager;

import api.mod.config.FileConfiguration;
import videogoose.lorefulloot.LorefulLoot;

public class ConfigManager {

	private static FileConfiguration mainConfig;
	private static final String[] defaultMainConfig = {
			"debug-mode: false",
			"max-world-logs: 5",
			"generate-shipwrecks-from-combat: true",
			"combat-wreck-chance: 25",
			"combat-wreck-min-mass: 1000"
	};

	public static void initialize(LorefulLoot instance) {
		mainConfig = instance.getConfig("config");
		mainConfig.saveDefault(defaultMainConfig);
	}

	public static FileConfiguration getMainConfig() {
		return mainConfig;
	}
}
