package thederpgamer.lorefulloot.utils;

import api.common.GameClient;
import api.common.GameCommon;
import thederpgamer.lorefulloot.LorefulLoot;

import java.io.File;
import java.io.InputStream;
import java.util.logging.Level;

public class DataUtils {

	public static String getResourcesPath() {
		return LorefulLoot.getInstance().getSkeleton().getResourcesFolder().getPath().replace('\\', '/');
	}

	public static String getWorldDataPath() {
		String universeName = GameCommon.getUniqueContextId();
		if(!universeName.contains(":")) return getResourcesPath() + "/data/" + universeName;
		else {
			try {
				LorefulLoot.log.log(Level.WARNING,"Client " + GameClient.getClientPlayerState().getName() + " attempted to illegally access server data.");
			} catch(Exception ignored) { }
			return null;
		}
	}

	public static void copyInputStreamToFile(InputStream inputStream, File file) {
		try {
		} catch(Exception exception) {
			throw new RuntimeException(exception);
		}
	}
}
