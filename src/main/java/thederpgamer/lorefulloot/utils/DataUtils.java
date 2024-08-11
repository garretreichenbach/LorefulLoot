package thederpgamer.lorefulloot.utils;

import api.common.GameCommon;
import org.apache.commons.io.FileUtils;
import thederpgamer.lorefulloot.LorefulLoot;

import java.io.File;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class DataUtils {

	public static String getResourcesPath() {
		return LorefulLoot.getInstance().getSkeleton().getResourcesFolder().getPath().replace('\\', '/');
	}

	public static String getWorldDataPath() {
		String universeName = GameCommon.getUniqueContextId();
		if(!universeName.contains(":")) return getResourcesPath() + "/data/" + universeName;
		else return null;
	}

	public static void unzip(File src, File destination) {
		try {
			ZipInputStream zipInputStream = new ZipInputStream(src.toURI().toURL().openStream());
			ZipEntry zipEntry;
			while((zipEntry = zipInputStream.getNextEntry()) != null) {
				File file = new File(destination, zipEntry.getName());
				if(zipEntry.isDirectory()) file.mkdirs();
				else {
					file.getParentFile().mkdirs();
					FileUtils.copyInputStreamToFile(zipInputStream, file);
				}
				zipInputStream.closeEntry();
			}
			zipInputStream.close();
		} catch(Exception exception) {
			LorefulLoot.getInstance().logException("Failed to unzip file: " + src.getPath(), exception);
		}
	}
}
