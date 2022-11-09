package thederpgamer.lorefulloot;

import api.listener.events.controller.ClientInitializeEvent;
import api.mod.StarLoader;
import api.mod.StarMod;
import thederpgamer.lorefulloot.data.commands.CreateWreckCommand;
import thederpgamer.lorefulloot.data.commands.ForceGenerateCommand;
import thederpgamer.lorefulloot.data.commands.HollowShipCommand;
import thederpgamer.lorefulloot.data.commands.RemoveDeprecatedCommand;
import thederpgamer.lorefulloot.manager.ConfigManager;
import thederpgamer.lorefulloot.manager.EventManager;
import thederpgamer.lorefulloot.manager.GenerationManager;
import thederpgamer.lorefulloot.utils.DataUtils;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LorefulLoot extends StarMod {

	//Instance
	private static LorefulLoot instance;
	public LorefulLoot() {

	}
	public static LorefulLoot getInstance() {
		return instance;
	}
	public static void main(String[] args) {
	}

	//Data
	public static Logger log;

	@Override
	public void onEnable() {
		instance = this;
		ConfigManager.initialize(this);
		initLogger();
		EventManager.initialize(this);
		GenerationManager.initialize();
		registerCommands();
	}

	@Override
	public void onClientCreated(ClientInitializeEvent event) {
		super.onClientCreated(event);
		//GenerationManager.genDefaults();
	}

	private void initLogger() {
		String logFolderPath = DataUtils.getWorldDataPath() + "/logs";
		File logsFolder = new File(logFolderPath);
		if(!logsFolder.exists()) logsFolder.mkdirs();
		else {
			if(logsFolder.listFiles() != null && logsFolder.listFiles().length > 0) {
				File[] logFiles = new File[logsFolder.listFiles().length];
				int j = logFiles.length - 1;
				for(int i = 0; i < logFiles.length && j >= 0; i++) {
					if(!logsFolder.listFiles()[i].getName().endsWith(".lck")) logFiles[j] = logsFolder.listFiles()[i];
					else logsFolder.listFiles()[i].delete();
					j--;
				}

				//Trim null entries
				int nullCount = 0;
				for(File value : logFiles) {
					if(value == null) nullCount ++;
				}

				File[] trimmedLogFiles = new File[logFiles.length - nullCount];
				int l = 0;
				for(File file : logFiles) {
					if(file != null) {
						trimmedLogFiles[l] = file;
						l++;
					}
				}

				for(File logFile : trimmedLogFiles) {
					if(logFile == null) continue;
					String fileName = logFile.getName().replace(".txt", "");
					int logNumber = Integer.parseInt(fileName.substring(fileName.indexOf("log") + 3)) + 1;
					String newName = logFolderPath + "/log" + logNumber + ".txt";
					if(logNumber < ConfigManager.getMainConfig().getInt("max-world-logs") - 1) logFile.renameTo(new File(newName));
					else logFile.delete();
				}
			}
		}
		try {
			File newLogFile = new File(logFolderPath + "/log0.txt");
			if(newLogFile.exists()) newLogFile.delete();
			newLogFile.createNewFile();
			log = Logger.getLogger(newLogFile.getPath());
			FileHandler handler = new FileHandler(newLogFile.getPath());
			log.addHandler(handler);
			SimpleFormatter formatter = new SimpleFormatter();
			handler.setFormatter(formatter);
		} catch(IOException exception) {
			exception.printStackTrace();
		}
	}

	private void registerCommands() {
		StarLoader.registerCommand(new CreateWreckCommand());
		StarLoader.registerCommand(new ForceGenerateCommand());
		StarLoader.registerCommand(new RemoveDeprecatedCommand());
		StarLoader.registerCommand(new HollowShipCommand());
	}
}
