package thederpgamer.lorefulloot;

import api.common.GameCommon;
import api.common.GameServer;
import api.mod.StarLoader;
import api.mod.StarMod;
import thederpgamer.lorefulloot.data.commands.CreateWreckCommand;
import thederpgamer.lorefulloot.data.commands.ForceGenerateCommand;
import thederpgamer.lorefulloot.manager.ConfigManager;
import thederpgamer.lorefulloot.manager.EventManager;
import thederpgamer.lorefulloot.manager.GenerationManager;

import java.util.Arrays;

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
		registerCommands();
	}

	@Override
	public void logInfo(String message) {
		super.logInfo(message);
		System.out.println("[INFO] " + message);
	}

	@Override
	public void logWarning(String message) {
		super.logWarning(message);
		System.err.println("[WARNING] " + message);
	}

	@Override
	public void logException(String message, Exception exception) {
		super.logException(message, exception);
		System.err.println("[EXCEPTION] " + message + "\n" + exception.getMessage() + "\n" + Arrays.toString(exception.getStackTrace()));
	}

	@Override
	public void logFatal(String message, Exception exception) {
		logException(message, exception);
		if(GameCommon.getGameState().isOnServer()) {
			GameServer.getServerState().addCountdownMessage(10, "Server will perform an emergency shutdown due to a fatal error: " + message);
		}
	}

	private void registerCommands() {
		StarLoader.registerCommand(new CreateWreckCommand());
		StarLoader.registerCommand(new ForceGenerateCommand());
	}
}
