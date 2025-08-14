package org.busybee.clearlaggenhanced.commands.impl;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.busybee.clearlaggenhanced.ClearLaggEnhanced;
import org.busybee.clearlaggenhanced.modules.PerformanceModule;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;

/**
 * Module management commands
 */
@CommandAlias("clearlagg|cle")
public class ModuleCommands extends BaseCommand {
    
    private final ClearLaggEnhanced plugin;
    
    public ModuleCommands(ClearLaggEnhanced plugin) {
        this.plugin = plugin;
    }
    
    @Subcommand("modules")
    @Description("List all modules and their status")
    @CommandPermission("clearlaggenhanced.admin")
    public void onModules(CommandSender sender) {
        // Localized header
        sender.sendMessage(plugin.getLanguageManager().get("ui.modules.header"));
        
        for (PerformanceModule module : plugin.getModuleManager().getAllModules()) {
            PerformanceModule.ModuleStats stats = module.getStats();
            
            String status = plugin.getModuleManager().isModuleEnabled(module.getName()) ?
                ChatColor.GREEN + "ENABLED" : ChatColor.RED + "DISABLED";
            
            sender.sendMessage(ChatColor.YELLOW + module.getName() + ChatColor.GRAY + " - " + status);
            sender.sendMessage(ChatColor.GRAY + "  Status: " + stats.status() + ChatColor.GRAY + ", Ops: " + stats.operations());
        }
    }
    
    @Subcommand("module enable")
    @Description("Enable a specific module")
    @CommandPermission("clearlaggenhanced.admin")
    @CommandCompletion("@modules")
    public void onModuleEnable(CommandSender sender, String moduleName) {
        if (plugin.getModuleManager().getModule(moduleName) == null) {
            Map<String, String> ph = new HashMap<>();
            ph.put("module", moduleName);
            sender.sendMessage(plugin.getLanguageManager().get("module.not_found", ph));
            return;
        }
        
        plugin.getModuleManager().enableModule(moduleName);
        Map<String, String> ph = new HashMap<>();
        ph.put("module", moduleName);
        sender.sendMessage(plugin.getLanguageManager().get("module.enabled", ph));
    }
    
    @Subcommand("module disable")
    @Description("Disable a specific module")
    @CommandPermission("clearlaggenhanced.admin")
    @CommandCompletion("@modules")
    public void onModuleDisable(CommandSender sender, String moduleName) {
        if (plugin.getModuleManager().getModule(moduleName) == null) {
            Map<String, String> ph = new HashMap<>();
            ph.put("module", moduleName);
            sender.sendMessage(plugin.getLanguageManager().get("module.not_found", ph));
            return;
        }
        
        plugin.getModuleManager().disableModule(moduleName);
        Map<String, String> ph = new HashMap<>();
        ph.put("module", moduleName);
        sender.sendMessage(plugin.getLanguageManager().get("module.disabled", ph));
    }
}