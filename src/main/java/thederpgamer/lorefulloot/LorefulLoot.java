package thederpgamer.lorefulloot;

import api.listener.events.controller.ClientInitializeEvent;
import api.mod.StarLoader;
import api.mod.StarMod;
import org.apache.commons.io.IOUtils;
import thederpgamer.lorefulloot.data.commands.CreateWreckCommand;
import thederpgamer.lorefulloot.data.commands.ForceGenerateCommand;
import thederpgamer.lorefulloot.manager.ConfigManager;
import thederpgamer.lorefulloot.manager.EventManager;
import thederpgamer.lorefulloot.manager.GenerationManager;
import thederpgamer.lorefulloot.utils.DataUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
	private final String[] overwriteClasses = {
		"Ship" //Todo: Remove this next release
	};

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
//		GenerationManager.genDefaults();
	}

	@Override
	public byte[] onClassTransform(String className, byte[] byteCode) {
		for(String name : overwriteClasses) if(className.endsWith(name)) return overwriteClass(className, byteCode);
		return super.onClassTransform(className, byteCode);
	}

	private void initLogger() {
		String logFolderPath = DataUtils.getWorldDataPath() + "/logs";
		File logsFolder = new File(logFolderPath);
		if(!logsFolder.exists()) logsFolder.mkdirs();
		else {
			if(logsFolder.listFiles() != null && Objects.requireNonNull(logsFolder.listFiles()).length > 0) {
				File[] logFiles = new File[Objects.requireNonNull(logsFolder.listFiles()).length];
				int j = logFiles.length - 1;
				for(int i = 0; i < logFiles.length && j >= 0; i++) {
					try {
						if(!logFiles[i].getName().endsWith(".lck")) logFiles[j] = logFiles[i];
						else logFiles[i].delete();
						j--;
					} catch(Exception ignored) { }
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
	}

	private byte[] overwriteClass(String className, byte[] byteCode) {
		byte[] bytes = null;
		try {
			ZipInputStream file = new ZipInputStream(Files.newInputStream(getSkeleton().getJarFile().toPath()));
			while(true) {
				ZipEntry nextEntry = file.getNextEntry();
				if(nextEntry == null) break;
				if(nextEntry.getName().endsWith(className + ".class")) bytes = IOUtils.toByteArray(file);
			}
			file.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
		if(bytes != null) return bytes;
		else return byteCode;
	}
}
