package thederpgamer.lorefulloot.utils;

import api.utils.StarRunnable;
import com.bulletphysics.linearmath.Transform;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.ManagedUsableSegmentController;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.controller.damage.effects.InterEffectHandler;
import org.schema.game.common.controller.damage.effects.InterEffectSet;
import org.schema.game.common.controller.damage.effects.MetaWeaponEffectInterface;
import org.schema.game.common.controller.elements.ModuleExplosion;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.player.AbstractOwnerState;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.network.StateInterface;
import thederpgamer.lorefulloot.LorefulLoot;
import thederpgamer.lorefulloot.data.ItemStack;
import thederpgamer.lorefulloot.data.generation.EntitySpawn;

import javax.vecmath.Vector3f;
import java.util.Random;

/**
 * [Description]
 *
 * @author TheDerpGamer (MrGoose#0027)
 */
public class MiscUtils {

	/**
	 * Wrecks an entity based off a damage intensity.
	 * @param entity Entity to wreck.
	 * <p>Note: Make sure you save a copy of your ship before using this function!</p>
	 */
	public static void wreckShip(final SegmentController entity, final EntitySpawn entitySpawn) {
		(new Thread() {
			@Override
			public void run() {
				try {
					while(!entity.isFullyLoadedWithDock()) {
						Thread.sleep(100);
					}
				} catch(Exception exception) {
					exception.printStackTrace();
				}

				for(int j = 0; j < 5; j ++) {
					((Ship) entity).getManagerContainer().getShieldAddOn().setShields(0);
					((Ship) entity).getManagerContainer().getShieldAddOn().setShieldCapacityHP(0);
					((Ship) entity).getManagerContainer().getShieldAddOn().setRegenEnabled(false);

					Vector3f min = entity.getMinPos().toVector3f();
					Vector3f max = entity.getMaxPos().toVector3f();
					Vector3f size = new Vector3f();
					size.sub(max, min);
					int explosionCap = 15;
					float radius = Math.min(Math.max(size.length(), 5), 12);
					LongArrayList l = new LongArrayList(explosionCap);
					for(int i = 0; i < explosionCap; i++) {
						l.add(getRandomIndex(entity, 0));
					}
					ModuleExplosion expl =
							new ModuleExplosion(l,
									10,
									(int) radius,
									50000000,
									getRandomIndex(entity, 0),
									ModuleExplosion.ExplosionCause.INTEGRITY,
									entity.getBoundingBox());

					expl.setChain(true);
					((ManagedSegmentController<?>) entity).getManagerContainer().addModuleExplosions(expl);
				}
			}
		}).start();

		new StarRunnable() {
			@Override
			public void run() {
				if(entitySpawn != null) {
					ObjectArrayList<Inventory> inventories = ((ManagedUsableSegmentController<?>) entity).getInventories().inventoriesList;
					for(Inventory inventory : inventories) {
						try {
							for(ItemStack item : entitySpawn.getItems()) {
								Random random = new Random();
								if(random.nextFloat() <= item.getWeight()) item.addTo(inventory);
							}
						} catch(Exception exception) {
							LorefulLoot.log.log(java.util.logging.Level.WARNING, "Failed to add items to entity " + entitySpawn.getName() + " in sector " + entity.getSector(new Vector3i()) + "!");
						}
					}
					LorefulLoot.log.log(java.util.logging.Level.INFO, "Spawned entity " + entitySpawn.getName() + " in sector " + entity.getSector(new Vector3i()) + "!");
				}
			}
		}.runLater(LorefulLoot.getInstance(), 30);
	}

	private static long getRandomIndex(SegmentController entity, int attempts) {
		Vector3f min = entity.getBoundingBox().min;
		Vector3f max = entity.getBoundingBox().max;
		Vector3f size = new Vector3f();
		size.sub(max, min);
		Vector3f randomPos = new Vector3f();
		randomPos.x = min.x + (float) Math.random() * size.x;
		randomPos.y = min.y + (float) Math.random() * size.y;
		randomPos.z = min.z + (float) Math.random() * size.z;
		SegmentPiece segmentPiece = entity.getSegmentBuffer().getPointUnsave(new Vector3i(randomPos));
		if(attempts < 30) {
			if(segmentPiece == null || segmentPiece.getType() == 0) return getRandomIndex(entity, attempts + 1);
		}
		return 0;
	}

	private static Transform getRandomHullPoint(SegmentController entity, int attempts) {
		Transform transform = new Transform();
		Vector3f min = entity.getBoundingBox().min;
		Vector3f max = entity.getBoundingBox().max;
		Vector3f size = new Vector3f();
		size.sub(max, min);
		Vector3f randomPos = new Vector3f();
		randomPos.x = min.x + (float) Math.random() * size.x;
		randomPos.y = min.y + (float) Math.random() * size.y;
		randomPos.z = min.z + (float) Math.random() * size.z;
		SegmentPiece segmentPiece = entity.getSegmentBuffer().getPointUnsave(new Vector3i(randomPos));
		if(attempts < 30) {
			if(segmentPiece == null || segmentPiece.getType() == 0) return getRandomHullPoint(entity, attempts + 1);
		}
		return transform;
	}

	private static class WreckDamager implements Damager {

		private final SegmentController entity;
		private final InterEffectSet damageSet = new InterEffectSet();


		public WreckDamager(SegmentController entity) {
			this.entity = entity;
			this.damageSet.setStrength(InterEffectHandler.InterEffectType.EM, 0.5f);
			this.damageSet.setStrength(InterEffectHandler.InterEffectType.HEAT, 0.5f);
			this.damageSet.setStrength(InterEffectHandler.InterEffectType.KIN, 0.5f);
		}

		@Override
		public StateInterface getState() {
			return entity.getState();
		}

		@Override
		public void sendHitConfirm(byte b) {

		}

		@Override
		public boolean isSegmentController() {
			return true;
		}

		@Override
		public SimpleTransformableSendableObject<?> getShootingEntity() {
			return null;
		}

		@Override
		public int getFactionId() {
			return 0;
		}

		@Override
		public String getName() {
			return "Wreck Creator";
		}

		@Override
		public AbstractOwnerState getOwnerState() {
			return entity.getOwnerState();
		}

		@Override
		public void sendClientMessage(String s, int i) {

		}

		@Override
		public float getDamageGivenMultiplier() {
			return 1;
		}

		@Override
		public InterEffectSet getAttackEffectSet(long l, DamageDealerType damageDealerType) {
			return damageSet;
		}

		@Override
		public MetaWeaponEffectInterface getMetaWeaponEffect(long l, DamageDealerType damageDealerType) {
			return null;
		}

		@Override
		public int getSectorId() {
			return entity.getSectorId();
		}

		@Override
		public void sendServerMessage(Object[] objects, int i) {

		}
	}
}
