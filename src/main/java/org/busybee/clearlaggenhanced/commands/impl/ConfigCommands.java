package org.busybee.clearlaggenhanced.commands.impl;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.busybee.clearlaggenhanced.ClearLaggEnhanced;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

@CommandAlias("clearlagg|cle")
public class ConfigCommands extends BaseCommand {
    
    private final ClearLaggEnhanced plugin;
    
    public ConfigCommands(ClearLaggEnhanced plugin) {
        this.plugin = plugin;
    }
    
    @Subcommand("config")
    @Description("Show configuration information")
    @CommandPermission("clearlaggenhanced.config")
    public void onConfig(CommandSender sender) {
        sender.sendMessage(ChatColor.GREEN + "=== Configuration Status ===");
        sender.sendMessage(ChatColor.YELLOW + "Main config loaded: " + 
            (plugin.getConfigManager().getMainConfig() != null ? ChatColor.GREEN + "Yes" : ChatColor.RED + "No"));
        sender.sendMessage(ChatColor.YELLOW + "Module configs: " + 
            ChatColor.WHITE + plugin.getConfigManager().getAllModuleConfigs().size());
        sender.sendMessage(ChatColor.GRAY + "Use " + ChatColor.WHITE + "/cle reload" + 
            ChatColor.GRAY + " to reload configuration");
    }
}
