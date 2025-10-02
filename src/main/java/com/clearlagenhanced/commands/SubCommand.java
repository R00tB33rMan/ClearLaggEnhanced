package com.clearlagenhanced.commands;

import org.bukkit.command.CommandSender;

public interface SubCommand {

    /**
     * Execute the subcommand
     * @param sender The command sender
     * @param args The command arguments (excluding the subcommand name)
     * @return true if command executed successfully
     */
    boolean execute(CommandSender sender, String[] args);

    /**
     * Get the permission required for this command
     * @return Permission string
     */
    String getPermission();

    /**
     * Get the help message key for this command
     * @return Message key from messages.yml
     */
    String getHelpMessageKey();
}
