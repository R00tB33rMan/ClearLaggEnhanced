package org.busybee.clearlaggenhanced.commands.impl;

import org.busybee.clearlaggenhanced.ClearLaggEnhanced;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class MainCommands {
    
    private final ClearLaggEnhanced plugin;
    
    public MainCommands(ClearLaggEnhanced plugin) {
        this.plugin = plugin;
    }
    
    public void onHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GREEN + "=== ClearLaggEnhanced v" + plugin.getDescription().getVersion() + " ===");
        sender.sendMessage(ChatColor.YELLOW + "The ultimate performance management plugin");
        sender.sendMessage(ChatColor.GRAY + "Commands:");
        sender.sendMessage(ChatColor.WHITE + "/cle tps" + ChatColor.GRAY + " - Show server TPS and MSPT");
        sender.sendMessage(ChatColor.WHITE + "/cle memory" + ChatColor.GRAY + " - Show memory usage");
        sender.sendMessage(ChatColor.WHITE + "/cle check" + ChatColor.GRAY + " - Comprehensive health check");
        sender.sendMessage(ChatColor.WHITE + "/cle modules" + ChatColor.GRAY + " - List modules and status");
        sender.sendMessage(ChatColor.WHITE + "/cle module enable <name>" + ChatColor.GRAY + " - Enable a module");
        sender.sendMessage(ChatColor.WHITE + "/cle module disable <name>" + ChatColor.GRAY + " - Disable a module");
        sender.sendMessage(ChatColor.WHITE + "/cle clear" + ChatColor.GRAY + " - Clear entities intelligently");
        sender.sendMessage(ChatColor.WHITE + "/cle reload" + ChatColor.GRAY + " - Reload configuration");
        sender.sendMessage(ChatColor.WHITE + "/cle version" + ChatColor.GRAY + " - Show plugin version");
    }

    public void onReload(CommandSender sender) {
        sender.sendMessage(ChatColor.YELLOW + "Reloading ClearLaggEnhanced configuration...");
        
        try {
            plugin.getConfigManager().reloadAll();
            plugin.getModuleManager().reloadAll();
            sender.sendMessage(ChatColor.GREEN + "Configuration reloaded successfully!");
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Failed to reload configuration: " + e.getMessage());
        }
    }
    
    public void onClear(CommandSender sender) {
        sender.sendMessage(ChatColor.YELLOW + "Starting intelligent entity clearing...");

        // The EntityManagerModule does not yet expose a public clear method.
        // Preserve behavior by reporting availability based on module enablement.
        if (plugin.getModuleManager().isModuleEnabled("entity-manager")) {
            sender.sendMessage(ChatColor.RED + "Entity clearing is currently unavailable in this build.");
        } else {
            sender.sendMessage(ChatColor.RED + "Entity clearing is currently disabled.");
        }
    }
    
    public void onVersion(CommandSender sender) {
        sender.sendMessage(ChatColor.GREEN + "ClearLaggEnhanced " + ChatColor.WHITE + "v" + plugin.getDescription().getVersion());
        sender.sendMessage(ChatColor.GRAY + "Author: " + ChatColor.WHITE + "djtmk");
        sender.sendMessage(ChatColor.GRAY + "Website: " + ChatColor.WHITE + "https://github.com/PureGero/ClearLaggEnhanced");
    }
}
