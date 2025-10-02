package com.clearlagenhanced.commands;

import com.clearlagenhanced.ClearLaggEnhanced;
import com.clearlagenhanced.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LaggCommand implements CommandExecutor, TabCompleter {

    private final ClearLaggEnhanced plugin;

    public LaggCommand(ClearLaggEnhanced plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            // Show help by default
            CommandRegistry.HELP.execute(sender, new String[0]);
            return true;
        }

        String subCommandName = args[0].toLowerCase();
        CommandRegistry subCommand = CommandRegistry.fromString(subCommandName);

        if (subCommand == null) {
            Map<String, String> ph = new HashMap<>();
            ph.put("sub", subCommandName);
            MessageUtils.sendMessage(sender, "commands.unknown-subcommand", ph);
            CommandRegistry.HELP.execute(sender, new String[0]);
            return true;
        }

        // Remove the subcommand name from args
        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
        return subCommand.execute(sender, subArgs);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            String[] subCommands = CommandRegistry.getCommandNames();

            for (String subCommand : subCommands) {
                if (subCommand.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(subCommand);
                }
            }

            return completions;
        }

        return new ArrayList<>();
    }
}
