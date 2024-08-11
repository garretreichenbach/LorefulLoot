package thederpgamer.lorefulloot.utils;

import api.utils.game.inventory.ItemStack;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import org.schema.common.util.linAlg.Vector3b;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.elements.InventoryMap;
import org.schema.game.common.controller.elements.ModuleExplosion;
import org.schema.game.common.controller.rails.RailRelation;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.world.Segment;
import thederpgamer.lorefulloot.LorefulLoot;

import javax.vecmath.Vector3f;

/**
 * Utility class for miscellaneous functions.
 *
 * @author TheDerpGamer
 */
public class MiscUtils {
	/**
	 * Wrecks an entity based off a damage intensity.
	 *
	 * @param entity Entity to wreck.
	 *               <p>Note: Make sure you save a copy of your ship before using this function!</p>
	 */
	public static void wreckShip(final Ship entity) {
		(new Thread() {
			@Override
			public void run() {
				try {
					for(int j = 0; j < 5; j++) {
						entity.getManagerContainer().getShieldAddOn().setShields(0);
						entity.getManagerContainer().getShieldAddOn().setShieldCapacityHP(0);
						entity.getManagerContainer().getShieldAddOn().setRegenEnabled(false);
						int explosionCap = 15;
						float radius = 10;
						LongArrayList l = new LongArrayList(explosionCap);
						for(int i = 0; i < explosionCap; i++) l.add(getRandomIndex(entity, 0));
						long index = getRandomIndex(entity, 0);
						ModuleExplosion expl = new ModuleExplosion(l, 5, (int) radius, 50000000, index, ModuleExplosion.ExplosionCause.STABILITY, entity.getBoundingBox());
						expl.setChain(true);
						((ManagedSegmentController<?>) entity).getManagerContainer().addModuleExplosions(expl);
					}

                    if(!entity.checkCore(entity.getSegmentBuffer().getPointUnsave(Ship.core))) {
						entity.setMarkedForDeletePermanentIncludingDocks(true);
						entity.setMarkedForDeleteVolatileIncludingDocks(true);
                    }
                } catch(Exception exception) {
					LorefulLoot.getInstance().logException("Failed to wreck ship: " + entity.getName(), exception);
					entity.setMarkedForDeletePermanentIncludingDocks(true);
					entity.setMarkedForDeleteVolatileIncludingDocks(true);
				}
			}
		}).start();
	}

	private static long getRandomIndex(SegmentController entity, int attempts) {
		Vector3f min = new Vector3f(entity.getMinPos().x, entity.getMinPos().y, entity.getMinPos().z);
		Vector3f max = new Vector3f(entity.getMaxPos().x, entity.getMaxPos().y, entity.getMaxPos().z);
		Vector3f size = new Vector3f();
		size.sub(max, min);
		Vector3f randomPos = new Vector3f();
		randomPos.x = min.x + (float) Math.random() * size.x;
		randomPos.y = min.y + (float) Math.random() * size.y;
		randomPos.z = min.z + (float) Math.random() * size.z;
		Segment segment = entity.getSegmentBuffer().get(new Vector3i(randomPos));
		if(attempts < 30) {
			if(segment == null) return getRandomIndex(entity, attempts + 1);
			else {
				Vector3b segmentMin = segment.getSegmentData().getMin();
				Vector3b segmentMax = segment.getSegmentData().getMax();
				Vector3b segmentSize = new Vector3b(segmentMax);
				segmentSize.sub(segmentMin);
				Vector3b randomSegmentPos = new Vector3b();
				randomSegmentPos.x = (byte) (segmentMin.x + (int) (Math.random() * segmentSize.x));
				randomSegmentPos.y = (byte) (segmentMin.y + (int) (Math.random() * segmentSize.y));
				randomSegmentPos.z = (byte) (segmentMin.z + (int) (Math.random() * segmentSize.z));
				return segment.getAbsoluteIndex(randomSegmentPos.x, randomSegmentPos.y, randomSegmentPos.z);
			}
		}
		return 0;
	}

	public static void fillInventories(Ship controller, ItemStack[] itemStacks) {
		controller.setScrap(true);
		controller.setMinable(true);
		InventoryMap map = controller.getInventories();
		for(int i = 0; i < map.inventoriesList.size(); i++) {
			for(ItemStack itemStack : itemStacks) {
				map.inventoriesList.get(i).putNextFreeSlot(itemStack.getId(), itemStack.getAmount(), -1);
			}
			map.inventoriesList.get(i).sendAll();
		}
		for(RailRelation relation : controller.railController.next) {
			fillInventories((Ship) relation.docked.getSegmentController(), itemStacks);
		}
	}
}
