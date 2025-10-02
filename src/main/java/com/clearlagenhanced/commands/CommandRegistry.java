package com.clearlagenhanced.commands;

import com.clearlagenhanced.ClearLaggEnhanced;
import com.clearlagenhanced.commands.subcommands.*;
import com.clearlagenhanced.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public enum CommandRegistry {

    HELP("help", new HelpCommand()),
    CLEAR("clear", new ClearCommand()),
    NEXT("next", new NextCommand()),
    TPS("tps", new TpsCommand()),
    RAM("ram", new RamCommand()),
    CHUNKFINDER("chunkfinder", new ChunkFinderCommand()),
    ADMIN("admin", new AdminCommand()),
    RELOAD("reload", new ReloadCommand());

    private final String name;
    private final SubCommand executor;

    CommandRegistry(String name, SubCommand executor) {
        this.name = name;
        this.executor = executor;
    }

    public String getName() {
        return name;
    }

    public SubCommand getExecutor() {
        return executor;
    }

    /**
     * Execute this command with permission checking
     * @param sender The command sender
     * @param args The command arguments
     * @return true if command executed successfully
     */
    public boolean execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission(executor.getPermission())) {
            MessageUtils.sendMessage(sender, "notifications.no-permission");
            return true;
        }
        return executor.execute(sender, args);
    }

    /**
     * Find a command by name
     * @param name Command name to search for
     * @return CommandRegistry enum or null if not found
     */
    public static CommandRegistry fromString(String name) {
        for (CommandRegistry cmd : values()) {
            if (cmd.name.equalsIgnoreCase(name)) {
                return cmd;
            }
        }
        return null;
    }

    /**
     * Get all command names for tab completion
     * @return Array of command names
     */
    public static String[] getCommandNames() {
        CommandRegistry[] commands = values();
        String[] names = new String[commands.length];
        for (int i = 0; i < commands.length; i++) {
            names[i] = commands[i].name;
        }
        return names;
    }
}
