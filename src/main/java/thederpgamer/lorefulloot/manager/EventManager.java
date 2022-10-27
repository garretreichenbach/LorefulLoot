package thederpgamer.lorefulloot.manager;

import api.listener.Listener;
import api.listener.events.world.AsteroidPreSpawnEvent;
import api.mod.StarLoader;
import thederpgamer.lorefulloot.LorefulLoot;

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
				//GenerationManager.generateForSector(event.getSector());
			}
		}, instance);

		LorefulLoot.log.log(java.util.logging.Level.INFO, "EventManager initialized.");
	}
}
