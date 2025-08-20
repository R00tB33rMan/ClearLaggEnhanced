package com.clearlagenhanced.commands;

import com.clearlagenhanced.ClearLaggEnhanced;
import com.clearlagenhanced.utils.MessageUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class LaggCommand implements CommandExecutor, TabCompleter {
    
    private final ClearLaggEnhanced plugin;
    public LaggCommand(ClearLaggEnhanced plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "help":
                if (!sender.hasPermission("CLE.help")) {
                    MessageUtils.sendMessage(sender, Component.text("You don't have permission to use this command!")
                            .color(NamedTextColor.RED));
                    return true;
                }
                showHelp(sender);
                break;
                
            case "clear":
                if (!sender.hasPermission("CLE.clear")) {
                    MessageUtils.sendMessage(sender, Component.text("You don't have permission to use this command!")
                            .color(NamedTextColor.RED));
                    return true;
                }
                handleClear(sender, args);
                break;
                
            case "next":
                if (!sender.hasPermission("CLE.next")) {
                    MessageUtils.sendMessage(sender, Component.text("You don't have permission to use this command!")
                            .color(NamedTextColor.RED));
                    return true;
                }
                handleNext(sender);
                break;
                
            case "tps":
                if (!sender.hasPermission("CLE.tps")) {
                    MessageUtils.sendMessage(sender, Component.text("You don't have permission to use this command!")
                            .color(NamedTextColor.RED));
                    return true;
                }
                handleTps(sender);
                break;
                
            case "ram":
                if (!sender.hasPermission("CLE.ram")) {
                    MessageUtils.sendMessage(sender, Component.text("You don't have permission to use this command!")
                            .color(NamedTextColor.RED));
                    return true;
                }
                handleRam(sender);
                break;
                
            case "chunkfinder":
                if (!sender.hasPermission("CLE.chunkfinder")) {
                    MessageUtils.sendMessage(sender, Component.text("You don't have permission to use this command!")
                            .color(NamedTextColor.RED));
                    return true;
                }
                handleChunkFinder(sender);
                break;
                
            case "admin":
                if (!sender.hasPermission("CLE.admin")) {
                    MessageUtils.sendMessage(sender, Component.text("You don't have permission to use this command!")
                            .color(NamedTextColor.RED));
                    return true;
                }
                handleAdmin(sender);
                break;
                
            case "reload":
                if (!sender.hasPermission("CLE.reload")) {
                    MessageUtils.sendMessage(sender, Component.text("You don't have permission to use this command!")
                            .color(NamedTextColor.RED));
                    return true;
                }
                handleReload(sender);
                break;
                
            default:
                MessageUtils.sendMessage(sender, Component.text("Unknown subcommand: " + subCommand)
                        .color(NamedTextColor.RED));
                showHelp(sender);
                break;
        }
        
        return true;
    }
    
    private void showHelp(CommandSender sender) {
        MessageUtils.sendMessage(sender, Component.text("=== ClearLaggEnhanced Help ===")
                .color(NamedTextColor.GOLD));
        MessageUtils.sendMessage(sender, Component.text("/lagg help - Show this help message")
                .color(NamedTextColor.YELLOW));
        MessageUtils.sendMessage(sender, Component.text("/lagg clear - Clear entities manually")
                .color(NamedTextColor.YELLOW));
        MessageUtils.sendMessage(sender, Component.text("/lagg next - Show time until next clear")
                .color(NamedTextColor.YELLOW));
        MessageUtils.sendMessage(sender, Component.text("/lagg tps - Show server TPS")
                .color(NamedTextColor.YELLOW));
        MessageUtils.sendMessage(sender, Component.text("/lagg ram - Show RAM usage")
                .color(NamedTextColor.YELLOW));
        MessageUtils.sendMessage(sender, Component.text("/lagg chunkfinder - Find laggy chunks")
                .color(NamedTextColor.YELLOW));
        MessageUtils.sendMessage(sender, Component.text("/lagg admin - Open admin GUI")
                .color(NamedTextColor.YELLOW));
        MessageUtils.sendMessage(sender, Component.text("/lagg reload - Reload configuration")
                .color(NamedTextColor.YELLOW));
    }
    
    private void handleClear(CommandSender sender, String[] args) {
        MessageUtils.sendMessage(sender, Component.text("Clearing entities...")
                .color(NamedTextColor.GREEN));

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            long startTime = System.currentTimeMillis();
            int cleared = plugin.getEntityManager().clearEntities();
            long duration = System.currentTimeMillis() - startTime;

            Bukkit.getScheduler().runTask(plugin, () -> {
                MessageUtils.sendMessage(sender, Component.text("Cleared " + cleared + " entities in " + duration + "ms")
                        .color(NamedTextColor.GREEN));
            });
        });
    }
    
    private void handleNext(CommandSender sender) {
        long timeUntilNext = plugin.getEntityManager().getTimeUntilNextClear();
        
        if (timeUntilNext == -1) {
            MessageUtils.sendMessage(sender, Component.text("Automatic entity clearing is disabled")
                    .color(NamedTextColor.YELLOW));
            return;
        }
        
        String formattedTime = plugin.getEntityManager().getFormattedTimeUntilNextClear();
        MessageUtils.sendMessage(sender, Component.text("Next automatic clear in: " + formattedTime)
                .color(NamedTextColor.GREEN));
    }
    
    private void handleTps(CommandSender sender) {
        double tps = plugin.getPerformanceManager().getTPS();
        NamedTextColor tpsColor = tps >= 18.0 ? NamedTextColor.GREEN : tps >= 15.0 ? NamedTextColor.YELLOW : NamedTextColor.RED;
        
        MessageUtils.sendMessage(sender, Component.text("Current TPS: " + String.format("%.2f", tps))
                .color(tpsColor));
    }
    
    private void handleRam(CommandSender sender) {
        String memoryUsage = plugin.getPerformanceManager().getFormattedMemoryUsage();
        double memoryPercent = plugin.getPerformanceManager().getMemoryUsagePercentage();
        
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory() / 1024 / 1024;
        long totalMemory = runtime.totalMemory() / 1024 / 1024;
        long freeMemory = runtime.freeMemory() / 1024 / 1024;
        long usedMemory = totalMemory - freeMemory;
        
        NamedTextColor memoryColor = memoryPercent < 70.0 ? NamedTextColor.GREEN : memoryPercent < 85.0 ? NamedTextColor.YELLOW : NamedTextColor.RED;
        
        MessageUtils.sendMessage(sender, Component.text("=== RAM Usage ===")
                .color(NamedTextColor.GOLD));
        MessageUtils.sendMessage(sender, Component.text("Used: " + usedMemory + " MB (" + String.format("%.1f", memoryPercent) + "%)")
                .color(memoryColor));
        MessageUtils.sendMessage(sender, Component.text("Total: " + totalMemory + " MB")
                .color(NamedTextColor.YELLOW));
        MessageUtils.sendMessage(sender, Component.text("Max: " + maxMemory + " MB")
                .color(NamedTextColor.YELLOW));
    }
    
    private void handleChunkFinder(CommandSender sender) {
        if (!(sender instanceof Player)) {
            MessageUtils.sendMessage(sender, Component.text("This command can only be used by players!")
                    .color(NamedTextColor.RED));
            return;
        }
        
        Player player = (Player) sender;
        plugin.getPerformanceManager().findLaggyChunksAsync(player);
    }
    
    private void handleAdmin(CommandSender sender) {
        if (!(sender instanceof Player)) {
            MessageUtils.sendMessage(sender, Component.text("This command can only be used by players!")
                    .color(NamedTextColor.RED));
            return;
        }
        
        Player player = (Player) sender;
        plugin.getGUIManager().openMainGUI(player);
    }
    
    private void handleReload(CommandSender sender) {
        plugin.getConfigManager().reload();
        MessageUtils.sendMessage(sender, Component.text("Configuration reloaded successfully!")
                .color(NamedTextColor.GREEN));
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            String[] subCommands = {"help", "clear", "next", "tps", "ram", "chunkfinder", "admin", "reload"};
            
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
