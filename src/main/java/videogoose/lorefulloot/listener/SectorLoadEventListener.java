package videogoose.lorefulloot.listener;

import api.listener.fastevents.world.SectorLoadListener;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.Universe;
import videogoose.lorefulloot.manager.GenerationManager;

public class SectorLoadEventListener implements SectorLoadListener {

	@Override
	public void onLoad(Universe universe, Sector sector, long timeTaken) {
		//Fires when an already-created sector is loaded. If generation was deferred because the sector was
		//created remotely with no player nearby, generate it now that a player is actually loading it.
		GenerationManager.onSectorLoad(universe, sector);
	}
}
