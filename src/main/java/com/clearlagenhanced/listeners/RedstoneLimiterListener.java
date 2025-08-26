package com.clearlagenhanced.listeners;

import com.clearlagenhanced.ClearLaggEnhanced;
import com.clearlagenhanced.managers.ConfigManager;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockRedstoneEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Redstone limiter that enforces a per-chunk, per-tick budget for redstone activity.
 * It neutralizes redstone current changes and cancels piston/dispense events when
 * the budget is exceeded, as configured in config.yml under lag-prevention.redstone-limiter.
 */
public class RedstoneLimiterListener implements Listener {

    // Costs allow weighting heavier actions more than simple current changes
    private static final int COST_REDSTONE_CHANGE = 1;
    private static final int COST_PISTON = 4;
    private static final int COST_DISPENSE = 2;

    private final ClearLaggEnhanced plugin;
    private final ConfigManager config;
    private final Map<ChunkKey, Integer> creditsUsed = new HashMap<>();
    private final Set<String> worldFilter = new HashSet<>();
    private int taskId = -1;

    // Cached values to avoid per-event config accesses
    private final boolean enabled;
    private final int maxPerChunk;
    private final boolean debug;

    public RedstoneLimiterListener(ClearLaggEnhanced plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();

        // Cache flags
        this.enabled = config.getBoolean("lag-prevention.redstone-limiter.enabled", true);
        this.maxPerChunk = config.getInt("lag-prevention.redstone-limiter.max-redstone-per-chunk", 100);
        this.debug = config.getBoolean("debug", false);

        // Optional list of worlds to apply limiter to (empty = all worlds)
        for (String w : config.getStringList("lag-prevention.redstone-limiter.worlds")) {
            worldFilter.add(w);
        }

        // Reset per-chunk counters every tick
        this.taskId = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(
                plugin, creditsUsed::clear, 1L, 1L
        );
    }

    public void shutdown() {
        if (taskId != -1) {
            plugin.getServer().getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }

    private boolean enabled() {
        return enabled;
    }

    private int maxPerChunk() {
        return maxPerChunk;
    }

    private boolean isWorldAllowed(World world) {
        return worldFilter.isEmpty() || worldFilter.contains(world.getName());
    }

    private ChunkKey key(Chunk chunk) {
        return new ChunkKey(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
    }

    /**
     * Returns true if the budget has already been exceeded (or becomes exceeded by this cost).
     * Also increments the usage counter when appropriate.
     */
    private boolean budgetExceeded(Chunk chunk, int cost) {
        if (!enabled()) return false;
        if (!isWorldAllowed(chunk.getWorld())) return false;

        ChunkKey key = key(chunk);
        int used = creditsUsed.getOrDefault(key, 0);
        int max = maxPerChunk();
        if (used >= max) {
            return true;
        }
        int newUsed = used + cost;
        creditsUsed.put(key, newUsed);
        return newUsed > max;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onRedstoneChange(BlockRedstoneEvent event) {
        Block block = event.getBlock();
        Chunk chunk = block.getChunk();
        // Skip if no actual change
        if (event.getNewCurrent() == event.getOldCurrent()) return;

        boolean exceeded = budgetExceeded(chunk, COST_REDSTONE_CHANGE);
        if (exceeded) {
            // Neutralize the redstone change by restoring old current
            event.setNewCurrent(event.getOldCurrent());
            if (debug) {
                java.util.Map<String, String> ph = new java.util.HashMap<>();
                ph.put("world", block.getWorld().getName());
                ph.put("x", String.valueOf(block.getLocation().getBlockX()));
                ph.put("y", String.valueOf(block.getLocation().getBlockY()));
                ph.put("z", String.valueOf(block.getLocation().getBlockZ()));
                ph.put("cx", String.valueOf(chunk.getX()));
                ph.put("cz", String.valueOf(chunk.getZ()));
                var comp = plugin.getMessageManager().getMessage("debug.redstone.neutralized", ph);
                plugin.getServer().getOnlinePlayers().stream()
                        .filter(p -> p.hasPermission("CLE.admin"))
                        .forEach(p -> p.sendMessage(comp));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        Chunk chunk = event.getBlock().getChunk();
        boolean exceeded = budgetExceeded(chunk, COST_PISTON);
        if (exceeded) {
            event.setCancelled(true);
            if (debug) {
                java.util.Map<String, String> ph = new java.util.HashMap<>();
                ph.put("world", event.getBlock().getWorld().getName());
                ph.put("x", String.valueOf(event.getBlock().getLocation().getBlockX()));
                ph.put("y", String.valueOf(event.getBlock().getLocation().getBlockY()));
                ph.put("z", String.valueOf(event.getBlock().getLocation().getBlockZ()));
                ph.put("cx", String.valueOf(chunk.getX()));
                ph.put("cz", String.valueOf(chunk.getZ()));
                var comp = plugin.getMessageManager().getMessage("debug.redstone.piston-extend", ph);
                plugin.getServer().getOnlinePlayers().stream()
                        .filter(p -> p.hasPermission("CLE.admin"))
                        .forEach(p -> p.sendMessage(comp));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        Chunk chunk = event.getBlock().getChunk();
        boolean exceeded = budgetExceeded(chunk, COST_PISTON);
        if (exceeded) {
            event.setCancelled(true);
            if (debug) {
                java.util.Map<String, String> ph = new java.util.HashMap<>();
                ph.put("world", event.getBlock().getWorld().getName());
                ph.put("x", String.valueOf(event.getBlock().getLocation().getBlockX()));
                ph.put("y", String.valueOf(event.getBlock().getLocation().getBlockY()));
                ph.put("z", String.valueOf(event.getBlock().getLocation().getBlockZ()));
                ph.put("cx", String.valueOf(chunk.getX()));
                ph.put("cz", String.valueOf(chunk.getZ()));
                var comp = plugin.getMessageManager().getMessage("debug.redstone.piston-retract", ph);
                plugin.getServer().getOnlinePlayers().stream()
                        .filter(p -> p.hasPermission("CLE.admin"))
                        .forEach(p -> p.sendMessage(comp));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDispense(BlockDispenseEvent event) {
        Chunk chunk = event.getBlock().getChunk();
        boolean exceeded = budgetExceeded(chunk, COST_DISPENSE);
        if (exceeded) {
            event.setCancelled(true);
            if (debug) {
                java.util.Map<String, String> ph = new java.util.HashMap<>();
                ph.put("world", event.getBlock().getWorld().getName());
                ph.put("x", String.valueOf(event.getBlock().getLocation().getBlockX()));
                ph.put("y", String.valueOf(event.getBlock().getLocation().getBlockY()));
                ph.put("z", String.valueOf(event.getBlock().getLocation().getBlockZ()));
                ph.put("cx", String.valueOf(chunk.getX()));
                ph.put("cz", String.valueOf(chunk.getZ()));
                var comp = plugin.getMessageManager().getMessage("debug.redstone.dispense", ph);
                plugin.getServer().getOnlinePlayers().stream()
                        .filter(p -> p.hasPermission("CLE.admin"))
                        .forEach(p -> p.sendMessage(comp));
            }
        }
    }

    private static String loc(Location l) {
        return l.getWorld().getName() + " " + l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ();
    }

    private void debugAdmins(String message) {
        plugin.getServer().getOnlinePlayers().stream()
                .filter(p -> p.hasPermission("CLE.admin"))
                .forEach(p -> p.sendMessage(message));
    }

    private static final class ChunkKey {
        final String world;
        final int x;
        final int z;

        ChunkKey(String world, int x, int z) {
            this.world = world;
            this.x = x;
            this.z = z;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ChunkKey)) return false;
            ChunkKey k = (ChunkKey) o;
            return x == k.x && z == k.z && world.equals(k.world);
        }

        @Override
        public int hashCode() {
            int result = world.hashCode();
            result = 31 * result + x;
            result = 31 * result + z;
            return result;
        }
    }
}
