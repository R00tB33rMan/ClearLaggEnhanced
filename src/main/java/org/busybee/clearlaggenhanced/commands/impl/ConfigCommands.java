package org.busybee.clearlaggenhanced.commands.impl;

import org.busybee.clearlaggenhanced.ClearLaggEnhanced;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class ConfigCommands {
    
    private final ClearLaggEnhanced plugin;
    
    public ConfigCommands(ClearLaggEnhanced plugin) {
        this.plugin = plugin;
    }
    
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
