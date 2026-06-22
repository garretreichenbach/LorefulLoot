package videogoose.lorefulloot.listener;

import api.listener.fastevents.world.SectorGenerateListener;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.Universe;
import videogoose.lorefulloot.manager.GenerationManager;

public class SectorGenerateEventListener implements SectorGenerateListener {

	@Override
	public void onGenerate(Universe universe, Sector sector, long TimeTaken) {
		//Fires the first time a sector is created - which AI/fleets/missiles can trigger remotely with no
		//player present. GenerationManager only generates if a player is near, deferring the rest.
		GenerationManager.requestGeneration(universe, sector);
	}
}