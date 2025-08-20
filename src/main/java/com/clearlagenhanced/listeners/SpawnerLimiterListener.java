package com.clearlagenhanced.listeners;

import com.clearlagenhanced.ClearLaggEnhanced;
import com.clearlagenhanced.managers.ConfigManager;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.SpawnerSpawnEvent;

import java.util.HashSet;
import java.util.Set;

/**
 * Spawner limiter: slows down spawners by multiplying the next spawn delay
 * and cancels spawns if the chunk has reached the mob cap.
 */
public class SpawnerLimiterListener implements Listener {

    private final ClearLaggEnhanced plugin;
    private final ConfigManager config;
    private final Set<String> worldFilter = new HashSet<>();

    public SpawnerLimiterListener(ClearLaggEnhanced plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
        for (String w : config.getStringList("lag-prevention.spawner-limiter.worlds")) {
            worldFilter.add(w);
        }
    }

    private boolean enabled() {
        return config.getBoolean("lag-prevention.spawner-limiter.enabled", true);
    }

    private double multiplier() {
        return Math.max(1.0, config.getDouble("lag-prevention.spawner-limiter.spawn-delay-multiplier", 1.5));
    }

    private boolean isWorldAllowed(World world) {
        return worldFilter.isEmpty() || worldFilter.contains(world.getName());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpawnerSpawn(SpawnerSpawnEvent event) {
        if (!enabled()) return;

        CreatureSpawner spawner = event.getSpawner();
        if (!isWorldAllowed(spawner.getWorld())) return;

        // Respect mob limiter: if the chunk is at/over cap, cancel immediately
        Chunk chunk = event.getLocation().getChunk();
        if (plugin.getLagPreventionManager().isMobLimitReached(chunk)) {
            event.setCancelled(true);
            if (config.getBoolean("debug", false)) {
                plugin.getLogger().info("[SpawnerLimiter] Cancelled spawn due to mob cap at chunk " + chunk.getX() + "," + chunk.getZ());
            }
            return;
        }

        // Increase the NEXT spawn delay by multiplier
        try {
            int current = Math.max(1, spawner.getDelay());
            int newDelay = (int) Math.min((long) (current * multiplier()), 32767L);
            spawner.setDelay(newDelay);

            if (config.getBoolean("debug", false)) {
                plugin.getLogger().info("[SpawnerLimiter] Set next spawner delay to " + newDelay +
                        " at " + spawner.getWorld().getName() + " " + spawner.getX() + "," + spawner.getY() + "," + spawner.getZ());
            }
        } catch (Throwable t) {
            if (config.getBoolean("debug", false)) {
                plugin.getLogger().warning("[SpawnerLimiter] Failed to adjust spawner delay: " + t.getMessage());
            }
        }
    }
}
