package thederpgamer.lorefulloot;

import api.mod.StarLoader;
import api.mod.StarMod;
import thederpgamer.lorefulloot.data.commands.CreateWreckCommand;
import thederpgamer.lorefulloot.data.commands.ForceGenerateCommand;
import thederpgamer.lorefulloot.manager.ConfigManager;
import thederpgamer.lorefulloot.manager.EventManager;
import thederpgamer.lorefulloot.manager.GenerationManager;

public class LorefulLoot extends StarMod {

	private static LorefulLoot instance;
	public LorefulLoot() {}
	public static LorefulLoot getInstance() {
		return instance;
	}
	public static void main(String[] args) {}

	@Override
	public void onEnable() {
		instance = this;
		ConfigManager.initialize(this);
		EventManager.initialize(this);
		GenerationManager.initialize();
		registerCommands();
	}

	@Override
	public void logException(String message, Exception exception) {
		super.logException(message, exception);
		exception.printStackTrace();
	}

	private void registerCommands() {
		StarLoader.registerCommand(new CreateWreckCommand());
		StarLoader.registerCommand(new ForceGenerateCommand());
	}
}
