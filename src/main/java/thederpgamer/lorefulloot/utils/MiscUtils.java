package thederpgamer.lorefulloot.utils;

import com.bulletphysics.linearmath.Transform;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.EditableSendableSegmentController;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.controller.damage.HitType;
import org.schema.game.common.controller.damage.effects.InterEffectHandler;
import org.schema.game.common.controller.damage.effects.InterEffectSet;
import org.schema.game.common.controller.damage.effects.MetaWeaponEffectInterface;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.explosion.AfterExplosionCallback;
import org.schema.game.common.data.explosion.ExplosionData;
import org.schema.game.common.data.player.AbstractOwnerState;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.network.StateInterface;

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
	 * @param intensity Damage intensity.
	 * <p>Note: Make sure you save a copy of your ship before using this function!</p>
	 */
	public static void wreckShip(SegmentController entity, float intensity) {
		intensity = Math.max(1, Math.min(10, intensity));
		Vector3f min = entity.getMinPos().toVector3f();
		Vector3f max = entity.getMaxPos().toVector3f();
		Vector3f size = new Vector3f();
		size.sub(max, min);
		int explosionCap = (int) ((entity.getTotalPhysicalMass() / size.length()) * intensity);
		int explosions = 0;
		float radius = (size.length() / 4) * (new Random().nextFloat() + intensity);
		while(explosions < explosionCap) {
			((EditableSendableSegmentController) entity).addExplosion(
					new WreckDamager(entity),
					DamageDealerType.EXPLOSIVE,
					HitType.ENVIROMENTAL,
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
	}

	private static Transform getRandomHullPoint(SegmentController entity, int attempts) {
		Transform transform = new Transform();
		Vector3f min = entity.getBoundingBox().min;
		Vector3f max = entity.getBoundingBox().max;
		Vector3f size = new Vector3f();
		size.sub(min, max);
		Vector3f center = new Vector3f();
		center.add(min, max);
		Vector3f randomPos = randomizeOffset(center, (int) size.length());
		transform.origin.set(randomPos);
		SegmentPiece segmentPiece = entity.getSegmentBuffer().getPointUnsave(new Vector3i(randomPos));
		if(attempts < 30) {
			if(segmentPiece == null || segmentPiece.getType() == 0 || !segmentPiece.getInfo().getName().contains("Armor")) return getRandomHullPoint(entity, attempts + 1);
		}
		return transform;
	}

	private static Vector3f randomizeOffset(Vector3f offset, int range) {
		Random random = new Random();
		offset.x += random.nextInt(range) - range / 2;
		offset.y += random.nextInt(range) - range / 2;
		offset.z += random.nextInt(range) - range / 2;
		return offset;
	}

	private static class WreckDamager implements Damager {

		private final SegmentController entity;
		private final InterEffectSet damageSet = new InterEffectSet();


		public WreckDamager(SegmentController entity) {
			this.entity = entity;
			this.damageSet.setStrength(InterEffectHandler.InterEffectType.HEAT, 1000.0f);
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
			return null;
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
