package videogoose.lorefulloot;

import api.mod.StarLoader;
import api.mod.StarMod;
import videogoose.lorefulloot.data.commands.CreateWreckCommand;
import videogoose.lorefulloot.data.commands.ForceGenerateCommand;
import videogoose.lorefulloot.manager.ConfigManager;
import videogoose.lorefulloot.manager.EventManager;
import videogoose.lorefulloot.manager.GenerationManager;
import videogoose.lorefulloot.manager.WreckageManager;

public class LorefulLoot extends StarMod {

	private static LorefulLoot instance;

	public LorefulLoot() {
		instance = this;
	}

	public static LorefulLoot getInstance() {
		return instance;
	}

	public static void main(String[] args) {
	}

	@Override
	public void onEnable() {
		instance = this;
		ConfigManager.initialize(this);
		EventManager.initialize(this);
		GenerationManager.initialize();
		WreckageManager.initialize();
		registerCommands();
	}

	private void registerCommands() {
		StarLoader.registerCommand(new CreateWreckCommand());
		StarLoader.registerCommand(new ForceGenerateCommand());
	}

	public void logDebug(String message) {
		if(ConfigManager.getMainConfig().getBoolean("debug-mode")) {
			logMessage("[DEBUG]: " + message);
		}
	}
}
