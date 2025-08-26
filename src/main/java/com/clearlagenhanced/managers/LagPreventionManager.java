package com.clearlagenhanced.managers;

import com.clearlagenhanced.ClearLaggEnhanced;
import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public class LagPreventionManager {

    private final ClearLaggEnhanced plugin;
    private final ConfigManager configManager;
    private final boolean mobLimiterEnabled;
    private final int maxMobsPerChunk;
    private final boolean hopperLimiterEnabled;
    private final boolean redstoneLimiterEnabled;

    public LagPreventionManager(ClearLaggEnhanced plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.mobLimiterEnabled = configManager.getBoolean("lag-prevention.mob-limiter.enabled", true);
        this.maxMobsPerChunk = configManager.getInt("lag-prevention.mob-limiter.max-mobs-per-chunk", 50);
        this.hopperLimiterEnabled = configManager.getBoolean("lag-prevention.hopper-limiter.enabled", true);
        this.redstoneLimiterEnabled = configManager.getBoolean("lag-prevention.redstone-limiter.enabled", true);
    }

    public boolean isMobLimitReached(Chunk chunk) {
        if (!mobLimiterEnabled) return false;

        int mobCount = 0;
        for (Entity entity : chunk.getEntities()) {
            if (entity instanceof org.bukkit.entity.LivingEntity && entity.getType() != EntityType.PLAYER) {
                mobCount++;
            }
        }
        return mobCount >= maxMobsPerChunk;
    }

    public boolean isRedstoneExcessive(Chunk chunk) {
        if (!redstoneLimiterEnabled) return false;
        return false;
    }

    public boolean areHoppersExcessive(Chunk chunk) {
        if (!hopperLimiterEnabled) return false;
        return false;
    }

    public void optimizeChunk(Chunk chunk) {
        if (!isMobLimitReached(chunk)) {
            return;
        }

        int livingCount = 0;
        for (Entity e : chunk.getEntities()) {
            if (e instanceof org.bukkit.entity.LivingEntity && e.getType() != EntityType.PLAYER) {
                livingCount++;
            }
        }

        int over = livingCount - maxMobsPerChunk;
        if (over <= 0) {
            return;
        }

        for (Entity entity : chunk.getEntities()) {
            if (over <= 0) break;

            if (entity instanceof org.bukkit.entity.LivingEntity
                    && entity.getType() != EntityType.PLAYER
                    && entity.getCustomName() == null) {
                entity.remove();
                over--;
            }
        }
    }
}
