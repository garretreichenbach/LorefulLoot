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
 * @author TheDerpGamer (TheDerpGamer#0027)
 */
public class HollowShipCommand implements CommandInterface {
    @Override
    public String getCommand() {
        return "hollow";
    }

    @Override
    public String[] getAliases() {
        return new String[] { "hollow" };
    }

    @Override
    public String getDescription() {
        return "Hollows out the entity you are currently in.\n" +
                " - %COMMAND% : Hollows out the entity you are currently in.";
    }

    @Override
    public boolean isAdminOnly() {
        return true;
    }

    @Override
    public boolean onCommand(PlayerState sender, String[] args) {
        if(!(PlayerUtils.getCurrentControl(sender) instanceof SegmentController)) return false;
        try {
            GenerationManager.hollowEntity((SegmentController) PlayerUtils.getCurrentControl(sender), sender);
        } catch(Exception exception) {
            LorefulLoot.log.log(Level.WARNING, "Failed to hollow entity!", exception);
            PlayerUtils.sendMessage(sender, "Failed to hollow entity! Check the server logs for more information.");
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
