package thederpgamer.lorefulloot.data.commands;

import api.common.GameServer;
import api.mod.StarMod;
import api.utils.game.chat.CommandInterface;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.Sector;
import thederpgamer.lorefulloot.LorefulLoot;
import thederpgamer.lorefulloot.manager.GenerationManager;

import javax.annotation.Nullable;

/**
 * [Description]
 *
 * @author TheDerpGamer (MrGoose#0027)
 */
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
			GenerationManager.generateForSector(sector, sector.getSectorType(), true);
		} catch(Exception exception) {
			exception.printStackTrace();
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
