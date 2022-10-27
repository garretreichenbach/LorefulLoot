package thederpgamer.lorefulloot.data.commands;

import api.mod.StarMod;
import api.utils.game.PlayerUtils;
import api.utils.game.chat.CommandInterface;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.player.PlayerState;
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
				" - /%COMMAND% [damage_intensity] : Creates a wreck from the player's current ship based off an intensity value. If no intensity is specified, command will choose one based off the mass.";
	}

	@Override
	public boolean isAdminOnly() {
		return true;
	}

	@Override
	public boolean onCommand(PlayerState sender, String[] args) {
		if(args.length != 1) return false;
		else {
			if(!(PlayerUtils.getCurrentControl(sender) instanceof SegmentController)) {
				PlayerUtils.sendMessage(sender, "You must be controlling a ship to use this command!");
				return true;
			}
			try {
				MiscUtils.wreckShip((SegmentController) PlayerUtils.getCurrentControl(sender), Float.parseFloat(args[0]));
			} catch(Exception exception) {
				PlayerUtils.sendMessage(sender, "You must specify a valid damage intensity!");
			}
			return true;
		}
	}

	@Override
	public void serverAction(@Nullable PlayerState playerState, String[] strings) {

	}

	@Override
	public StarMod getMod() {
		return null;
	}
}
