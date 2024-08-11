package thederpgamer.lorefulloot.data.commands;

import api.mod.StarMod;
import api.utils.game.PlayerUtils;
import api.utils.game.chat.CommandInterface;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.data.player.PlayerState;
import thederpgamer.lorefulloot.LorefulLoot;
import thederpgamer.lorefulloot.utils.MiscUtils;

import javax.annotation.Nullable;

/**
 * [Description]
 *
 * @author TheDerpGamer (MrGoose#0027)
 */
public class CreateWreckCommand implements CommandInterface {

	@Override
	public String getCommand() {
		return "create_wreck";
	}

	@Override
	public String[] getAliases() {
		return new String[] {"create_wreck"};
	}

	@Override
	public String getDescription() {
		return "Creates a wreck from the player's current ship. Note: Will damage the current entity, so make sure to save a copy!\n" +
				" - /%COMMAND : Creates a wreck from the player's current ship.";
	}

	@Override
	public boolean isAdminOnly() {
		return true;
	}

	@Override
	public boolean onCommand(PlayerState sender, String[] args) {
		if(!(PlayerUtils.getCurrentControl(sender) instanceof Ship)) {
			PlayerUtils.sendMessage(sender, "You must be controlling a ship to use this command!");
			return true;
		}
		try {
			MiscUtils.wreckShip((Ship) PlayerUtils.getCurrentControl(sender));
		} catch(Exception exception) {
			LorefulLoot.getInstance().logException("Failed to create wreck!", exception);
			PlayerUtils.sendMessage(sender, "Failed to create wreck! Check the server logs for more information.");
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
