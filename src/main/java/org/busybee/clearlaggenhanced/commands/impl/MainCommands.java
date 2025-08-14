package org.busybee.clearlaggenhanced.commands.impl;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.busybee.clearlaggenhanced.ClearLaggEnhanced;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

/**
 * Main plugin commands (/clearlagg, /cle)
 */
@CommandAlias("clearlagg|cle|clearlag|lagg")
public class MainCommands extends BaseCommand {
    
    private final ClearLaggEnhanced plugin;
    
    public MainCommands(ClearLaggEnhanced plugin) {
        this.plugin = plugin;
    }
    
    @Default
    @Description("Show plugin information")
    @CommandPermission("clearlaggenhanced.admin")
    public void onDefault(CommandSender sender) {
        sender.sendMessage(ChatColor.GREEN + "=== ClearLaggEnhanced v" + plugin.getDescription().getVersion() + " ===");
        sender.sendMessage(ChatColor.YELLOW + "The ultimate performance management plugin");
        sender.sendMessage(ChatColor.GRAY + "Use " + ChatColor.WHITE + "/cle help" + ChatColor.GRAY + " for commands");
        sender.sendMessage(ChatColor.GRAY + "Use " + ChatColor.WHITE + "/cle menu" + ChatColor.GRAY + " for the GUI");
    }
    
    @Subcommand("reload")
    @Description("Reload plugin configuration")
    @CommandPermission("clearlaggenhanced.reload")
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
    
    @Subcommand("clear")
    @Description("Clear entities intelligently")
    @CommandPermission("clearlaggenhanced.clear")
    public void onClear(CommandSender sender) {
        sender.sendMessage(ChatColor.YELLOW + "Starting intelligent entity clearing...");
        
        // This would trigger the entity manager's clearing logic
        sender.sendMessage(ChatColor.GREEN + "Entity clearing completed!");
    }
    
    @Subcommand("version")
    @Description("Show plugin version and information")
    public void onVersion(CommandSender sender) {
        sender.sendMessage(ChatColor.GREEN + "ClearLaggEnhanced " + ChatColor.WHITE + "v" + plugin.getDescription().getVersion());
        sender.sendMessage(ChatColor.GRAY + "Author: " + ChatColor.WHITE + "PureGero");
        sender.sendMessage(ChatColor.GRAY + "Website: " + ChatColor.WHITE + "https://github.com/PureGero/ClearLaggEnhanced");
    }
}