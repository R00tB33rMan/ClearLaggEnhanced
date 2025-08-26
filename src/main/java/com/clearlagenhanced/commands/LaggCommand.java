package com.clearlagenhanced.commands;

import com.clearlagenhanced.ClearLaggEnhanced;
import com.clearlagenhanced.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                    MessageUtils.sendMessage(sender, "notifications.no-permission");
                    return true;
                }
                showHelp(sender);
                break;
                
            case "clear":
                if (!sender.hasPermission("CLE.clear")) {
                    MessageUtils.sendMessage(sender, "notifications.no-permission");
                    return true;
                }
                handleClear(sender, args);
                break;
                
            case "next":
                if (!sender.hasPermission("CLE.next")) {
                    MessageUtils.sendMessage(sender, "notifications.no-permission");
                    return true;
                }
                handleNext(sender);
                break;
                
            case "tps":
                if (!sender.hasPermission("CLE.tps")) {
                    MessageUtils.sendMessage(sender, "notifications.no-permission");
                    return true;
                }
                handleTps(sender);
                break;
                
            case "ram":
                if (!sender.hasPermission("CLE.ram")) {
                    MessageUtils.sendMessage(sender, "notifications.no-permission");
                    return true;
                }
                handleRam(sender);
                break;
                
            case "chunkfinder":
                if (!sender.hasPermission("CLE.chunkfinder")) {
                    MessageUtils.sendMessage(sender, "notifications.no-permission");
                    return true;
                }
                handleChunkFinder(sender);
                break;
                
            case "admin":
                if (!sender.hasPermission("CLE.admin")) {
                    MessageUtils.sendMessage(sender, "notifications.no-permission");
                    return true;
                }
                handleAdmin(sender);
                break;
                
            case "reload":
                if (!sender.hasPermission("CLE.reload")) {
                    MessageUtils.sendMessage(sender, "notifications.no-permission");
                    return true;
                }
                handleReload(sender);
                break;
                
            default:
                Map<String, String> ph = new HashMap<>();
                ph.put("sub", subCommand);
                MessageUtils.sendMessage(sender, "commands.unknown-subcommand", ph);
                showHelp(sender);
                break;
        }
        
        return true;
    }
    
    private void showHelp(CommandSender sender) {
        MessageUtils.sendMessage(sender, "commands.help.header");
        MessageUtils.sendMessage(sender, "commands.help.clear");
        MessageUtils.sendMessage(sender, "commands.help.next");
        MessageUtils.sendMessage(sender, "commands.help.tps");
        MessageUtils.sendMessage(sender, "commands.help.ram");
        MessageUtils.sendMessage(sender, "commands.help.chunkfinder");
        MessageUtils.sendMessage(sender, "commands.help.admin");
        MessageUtils.sendMessage(sender, "commands.help.reload");
    }
    
    private void handleClear(CommandSender sender, String[] args) {
        MessageUtils.sendMessage(sender, "commands.clear.starting");

        Bukkit.getScheduler().runTask(plugin, () -> {
            long startTime = System.currentTimeMillis();
            int cleared = plugin.getEntityManager().clearEntities();
            long duration = System.currentTimeMillis() - startTime;

            Map<String, String> ph = new HashMap<>();
            ph.put("count", String.valueOf(cleared));
            ph.put("time", String.valueOf(duration));
            MessageUtils.sendMessage(sender, "notifications.clear-complete", ph);
        });
    }
    
    private void handleNext(CommandSender sender) {
        long timeUntilNext = plugin.getEntityManager().getTimeUntilNextClear();
        
        if (timeUntilNext == -1) {
            MessageUtils.sendMessage(sender, "next-clear.disabled");
            return;
        }
        
        String formattedTime = plugin.getEntityManager().getFormattedTimeUntilNextClear();
        Map<String, String> ph = new HashMap<>();
        ph.put("time", formattedTime);
        MessageUtils.sendMessage(sender, "next-clear.scheduled", ph);
    }
    
    private void handleTps(CommandSender sender) {
        double tps = plugin.getPerformanceManager().getTPS();
        Map<String, String> ph = new HashMap<>();
        ph.put("tps", String.format("%.2f", tps));
        MessageUtils.sendMessage(sender, "performance.tps", ph);
    }
    
    private void handleRam(CommandSender sender) {
        double memoryPercent = plugin.getPerformanceManager().getMemoryUsagePercentage();
        
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory() / 1024 / 1024;
        long totalMemory = runtime.totalMemory() / 1024 / 1024;
        long freeMemory = runtime.freeMemory() / 1024 / 1024;
        long usedMemory = totalMemory - freeMemory;
        
        MessageUtils.sendMessage(sender, "performance.ram.header");
        Map<String, String> usedPh = new HashMap<>();
        usedPh.put("used", String.valueOf(usedMemory));
        usedPh.put("percent", String.format("%.1f", memoryPercent));
        MessageUtils.sendMessage(sender, "performance.ram.used", usedPh);
        Map<String, String> totalPh = new HashMap<>();
        totalPh.put("total", String.valueOf(totalMemory));
        MessageUtils.sendMessage(sender, "performance.ram.total", totalPh);
        Map<String, String> maxPh = new HashMap<>();
        maxPh.put("max", String.valueOf(maxMemory));
        MessageUtils.sendMessage(sender, "performance.ram.max", maxPh);
    }
    
    private void handleChunkFinder(CommandSender sender) {
        if (!(sender instanceof Player)) {
            MessageUtils.sendMessage(sender, "errors.player-only");
            return;
        }
        
        Player player = (Player) sender;
        plugin.getPerformanceManager().findLaggyChunksAsync(player);
    }
    
    private void handleAdmin(CommandSender sender) {
        if (!(sender instanceof Player)) {
            MessageUtils.sendMessage(sender, "errors.player-only");
            return;
        }
        
        Player player = (Player) sender;
        plugin.getGUIManager().openMainGUI(player);
    }
    
    private void handleReload(CommandSender sender) {
        plugin.getConfigManager().reload();
        MessageUtils.sendMessage(sender, "notifications.reload-complete");
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
