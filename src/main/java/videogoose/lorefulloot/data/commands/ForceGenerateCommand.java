package videogoose.lorefulloot.data.commands;

import api.common.GameServer;
import api.mod.StarMod;
import api.utils.game.chat.CommandInterface;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.Sector;
import videogoose.lorefulloot.LorefulLoot;
import videogoose.lorefulloot.manager.GenerationManager;

import javax.annotation.Nullable;

public class ForceGenerateCommand implements CommandInterface {

	@Override
	public String getCommand() {
		return "force_generate";
	}

	@Override
	public String[] getAliases() {
		return new String[] {"force_generate"};
	}

	@Override
	public String getDescription() {
		return "Forces the generation of loot in the current sector.\n" +
				" - /%COMMAND% : Forces the generation of loot in the current sector.";
	}

	@Override
	public boolean isAdminOnly() {
		return true;
	}

	@Override
	public boolean onCommand(PlayerState playerState, String[] strings) {
		try {
			Sector sector = GameServer.getUniverse().getSector(playerState.getCurrentSectorId());
			javax.vecmath.Vector4f starColor = new javax.vecmath.Vector4f(1, 1, 1, 1);
			try {
				org.schema.game.server.data.Galaxy galaxy = sector.getState().getUniverse().getGalaxyFromSystemPos(sector._getSystem().getPos());
				if (galaxy != null) {
					starColor = galaxy.getSunColor(sector._getSystem().getPos());
				}
			} catch(Exception ignored) {}
			GenerationManager.generateForSector(sector, sector.getSectorType(), starColor, true);
		} catch(Exception exception) {
			LorefulLoot.getInstance().logException("Failed to force generate loot for sector!", exception);
		}
		return true;
	}

	@Override
	public void serverAction(@Nullable PlayerState playerState, String[] strings) {

	}

	@Override
	public StarMod getMod() {
		return LorefulLoot.getInstance();
	}
}
