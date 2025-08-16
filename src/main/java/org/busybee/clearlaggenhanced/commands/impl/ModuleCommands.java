package org.busybee.clearlaggenhanced.commands.impl;

import org.busybee.clearlaggenhanced.ClearLaggEnhanced;
import org.busybee.clearlaggenhanced.modules.PerformanceModule;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;

public class ModuleCommands {
    
    private final ClearLaggEnhanced plugin;
    
    public ModuleCommands(ClearLaggEnhanced plugin) {
        this.plugin = plugin;
    }
    
    public void onModules(CommandSender sender) {

        sender.sendMessage(plugin.getLanguageManager().get("ui.modules.header"));
        
        for (PerformanceModule module : plugin.getModuleManager().getAllModules()) {
            PerformanceModule.ModuleStats stats = module.getStats();
            
            String status = plugin.getModuleManager().isModuleEnabled(module.getName()) ?
                ChatColor.GREEN + "ENABLED" : ChatColor.RED + "DISABLED";
            
            sender.sendMessage(ChatColor.YELLOW + module.getName() + ChatColor.GRAY + " - " + status);
            sender.sendMessage(ChatColor.GRAY + "  Status: " + stats.status() + ChatColor.GRAY + ", Ops: " + stats.operations());
        }
    }
    
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
