package com.clearlagenhanced.listeners;

import com.clearlagenhanced.ClearLaggEnhanced;
import com.clearlagenhanced.managers.LagPreventionManager;
import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;

public class MobLimiterListener implements Listener {

    private final ClearLaggEnhanced plugin;
    private final LagPreventionManager limiter;
    private final boolean debug;

    public MobLimiterListener(ClearLaggEnhanced plugin) {
        this.plugin = plugin;
        this.limiter = plugin.getLagPreventionManager();
        this.debug = plugin.getConfigManager().getBoolean("debug", false);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        LivingEntity entity = event.getEntity();
        if (!isCountable(entity)) return;

        Chunk chunk = entity.getLocation().getChunk();
        if (limiter.isMobLimitReached(chunk)) {
            event.setCancelled(true);
            if (debug) {
                java.util.Map<String, String> ph = new java.util.HashMap<>();
                ph.put("type", entity.getType().name());
                ph.put("x", String.valueOf(chunk.getX()));
                ph.put("z", String.valueOf(chunk.getZ()));
                var comp = plugin.getMessageManager().getMessage("debug.mob-limiter.cancelled", ph);
                plugin.getServer().getOnlinePlayers().stream()
                        .filter(p -> p.hasPermission("CLE.admin"))
                        .forEach(p -> p.sendMessage(comp));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpawnerSpawn(SpawnerSpawnEvent event) {
        Entity entity = event.getEntity();
        if (!isCountable(entity)) return;

        Chunk chunk = entity.getLocation().getChunk();
        if (limiter.isMobLimitReached(chunk)) {
            event.setCancelled(true);
            if (debug) {
                java.util.Map<String, String> ph = new java.util.HashMap<>();
                ph.put("type", entity.getType().name());
                ph.put("x", String.valueOf(chunk.getX()));
                ph.put("z", String.valueOf(chunk.getZ()));
                var comp = plugin.getMessageManager().getMessage("debug.mob-limiter.cancelled", ph);
                plugin.getServer().getOnlinePlayers().stream()
                        .filter(p -> p.hasPermission("CLE.admin"))
                        .forEach(p -> p.sendMessage(comp));
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCreatureSpawnMonitor(CreatureSpawnEvent event) {
        final Chunk chunk = event.getEntity().getLocation().getChunk();
        limiter.optimizeChunk(chunk);
    }

    private boolean isCountable(Entity entity) {
        if (!(entity instanceof LivingEntity)) return false;
        return entity.getType() != EntityType.PLAYER;
    }

    private void debugAdmins(String message) {
        plugin.getServer().getOnlinePlayers().stream()
                .filter(p -> p.hasPermission("CLE.admin"))
                .forEach(p -> p.sendMessage(message));
    }
}
