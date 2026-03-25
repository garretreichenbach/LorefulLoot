package videogoose.lorefulloot.data;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;

import java.io.Serializable;

public class WreckageData implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String uniqueIdentifier;
	private final String entityName;
	private final long createdAt;
	private final String createdBy;
	private final int sectorX;
	private final int sectorY;
	private final int sectorZ;

	public WreckageData(String uniqueIdentifier, String entityName, long createdAt, String createdBy, int sectorX, int sectorY, int sectorZ) {
		this.uniqueIdentifier = uniqueIdentifier;
		this.entityName = entityName;
		this.createdAt = createdAt;
		this.createdBy = createdBy;
		this.sectorX = sectorX;
		this.sectorY = sectorY;
		this.sectorZ = sectorZ;
	}

	public static WreckageData fromController(SegmentController controller, String createdBy) {
		String uniqueIdentifier = controller.getUniqueIdentifier();
		String entityName = controller.getRealName();
		long createdAt = System.currentTimeMillis();
		Vector3i sector = controller.getSector(new Vector3i());
		return new WreckageData(uniqueIdentifier, entityName, createdAt, createdBy, sector.x, sector.y, sector.z);
	}

	public String getUniqueIdentifier() {
		return uniqueIdentifier;
	}

	public String getEntityName() {
		return entityName;
	}

	public long getCreatedAt() {
		return createdAt;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public int getSectorX() {
		return sectorX;
	}

	public int getSectorY() {
		return sectorY;
	}

	public int getSectorZ() {
		return sectorZ;
	}
}
