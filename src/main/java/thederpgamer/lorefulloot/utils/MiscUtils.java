package thederpgamer.lorefulloot.utils;

import api.common.GameServer;
import api.utils.StarRunnable;
import com.bulletphysics.linearmath.Transform;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.Vector3b;
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
import org.schema.game.common.controller.rails.RailRelation;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.meta.Logbook;
import org.schema.game.common.data.element.meta.MetaObjectManager;
import org.schema.game.common.data.player.AbstractOwnerState;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.network.StateInterface;
import thederpgamer.lorefulloot.LorefulLoot;
import thederpgamer.lorefulloot.data.ItemStack;
import thederpgamer.lorefulloot.data.generation.EntityLore;
import thederpgamer.lorefulloot.data.generation.EntitySpawn;
import thederpgamer.lorefulloot.manager.GenerationManager;

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
					while(!entity.isFullyLoadedWithDock()) Thread.sleep(100);
				} catch(Exception exception) {
					exception.printStackTrace();
				}

				try {
					for(int j = 0; j < 5; j++) {
						((Ship) entity).getManagerContainer().getShieldAddOn().setShields(0);
						((Ship) entity).getManagerContainer().getShieldAddOn().setShieldCapacityHP(0);
						((Ship) entity).getManagerContainer().getShieldAddOn().setRegenEnabled(false);
						int explosionCap = 10;
						float radius = 10;
						if(entity.getName().contains("Small")) radius = 3;
						else if(entity.getName().contains("Medium")) radius = 5;
						LongArrayList l = new LongArrayList(explosionCap);
						for(int i = 0; i < explosionCap; i++) l.add(getRandomIndex(entity, 0));
						long index = getRandomIndex(entity, 0);
						Segment segment = entity.getSegmentBuffer().getPointUnsave(index).getSegment();
						if(segment.pos.length() == 0 && !entity.getName().contains("Small")) continue; //Dont explode in the core
						ModuleExplosion expl = new ModuleExplosion(l, 5, (int) radius, 50000000, index, ModuleExplosion.ExplosionCause.STABILITY, entity.getBoundingBox());
						expl.setChain(true);
						((ManagedSegmentController<?>) entity).getManagerContainer().addModuleExplosions(expl);
					}
				} catch(Exception exception) {
					exception.printStackTrace();
					entity.setMarkedForDeletePermanentIncludingDocks(true);
					entity.setMarkedForDeleteVolatileIncludingDocks(true);
				}
			}
		}).start();

		new StarRunnable() {
			@Override
			public void run() {
				try {
					entity.stopCoreOverheating();
					genItems(entity, entitySpawn);
				} catch(Exception exception) {
					exception.printStackTrace();
					entity.setMarkedForDeletePermanentIncludingDocks(true);
					entity.setMarkedForDeleteVolatileIncludingDocks(true);
					LorefulLoot.log.warning("Ship " + entity.getName() + " has been deleted due to exception.");
				}
			}
		}.runLater(LorefulLoot.getInstance(), 60);

		/*
		new StarRunnable() {
			@Override
			public void run() {
				Ship ship = (Ship) entity;
				if(!ship.getSegmentBuffer().existsPointUnsave(new Vector3i(0, 0, 0)) || !ship.checkCore(ship.getSegmentBuffer().getPointUnsave(new Vector3i(0, 0, 0)))) {
					ship.setMarkedForDeletePermanentIncludingDocks(true);
					ship.setMarkedForDeleteVolatileIncludingDocks(true);
					LorefulLoot.log.warning("Ship " + ship.getName() + " has been deleted due to core destruction.");
				}
			}
		}.runLater(LorefulLoot.getInstance(), 120);
		 */
	}

	private static void genItems(SegmentController entity, EntitySpawn entitySpawn) {
		if(entitySpawn != null) {
			ObjectArrayList<Inventory> inventories = ((ManagedUsableSegmentController<?>) entity).getInventories().inventoriesList;
			if(entitySpawn.getEntityLore() != null) {
				Logbook logbook = (Logbook) MetaObjectManager.instantiate(MetaObjectManager.MetaObjectType.LOG_BOOK, (short) - 1, true);
				String fullText = entitySpawn.getEntityLore().getHeader() + "\n" + entitySpawn.getEntityLore().getContent();
				fullText = StringTools.wrap(fullText, 80);
				logbook.setTxt(fullText);
				Inventory inventory = inventories.get((new Random()).nextInt(inventories.size()));
				try {
					inventory.put(inventory.getFreeSlot(), logbook);
					inventory.sendAll();
				} catch(Exception exception) {
					LorefulLoot.log.log(java.util.logging.Level.WARNING, "Failed to add items to entity " + entitySpawn.getName() + " in sector " + entity.getSector(new Vector3i()) + "!", exception);
					exception.printStackTrace();
				}
			} else {
				Sector sector = GameServer.getServerState().getUniverse().getSector(entity.getSectorId());
				EntityLore entityLore = GenerationManager.generateRandomLore(sector);
				if(entityLore != null) {
					Logbook logbook = (Logbook) MetaObjectManager.instantiate(MetaObjectManager.MetaObjectType.LOG_BOOK, (short) - 1, true);
					String fullText = entityLore.getHeader() + "\n" + entityLore.getContent();
					fullText = StringTools.wrap(fullText, 80);
					logbook.setTxt(fullText);
					Inventory inventory = inventories.get((new Random()).nextInt(inventories.size()));
					try {
						inventory.put(inventory.getFreeSlot(), logbook);
						inventory.sendAll();
					} catch(Exception exception) {
						exception.printStackTrace();
					}
				}
			}

			for(Inventory inventory : inventories) {
				try {
					if(entitySpawn.getItems() == null || entitySpawn.getItems().length == 0) {
						ItemStack[] itemStacks = GenerationManager.generateRandomItemStacks(5, 30);
						for(ItemStack item : itemStacks) item.addTo(inventory);
					} else for(ItemStack item : entitySpawn.getItems()) item.addTo(inventory);
					inventory.sendAll();
				} catch(Exception exception) {
					LorefulLoot.log.log(java.util.logging.Level.WARNING, "Failed to add items to entity " + entitySpawn.getName() + " in sector " + entity.getSector(new Vector3i()) + "!", exception);
					exception.printStackTrace();
				}
			}
			LorefulLoot.log.log(java.util.logging.Level.INFO, "Spawned entity " + entitySpawn.getName() + " in sector " + entity.getSector(new Vector3i()) + "!");
		}
		for(RailRelation relation : entity.railController.next) {
			if(relation.rail != null) {
				if(relation.rail.getSegmentController() != entity) genItems(relation.rail.getSegmentController(), entitySpawn);
			}
		}
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
