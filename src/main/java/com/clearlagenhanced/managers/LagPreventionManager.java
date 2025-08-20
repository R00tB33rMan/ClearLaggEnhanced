package com.clearlagenhanced.managers;

import com.clearlagenhanced.ClearLaggEnhanced;
import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public class LagPreventionManager {
    
    private final ClearLaggEnhanced plugin;
    private final ConfigManager configManager;
    
    public LagPreventionManager(ClearLaggEnhanced plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
    }
    
    public boolean isMobLimitReached(Chunk chunk) {
        if (!configManager.getBoolean("lag-prevention.mob-limiter.enabled", true)) {
            return false;
        }
        
        int maxMobs = configManager.getInt("lag-prevention.mob-limiter.max-mobs-per-chunk", 50);
        int mobCount = 0;
        
        for (Entity entity : chunk.getEntities()) {
            if (entity.getType() != EntityType.PLAYER && entity.getType() != EntityType.ITEM) {
                mobCount++;
            }
        }
        
        return mobCount >= maxMobs;
    }
    
    public boolean isRedstoneExcessive(Chunk chunk) {
        if (!configManager.getBoolean("lag-prevention.redstone-limiter.enabled", true)) {
            return false;
        }

        // This would need more complex implementation to detect redstone circuits
        // For now, return false as placeholder
        return false;
    }
    
    public boolean areHoppersExcessive(Chunk chunk) {
        if (!configManager.getBoolean("lag-prevention.hopper-limiter.enabled", true)) {
            return false;
        }
        
        // This would need block scanning implementation
        // For now, return false as placeholder
        return false;
    }
    
    public void optimizeChunk(Chunk chunk) {
        if (isMobLimitReached(chunk)) {
            int removed = 0;
            int maxMobs = configManager.getInt("lag-prevention.mob-limiter.max-mobs-per-chunk", 50);
            
            for (Entity entity : chunk.getEntities()) {
                if (removed >= (chunk.getEntities().length - maxMobs)) {
                    break;
                }
                
                if (entity.getType() != EntityType.PLAYER && 
                    entity.getCustomName() == null) {
                    entity.remove();
                    removed++;
                }
            }
        }
    }
}
