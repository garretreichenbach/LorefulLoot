package thederpgamer.lorefulloot.data;

import org.json.JSONObject;

/**
 * Interface for classes that can be serialized to JSON.
 *
 * @author TheDerpGamer
 */
public interface JSONSerializable {

	JSONObject toJSON();
	void fromJSON(JSONObject json);
}
