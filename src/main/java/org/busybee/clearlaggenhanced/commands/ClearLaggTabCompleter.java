package org.busybee.clearlaggenhanced.commands;

import org.busybee.clearlaggenhanced.ClearLaggEnhanced;
import org.busybee.clearlaggenhanced.modules.PerformanceModule;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClearLaggTabCompleter implements TabCompleter {
    private final ClearLaggEnhanced plugin;

    public ClearLaggTabCompleter(ClearLaggEnhanced plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            final List<String> subcommands = List.of("help", "tps", "memory", "check", "reload", "clear", "version", "config", "modules", "module");
            return StringUtil.copyPartialMatches(args[0], subcommands, new ArrayList<>());
        }

        if (args.length >= 1 && args[0].equalsIgnoreCase("module")) {
            if (args.length == 2) {
                final List<String> moduleActions = List.of("enable", "disable");
                return StringUtil.copyPartialMatches(args[1], moduleActions, new ArrayList<>());
            }
            if (args.length == 3 && (args[1].equalsIgnoreCase("enable") || args[1].equalsIgnoreCase("disable"))) {
                final List<String> moduleNames = plugin.getModuleManager().getAllModules().stream()
                        .map(PerformanceModule::getName)
                        .toList();
                return StringUtil.copyPartialMatches(args[2], moduleNames, new ArrayList<>());
            }
        }

        return Collections.emptyList();
    }
}
