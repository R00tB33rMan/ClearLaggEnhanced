package com.clearlagenhanced.commands.subcommands;

import com.clearlagenhanced.ClearLaggEnhanced;
import com.clearlagenhanced.commands.SubCommand;
import com.clearlagenhanced.utils.MessageUtils;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;

public class TpsCommand implements SubCommand {

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        double tps = ClearLaggEnhanced.getInstance().getPerformanceManager().getTPS();
        Map<String, String> ph = new HashMap<>();
        ph.put("tps", String.format("%.2f", tps));
        MessageUtils.sendMessage(sender, "performance.tps", ph);
        return true;
    }

    @Override
    public String getPermission() {
        return "CLE.tps";
    }

    @Override
    public String getHelpMessageKey() {
        return "commands.help.tps";
    }
}
