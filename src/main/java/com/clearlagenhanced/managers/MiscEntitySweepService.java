package com.clearlagenhanced.managers;

import com.clearlagenhanced.ClearLaggEnhanced;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Periodic sweeper that enforces per-chunk caps for configured non-mob entity types.
 * Also exposes a throttled admin notification helper used by the proactive listener.
 */
public class MiscEntitySweepService {

    private final ClearLaggEnhanced plugin;
    private final Map<EntityType, Integer> caps;
    private final Set<String> worldFilter;
    private final boolean protectNamed;
    private final Set<String> protectedTags;
    private final int maxChunksPerTick;
    private final long intervalTicks;
    private final long notifyThrottleTicks;
    private final String adminPerm;

    private int taskId = -1;
    private int cursor = 0;
    private final Map<String, Long> lastNotifyTick = new HashMap<>(); // key: world:x,z:type

    public MiscEntitySweepService(ClearLaggEnhanced plugin, ConfigManager cfg) {
        this.plugin = plugin;

        // Load config
        this.caps = loadCaps(cfg);
        this.worldFilter = new HashSet<>(cfg.getStringList("lag-prevention.misc-entity-limiter.worlds"));
        this.protectNamed = cfg.getBoolean("lag-prevention.misc-entity-limiter.protect.named", true);
        this.protectedTags = new HashSet<>(cfg.getStringList("lag-prevention.misc-entity-limiter.protect.tags"));
        this.maxChunksPerTick = Math.max(1, cfg.getInt("lag-prevention.misc-entity-limiter.sweep.max-chunks-per-tick", 20));
        this.intervalTicks = Math.max(1, cfg.getInt("lag-prevention.misc-entity-limiter.sweep.interval-ticks", 100));
        this.adminPerm = cfg.getString("lag-prevention.misc-entity-limiter.notify.admins-permission", "CLE.admin");
        long throttleSeconds = Math.max(5, cfg.getInt("lag-prevention.misc-entity-limiter.notify.throttle-seconds", 60));
        this.notifyThrottleTicks = throttleSeconds * 20L;
    }

    private Map<EntityType, Integer> loadCaps(ConfigManager cfg) {
        Map<EntityType, Integer> map = new EnumMap<>(EntityType.class);
        var sec = cfg.getConfig().getConfigurationSection("lag-prevention.misc-entity-limiter.limits-per-chunk");
        if (sec != null) {
            for (String key : sec.getKeys(false)) {
                try {
                    EntityType type = EntityType.valueOf(key.toUpperCase(Locale.ROOT));
                    int cap = sec.getInt(key, -1);
                    if (cap >= 0) map.put(type, cap);
                } catch (IllegalArgumentException ignored) {}
            }
        }
        return map;
    }

    public void start() {
        if (taskId != -1) return;
        taskId = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this::tick, intervalTicks, intervalTicks);
    }

    public void shutdown() {
        if (taskId != -1) {
            plugin.getServer().getScheduler().cancelTask(taskId);
            taskId = -1;
        }
        lastNotifyTick.clear();
    }

    private boolean isWorldAllowed(World w) {
        return worldFilter.isEmpty() || worldFilter.contains(w.getName());
    }

    private boolean exempt(Entity e) {
        if (protectNamed && e.getCustomName() != null && !e.getCustomName().isEmpty()) return true;
        if (!protectedTags.isEmpty()) {
            for (String t : protectedTags) if (e.getScoreboardTags().contains(t)) return true;
        }
        return false;
    }

    private void tick() {
        List<World> worlds = plugin.getServer().getWorlds();
        List<Chunk> chunks = new ArrayList<>();
        for (World w : worlds) {
            if (!isWorldAllowed(w)) continue;
            chunks.addAll(Arrays.asList(w.getLoadedChunks()));
        }
        if (chunks.isEmpty()) return;

        int processed = 0;
        for (; processed < maxChunksPerTick && !chunks.isEmpty(); processed++) {
            if (cursor >= chunks.size()) cursor = 0;
            Chunk c = chunks.get(cursor++);
            enforceChunk(c);
        }
    }

    private void enforceChunk(Chunk chunk) {
        long now = chunk.getWorld().getFullTime();
        Map<EntityType, List<Entity>> byType = new EnumMap<>(EntityType.class);
        for (Entity e : chunk.getEntities()) {
            if (!caps.containsKey(e.getType())) continue;
            byType.computeIfAbsent(e.getType(), k -> new ArrayList<>()).add(e);
        }
        for (Map.Entry<EntityType, List<Entity>> entry : byType.entrySet()) {
            EntityType type = entry.getKey();
            int cap = caps.getOrDefault(type, -1);
            if (cap < 0) continue;
            List<Entity> list = entry.getValue();
            List<Entity> candidates = new ArrayList<>();
            for (Entity e : list) if (!exempt(e)) candidates.add(e);
            int over = candidates.size() - cap;
            if (over <= 0) continue;

            candidates.sort(this::compareForRemoval);

            int removed = 0;
            for (int i = 0; i < over && i < candidates.size(); i++) {
                Entity victim = candidates.get(i);
                victim.remove();
                removed++;
            }
            if (removed > 0) notifyAdmins(chunk, type, removed, false);
        }
    }

    private int compareForRemoval(Entity a, Entity b) {
        boolean aNamed = a.getCustomName() != null && !a.getCustomName().isEmpty();
        boolean bNamed = b.getCustomName() != null && !b.getCustomName().isEmpty();
        if (aNamed != bNamed) return aNamed ? 1 : -1; // unnamed first

        double ap = nearestPlayerDistSq(a);
        double bp = nearestPlayerDistSq(b);
        int cmp = Double.compare(bp, ap); // further first
        if (cmp != 0) return cmp;

        return Integer.compare(System.identityHashCode(a), System.identityHashCode(b));
    }

    private double nearestPlayerDistSq(Entity e) {
        double min = Double.POSITIVE_INFINITY;
        for (Player p : e.getWorld().getPlayers()) {
            double d = p.getLocation().distanceSquared(e.getLocation());
            if (d < min) min = d;
        }
        if (Double.isInfinite(min)) return 1.0E12;
        return min;
    }

    // Public so listener can reuse throttled notification path
    public void notifyAdmins(Chunk chunk, EntityType type, int count, boolean blocked) {
        long nowTick = chunk.getWorld().getFullTime();
        String nkey = chunk.getWorld().getName() + ":" + chunk.getX() + "," + chunk.getZ() + ":" + type.name();
        long last = lastNotifyTick.getOrDefault(nkey, 0L);
        if ((nowTick - last) < notifyThrottleTicks) return;
        lastNotifyTick.put(nkey, nowTick);

        java.util.Map<String, String> ph = new java.util.HashMap<>();
        ph.put("x", String.valueOf(chunk.getX()));
        ph.put("z", String.valueOf(chunk.getZ()));
        ph.put("type", type.name());
        ph.put("removed", String.valueOf(count));
        String msgKey = blocked ? "misc-entity-limiter.blocked" : "misc-entity-limiter.trimmed";

        var mm = plugin.getMessageManager();
        var component = mm.getMessage(msgKey, ph);
        plugin.getServer().getOnlinePlayers().stream()
                .filter(p -> p.hasPermission(adminPerm))
                .forEach(p -> p.sendMessage(component));
    }
}
