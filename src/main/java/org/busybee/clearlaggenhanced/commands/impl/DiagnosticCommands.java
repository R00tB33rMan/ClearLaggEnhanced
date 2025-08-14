package org.busybee.clearlaggenhanced.commands.impl;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.busybee.clearlaggenhanced.ClearLaggEnhanced;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

/**
 * Diagnostic and monitoring commands
 */
@CommandAlias("clearlagg|cle")
public class DiagnosticCommands extends BaseCommand {
    
    private final ClearLaggEnhanced plugin;
    
    public DiagnosticCommands(ClearLaggEnhanced plugin) {
        this.plugin = plugin;
    }
    
    @Subcommand("tps")
    @Description("Show server TPS and MSPT")
    @CommandPermission("clearlaggenhanced.tps")
    public void onTps(CommandSender sender) {
        double[] tps = plugin.getServer().getTPS();
        
        sender.sendMessage(ChatColor.GREEN + "=== Server Performance ===");
        sender.sendMessage(ChatColor.YELLOW + "TPS (1m): " + formatTps(tps[0]));
        sender.sendMessage(ChatColor.YELLOW + "TPS (5m): " + formatTps(tps[1]));
        sender.sendMessage(ChatColor.YELLOW + "TPS (15m): " + formatTps(tps[2]));
        
        // MSPT calculation (approximation)
        double mspt = 1000.0 / tps[0];
        sender.sendMessage(ChatColor.YELLOW + "MSPT: " + formatMspt(mspt));
    }
    
    @Subcommand("memory")
    @Description("Show memory usage information")
    @CommandPermission("clearlaggenhanced.memory")
    public void onMemory(CommandSender sender) {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        sender.sendMessage(ChatColor.GREEN + "=== Memory Usage ===");
        sender.sendMessage(ChatColor.YELLOW + "Used: " + formatMemory(usedMemory) + " / " + formatMemory(maxMemory));
        sender.sendMessage(ChatColor.YELLOW + "Free: " + formatMemory(freeMemory));
        sender.sendMessage(ChatColor.YELLOW + "Usage: " + String.format("%.1f%%", (double) usedMemory / maxMemory * 100));
    }
    
    @Subcommand("check")
    @Description("Comprehensive server health check")
    @CommandPermission("clearlaggenhanced.check")
    public void onCheck(CommandSender sender) {
        sender.sendMessage(ChatColor.GREEN + "=== Server Health Check ===");
        
        // TPS check
        double tps = plugin.getServer().getTPS()[0];
        if (tps >= 19.5) {
            sender.sendMessage(ChatColor.GREEN + "✓ TPS: Excellent (" + String.format("%.2f", tps) + ")");
        } else if (tps >= 18.0) {
            sender.sendMessage(ChatColor.YELLOW + "⚠ TPS: Good (" + String.format("%.2f", tps) + ")");
        } else {
            sender.sendMessage(ChatColor.RED + "✗ TPS: Poor (" + String.format("%.2f", tps) + ")");
        }
        
        // Memory check
        Runtime runtime = Runtime.getRuntime();
        double memoryUsage = (double) (runtime.totalMemory() - runtime.freeMemory()) / runtime.maxMemory() * 100;
        if (memoryUsage < 70) {
            sender.sendMessage(ChatColor.GREEN + "✓ Memory: Good (" + String.format("%.1f%%", memoryUsage) + ")");
        } else if (memoryUsage < 85) {
            sender.sendMessage(ChatColor.YELLOW + "⚠ Memory: High (" + String.format("%.1f%%", memoryUsage) + ")");
        } else {
            sender.sendMessage(ChatColor.RED + "✗ Memory: Critical (" + String.format("%.1f%%", memoryUsage) + ")");
        }
        
        // Module status
        int enabledModules = plugin.getModuleManager().getEnabledModuleCount();
        sender.sendMessage(ChatColor.YELLOW + "Active Modules: " + enabledModules);
    }
    
    private String formatTps(double tps) {
        if (tps >= 19.5) {
            return ChatColor.GREEN + String.format("%.2f", tps);
        } else if (tps >= 18.0) {
            return ChatColor.YELLOW + String.format("%.2f", tps);
        } else {
            return ChatColor.RED + String.format("%.2f", tps);
        }
    }
    
    private String formatMspt(double mspt) {
        if (mspt <= 50) {
            return ChatColor.GREEN + String.format("%.2f ms", mspt);
        } else if (mspt <= 55) {
            return ChatColor.YELLOW + String.format("%.2f ms", mspt);
        } else {
            return ChatColor.RED + String.format("%.2f ms", mspt);
        }
    }
    
    private String formatMemory(long bytes) {
        if (bytes < 1024) return bytes + " B";
        else if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        else if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        else return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
}