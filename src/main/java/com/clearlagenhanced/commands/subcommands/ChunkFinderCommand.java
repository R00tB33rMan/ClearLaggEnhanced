package com.clearlagenhanced.commands.subcommands;

import com.clearlagenhanced.ClearLaggEnhanced;
import com.clearlagenhanced.commands.SubCommand;
import com.clearlagenhanced.utils.MessageUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChunkFinderCommand implements SubCommand {

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtils.sendMessage(sender, "errors.player-only");
            return true;
        }

        Player player = (Player) sender;
        ClearLaggEnhanced.getInstance().getPerformanceManager().findLaggyChunksAsync(player);
        return true;
    }

    @Override
    public String getPermission() {
        return "CLE.chunkfinder";
    }

    @Override
    public String getHelpMessageKey() {
        return "commands.help.chunkfinder";
    }
}
