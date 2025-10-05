package com.clearlagenhanced.commands.subcommands;

import com.clearlagenhanced.ClearLaggEnhanced;
import com.clearlagenhanced.commands.SubCommand;
import com.clearlagenhanced.utils.MessageUtils;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;

public class ClearCommand implements SubCommand {

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        MessageUtils.sendMessage(sender, "commands.clear.starting");

        long startTime = System.currentTimeMillis();
        int cleared = ClearLaggEnhanced.getInstance().getEntityManager().clearEntities();
        long duration = System.currentTimeMillis() - startTime;

        Map<String, String> ph = new HashMap<>();
        ph.put("count", String.valueOf(cleared));
        ph.put("time", String.valueOf(duration));
        MessageUtils.sendMessage(sender, "notifications.clear-complete", ph);
        return true;
    }

    @Override
    public String getPermission() {
        return "CLE.clear";
    }

    @Override
    public String getHelpMessageKey() {
        return "commands.help.clear";
    }
}
