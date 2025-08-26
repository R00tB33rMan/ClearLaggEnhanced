package com.clearlagenhanced.managers;

import com.clearlagenhanced.ClearLaggEnhanced;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Tameable;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class EntityManager {
    
    private final ClearLaggEnhanced plugin;
    private final ConfigManager configManager;
    private BukkitTask clearTask;
    private long nextClearTime;
    private int clearInterval;
    
    public EntityManager(ClearLaggEnhanced plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        startAutoClearTask();
    }
    
    private void startAutoClearTask() {
        if (!configManager.getBoolean("entity-clearing.enabled", true)) {
            return;
        }
        
        clearInterval = configManager.getInt("entity-clearing.interval", 300); // Keep in seconds
        int intervalTicks = clearInterval * 20; // Convert to ticks for scheduler

        nextClearTime = System.currentTimeMillis() + (clearInterval * 1000);
        
        clearTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            nextClearTime = System.currentTimeMillis() + (clearInterval * 1000);
            plugin.getNotificationManager().sendClearWarnings();
        }, intervalTicks, intervalTicks);
        
        plugin.getLogger().info("Entity clearing task started with interval: " + clearInterval + " seconds");
    }

    public void restartTask() {
        if (clearTask != null) {
            clearTask.cancel();
            clearTask = null;
        }

        startAutoClearTask();
    }
    
    public int clearEntities() {
        if (Bukkit.isPrimaryThread()) {
            int cleared = clearEntitiesSync();
            if (clearTask != null) {
                nextClearTime = System.currentTimeMillis() + (clearInterval * 1000);
            }
            plugin.getLogger().info("Cleared " + cleared + " entities");
            return cleared;
        } else {
            CompletableFuture<Integer> future = new CompletableFuture<>();
            Bukkit.getScheduler().runTask(plugin, () -> {
                int cleared = clearEntitiesSync();
                if (clearTask != null) {
                    nextClearTime = System.currentTimeMillis() + (clearInterval * 1000);
                }
                plugin.getLogger().info("Cleared " + cleared + " entities");
                future.complete(cleared);
            });
            return future.join();
        }
    }

    private int clearEntitiesSync() {
        int cleared = 0;

        List<String> blacklist = configManager.getStringList("entity-clearing.blacklist");
        List<String> whitelist = configManager.getStringList("entity-clearing.whitelist");
        List<String> worlds = configManager.getStringList("entity-clearing.worlds");

        for (World world : Bukkit.getWorlds()) {
            if (!worlds.isEmpty() && !worlds.contains(world.getName())) {
                continue;
            }

            for (Entity entity : world.getEntities()) {
                if (shouldClearEntity(entity, blacklist, whitelist)) {
                    entity.remove();
                    cleared++;
                }
            }
        }

        return cleared;
    }
    
    private boolean shouldClearEntity(Entity entity, List<String> blacklist, List<String> whitelist) {
        EntityType type = entity.getType();
        String typeName = type.name();

        if (type == EntityType.PLAYER) {
            return false;
        }

        if (whitelist.contains(typeName)) {
            return false;
        }

        if (configManager.getBoolean("entity-clearing.protect-named-entities", true) && entity.getCustomName() != null) {
            return false;
        }

        if (configManager.getBoolean("entity-clearing.protect-tamed-entities", true) && entity instanceof Tameable) {
            Tameable tameable = (Tameable) entity;
            if (tameable.isTamed()) {
                return false;
            }
        }

        if (blacklist.contains(typeName)) {
            return true;
        }

        return !(entity instanceof LivingEntity);
    }

    public long getTimeUntilNextClear() {
        if (clearTask == null || !configManager.getBoolean("entity-clearing.enabled", true)) {
            return -1;
        }
        
        long currentTime = System.currentTimeMillis();
        long timeUntil = (nextClearTime - currentTime) / 1000;
        
        return Math.max(0, timeUntil);
    }

    public String getFormattedTimeUntilNextClear() {
        long seconds = getTimeUntilNextClear();
        
        if (seconds == -1) {
            return "Auto-clear disabled";
        }
        
        if (seconds == 0) {
            return "Any moment now";
        }
        
        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;
        
        if (minutes > 0) {
            return String.format("%dm %ds", minutes, remainingSeconds);
        } else {
            return String.format("%ds", seconds);
        }
    }
    
    public void shutdown() {
        if (clearTask != null) {
            clearTask.cancel();
            plugin.getLogger().info("Entity clearing task stopped");
        }
    }
}
