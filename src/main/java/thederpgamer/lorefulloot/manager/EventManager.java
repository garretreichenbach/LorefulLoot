package thederpgamer.lorefulloot.manager;

import api.listener.Listener;
import api.listener.events.block.SegmentPieceSalvageCheckEvent;
import api.listener.events.entity.SegmentControllerOverheatEvent;
import api.listener.events.world.sector.SectorGenerateEvent;
import api.mod.StarLoader;
import thederpgamer.lorefulloot.LorefulLoot;

public class EventManager {

	public static void initialize(LorefulLoot instance) {
		StarLoader.registerListener(SectorGenerateEvent.class, new Listener<SectorGenerateEvent>() {
			@Override
			public void onEvent(SectorGenerateEvent event) {
				GenerationManager.generateForSector(event.getSector(), event.getType());
			}
		}, instance);

		StarLoader.registerListener(SegmentControllerOverheatEvent.class, new Listener<SegmentControllerOverheatEvent>() {
			@Override
			public void onEvent(SegmentControllerOverheatEvent event) {
				if(ConfigManager.getMainConfig().getBoolean("generate-shipwrecks-from-combat")) {
					try {
						GenerationManager.createShipWreckFromCombat(event);
					} catch(Exception exception) {
						LorefulLoot.getInstance().logException("Failed to create ship wreck from combat!", exception);
					}
				}
			}
		}, instance);

		StarLoader.registerListener(SegmentPieceSalvageCheckEvent.class, new Listener<SegmentPieceSalvageCheckEvent>() {
			@Override
			public void onEvent(SegmentPieceSalvageCheckEvent event) {
				if(event.getController().getRealName().startsWith("[Wreckage] ")) {
					event.setCanSalvage(true);
				}
			}
		}, instance);

		LorefulLoot.getInstance().logInfo("Initialized Events");
	}
}