package thederpgamer.lorefulloot.manager;

import api.listener.Listener;
import api.listener.events.entity.SegmentControllerFullyLoadedEvent;
import api.listener.events.entity.SegmentControllerOverheatEvent;
import api.listener.events.world.AsteroidPreSpawnEvent;
import api.listener.events.world.PlanetCreateEvent;
import api.mod.StarLoader;
import thederpgamer.lorefulloot.LorefulLoot;
import thederpgamer.lorefulloot.utils.MiscUtils;

/**
 * [Description]
 *
 * @author TheDerpGamer (MrGoose#0027)
 */
public class EventManager {

	public static void initialize(LorefulLoot instance) {
		StarLoader.registerListener(AsteroidPreSpawnEvent.class, new Listener<AsteroidPreSpawnEvent>() {
			@Override
			public void onEvent(AsteroidPreSpawnEvent event) {
				GenerationManager.generateForSector(event.getSector(), false);
			}
		}, instance);

		StarLoader.registerListener(PlanetCreateEvent.class, new Listener<PlanetCreateEvent>() {
			@Override
			public void onEvent(PlanetCreateEvent event) {
				GenerationManager.generateForSector(event.getSector(), false);
			}
		}, instance);

		StarLoader.registerListener(SegmentControllerOverheatEvent.class, new Listener<SegmentControllerOverheatEvent>() {
			@Override
			public void onEvent(SegmentControllerOverheatEvent event) {
				if(ConfigManager.getMainConfig().getBoolean("generate-shipwrecks-from-combat")) {
					try {
						GenerationManager.createShipWreckFromCombat(event);
					} catch(Exception exception) {
						LorefulLoot.log.warning("Failed to generate shipwreck from combat:\n" + exception.getMessage());
					}
				}
			}
		}, instance);

		StarLoader.registerListener(SegmentControllerFullyLoadedEvent.class, new Listener<SegmentControllerFullyLoadedEvent>() {
			@Override
			public void onEvent(SegmentControllerFullyLoadedEvent event) {
				if(event.getController().getName().contains("Shipwreck") && !event.getController().isMinable()) {
					event.getController().setMinable(true);
					MiscUtils.putItems(event.getController());
				}
			}
		}, instance);

		LorefulLoot.log.log(java.util.logging.Level.INFO, "EventManager initialized.");
	}
}
