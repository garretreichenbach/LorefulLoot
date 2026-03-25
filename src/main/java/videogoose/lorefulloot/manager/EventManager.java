package videogoose.lorefulloot.manager;

import api.listener.Listener;
import api.listener.events.block.SegmentPieceSalvageCheckEvent;
import api.listener.events.entity.SegmentControllerOverheatEvent;
import api.listener.fastevents.FastListenerCommon;
import api.mod.StarLoader;
import videogoose.lorefulloot.LorefulLoot;
import videogoose.lorefulloot.listener.SectorGenerateEventListener;

public class EventManager {


	public static void initialize(LorefulLoot instance) {
		FastListenerCommon.sectorGenerateListeners.add(new SectorGenerateEventListener());

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
				if(WreckageManager.canSalvage(event.getController().getUniqueIdentifier())) {
					event.setCanSalvage(true);
				}
			}
		}, instance);

		LorefulLoot.getInstance().logInfo("Initialized Events");
	}
}