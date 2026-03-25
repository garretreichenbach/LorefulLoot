package videogoose.lorefulloot.manager;

import org.schema.game.common.controller.SegmentController;
import videogoose.lorefulloot.LorefulLoot;
import videogoose.lorefulloot.data.WreckageData;
import videogoose.lorefulloot.utils.DataUtils;

import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class WreckageManager {

	private static final String DATA_FILE_NAME = "wreckages.smdat";
	private static final HashMap<String, WreckageData> wreckages = new HashMap<>();

	public static void initialize() {
		load();
	}

	public static synchronized void addWreckage(WreckageData wreckageData) {
		if(wreckageData == null || wreckageData.getUniqueIdentifier() == null || wreckageData.getUniqueIdentifier().isEmpty()) {
			return;
		}
		wreckages.put(wreckageData.getUniqueIdentifier(), wreckageData);
		save();
	}

	public static void addWreckage(SegmentController controller, String createdBy) {
		if(controller == null) {
			return;
		}
		addWreckage(WreckageData.fromController(controller, createdBy));
	}

	public static synchronized boolean canSalvage(String uniqueIdentifier) {
		if(uniqueIdentifier == null || uniqueIdentifier.isEmpty()) {
			return false;
		}
		return wreckages.containsKey(uniqueIdentifier);
	}

	public static synchronized Map<String, WreckageData> getWreckages() {
		return Collections.unmodifiableMap(wreckages);
	}

	private static synchronized void load() {
		wreckages.clear();
		File file = getDataFile();
		if(file == null || !file.exists()) return;
		try(ObjectInputStream in = new ObjectInputStream(Files.newInputStream(file.toPath()))) {
			Object data = in.readObject();
			if(data instanceof HashMap<?, ?>) {
				HashMap<?, ?> map = (HashMap<?, ?>) data;
				for(Map.Entry<?, ?> entry : map.entrySet()) {
					if(entry.getKey() instanceof String && entry.getValue() instanceof WreckageData) {
						wreckages.put((String) entry.getKey(), (WreckageData) entry.getValue());
					}
				}
			}
		} catch(Exception exception) {
			LorefulLoot.getInstance().logException("Failed to load wreckage data!", exception);
		}
	}

	private static synchronized void save() {
		File file = getDataFile();
		if(file == null) {
			return;
		}
		try {
			File parent = file.getParentFile();
			if(parent != null && !parent.exists()) {
				parent.mkdirs();
			}
			try(ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(file.toPath()))) {
				out.writeObject(wreckages);
			}
		} catch(Exception exception) {
			LorefulLoot.getInstance().logException("Failed to save wreckage data!", exception);
		}
	}

	private static File getDataFile() {
		String worldDataPath = DataUtils.getWorldDataPath();
		if(worldDataPath == null) {
			return null;
		}
		return new File(worldDataPath, DATA_FILE_NAME);
	}
}
