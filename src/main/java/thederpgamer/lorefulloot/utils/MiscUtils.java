package thederpgamer.lorefulloot.utils;

import com.bulletphysics.linearmath.Transform;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.controller.damage.effects.InterEffectHandler;
import org.schema.game.common.controller.damage.effects.InterEffectSet;
import org.schema.game.common.controller.damage.effects.MetaWeaponEffectInterface;
import org.schema.game.common.controller.elements.ModuleExplosion;
import org.schema.game.common.controller.elements.VoidElementManager;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.player.AbstractOwnerState;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.network.StateInterface;

import javax.vecmath.Vector3f;

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
	public static void wreckShip(SegmentController entity) {
		((Ship) entity).getManagerContainer().getShieldAddOn().setShields(0);
		((Ship) entity).getManagerContainer().getShieldAddOn().setShieldCapacityHP(0);
		((Ship) entity).getManagerContainer().getShieldAddOn().setRegenEnabled(false);

		Vector3f min = entity.getMinPos().toVector3f();
		Vector3f max = entity.getMaxPos().toVector3f();
		Vector3f size = new Vector3f();
		size.sub(max, min);
		int explosionCap = 15;
		float radius = size.length();
		LongArrayList l = new LongArrayList(explosionCap);
		for(int i = 0; i < explosionCap; i++) {
			l.add(getRandomIndex(entity, 0));
		}
		ModuleExplosion expl =
				new ModuleExplosion(l,
						VoidElementManager.COLLECTION_INTEGRITY_EXPLOSION_RATE,
						(int) radius,
						50000000,
						getRandomIndex(entity, 0),
						ModuleExplosion.ExplosionCause.INTEGRITY,
						entity.getBoundingBox());

		expl.setChain(true);
		((ManagedSegmentController<?>) entity).getManagerContainer().addModuleExplosions(expl);

		/*
		while(explosions < explosionCap) {
			((EditableSendableSegmentController) entity).addExplosion(
					new WreckDamager(entity),
					DamageDealerType.EXPLOSIVE,
					HitType.GENERAL,
					Long.MIN_VALUE,
					getRandomHullPoint(entity, 0),
					radius,
					5000000,
					true,
					new AfterExplosionCallback() {
						@Override
						public void onExplosionDone() {

						}
					}, ExplosionData.INNER | ExplosionData.IGNORESHIELDS_GLOBAL);


			explosions ++;
		}


		 */
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
		return segmentPiece.getAbsoluteIndex();
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
