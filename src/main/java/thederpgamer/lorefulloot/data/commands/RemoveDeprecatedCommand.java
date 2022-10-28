package thederpgamer.lorefulloot.data.commands;

import api.mod.StarMod;
import api.utils.game.PlayerUtils;
import api.utils.game.chat.CommandInterface;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.player.PlayerState;
import thederpgamer.lorefulloot.LorefulLoot;
import thederpgamer.lorefulloot.manager.GenerationManager;

import javax.annotation.Nullable;
import java.util.logging.Level;

/**
 * [Description]
 *
 * @author TheDerpGamer (MrGoose#0027)
 */
public class RemoveDeprecatedCommand implements CommandInterface {
	@Override
	public String getCommand() {
		return "sanitize";
	}

	@Override
	public String[] getAliases() {
		return new String[] {"sanitize"};
	}

	@Override
	public String getDescription() {
		return "Removes deprecated items from the entered segment controller.\n" +
				" - /%COMMAND% : Removes deprecated items from the current segment controller.";
	}

	@Override
	public boolean isAdminOnly() {
		return true;
	}

	@Override
	public boolean onCommand(PlayerState playerState, String[] strings) {
		if(!(PlayerUtils.getCurrentControl(playerState) instanceof SegmentController)) return false;
		try {
			GenerationManager.sanitizeEntity((SegmentController) PlayerUtils.getCurrentControl(playerState), playerState);
		} catch(Exception exception) {
			LorefulLoot.log.log(Level.WARNING, "Failed to sanitize entity!", exception);
			PlayerUtils.sendMessage(playerState, "Failed to sanitize entity! Check the server logs for more information.");
		}
		return true;
	}

	@Override
	public void serverAction(@Nullable PlayerState playerState, String[] strings) {

	}

	@Override
	public StarMod getMod() {
		return null;
	}
}
