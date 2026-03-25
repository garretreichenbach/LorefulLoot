package videogoose.lorefulloot.listener;

import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.StellarSystem;
import org.schema.game.common.data.world.Universe;
import org.schema.game.server.data.Galaxy;
import videogoose.lorefulloot.LorefulLoot;
import videogoose.lorefulloot.manager.GenerationManager;

import java.io.IOException;

public class SectorGenerateEventListener implements SectorGenerateListener {

	@Override
	public void onGenerate(Universe universe, Sector sector, long TimeTaken) throws IOException {
		StellarSystem system = sector._getSystem();
		Galaxy galaxy = universe.getGalaxyFromSystemPos(system.getPos());
		if(galaxy == null) {
			LorefulLoot.getInstance().logWarning("Galaxy not found for sector generation event!");
			return;
		}
		GenerationManager.generateForSector(sector, sector.getSectorType(), true);
	}
}