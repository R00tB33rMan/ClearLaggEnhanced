package com.clearlagenhanced.listeners;

import com.clearlagenhanced.ClearLaggEnhanced;
import com.clearlagenhanced.managers.ConfigManager;
import com.clearlagenhanced.managers.MiscEntitySweepService;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Hanging;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

public class MiscEntityLimiterListener implements Listener {

    private final ClearLaggEnhanced plugin;
    private final ConfigManager cfg;
    private final boolean enabled;
    private final Map<EntityType, Integer> caps;
    private final Set<String> worldFilter;
    private final boolean protectNamed;
    private final Set<String> protectedTags;
    private final MiscEntitySweepService notifier;

    public MiscEntityLimiterListener(ClearLaggEnhanced plugin, MiscEntitySweepService notifier) {
        this.plugin = plugin;
        this.cfg = plugin.getConfigManager();
        this.notifier = notifier;

        enabled = cfg.getBoolean("lag-prevention.misc-entity-limiter.enabled", true);
        protectNamed = cfg.getBoolean("lag-prevention.misc-entity-limiter.protect.named", true);
        protectedTags = new HashSet<>(cfg.getStringList("lag-prevention.misc-entity-limiter.protect.tags"));
        worldFilter = new HashSet<>(cfg.getStringList("lag-prevention.misc-entity-limiter.worlds"));

        caps = new EnumMap<>(EntityType.class);
        loadCaps(cfg.getConfig().getConfigurationSection("lag-prevention.misc-entity-limiter.limits-per-chunk"));
    }

    private void loadCaps(ConfigurationSection sec) {
        if (sec == null) return;
        for (String key : sec.getKeys(false)) {
            try {
                EntityType type = EntityType.valueOf(key.toUpperCase(Locale.ROOT));
                int cap = sec.getInt(key, -1);
                if (cap >= 0) caps.put(type, cap);
            } catch (IllegalArgumentException ignored) {}
        }
    }

    private boolean isWorldAllowed(World w) {
        return worldFilter.isEmpty() || worldFilter.contains(w.getName());
    }

    private boolean exempt(Entity e) {
        if (protectNamed && e.getCustomName() != null && !e.getCustomName().isEmpty()) return true;
        if (!protectedTags.isEmpty()) {
            for (String t : protectedTags) {
                if (e.getScoreboardTags().contains(t)) return true;
            }
        }
        return false;
    }

    private boolean overCapIfAdded(Chunk chunk, EntityType type) {
        Integer cap = caps.get(type);
        if (cap == null || cap < 0) return false;
        int count = 0;
        for (Entity e : chunk.getEntities()) {
            if (e.getType() == type) count++;
        }
        return count + 1 > cap;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (!enabled) return;
        Entity e = event.getEntity();
        if (!caps.containsKey(e.getType())) return;
        if (!isWorldAllowed(e.getWorld())) return;
        if (exempt(e)) return;
        if (overCapIfAdded(e.getLocation().getChunk(), e.getType())) {
            event.setCancelled(true);
            if (notifier != null) notifier.notifyAdmins(event.getLocation().getChunk(), e.getType(), 0, true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHangingPlace(HangingPlaceEvent event) {
        if (!enabled) return;
        Hanging h = event.getEntity();
        if (!caps.containsKey(h.getType())) return;
        if (!isWorldAllowed(h.getWorld())) return;
        if (overCapIfAdded(h.getLocation().getChunk(), h.getType())) {
            event.setCancelled(true);
            if (notifier != null) notifier.notifyAdmins(h.getLocation().getChunk(), h.getType(), 0, true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onVehicleCreate(VehicleCreateEvent event) {
        if (!enabled) return;
        Entity e = event.getVehicle();
        if (!caps.containsKey(e.getType())) return;
        if (!isWorldAllowed(e.getWorld())) return;
        if (overCapIfAdded(e.getLocation().getChunk(), e.getType())) {
            Bukkit.getScheduler().runTask(plugin, e::remove);
            if (notifier != null) notifier.notifyAdmins(e.getLocation().getChunk(), e.getType(), 1, false);
        }
    }
}
