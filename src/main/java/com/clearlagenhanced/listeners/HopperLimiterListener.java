package com.clearlagenhanced.listeners;

import com.clearlagenhanced.ClearLaggEnhanced;
import com.clearlagenhanced.managers.ConfigManager;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Hopper;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Hopper limiter: throttles item transfers for hopper blocks and hopper minecarts.
 * Respects lag-prevention.hopper-limiter.enabled and transfer-cooldown (ticks) in config.yml.
 */
public class HopperLimiterListener implements Listener {

    private final ClearLaggEnhanced plugin;
    private final ConfigManager config;

    // current server tick since listener start
    private int tick = 0;
    private int taskId = -1;

    // Per-hopper last transfer tick
    private final Map<BlockKey, Integer> blockHopperLastTick = new HashMap<>();
    private final Map<UUID, Integer> minecartHopperLastTick = new HashMap<>();

    // Optional world scoping
    private final Set<String> worldFilter = new HashSet<>();

    public HopperLimiterListener(ClearLaggEnhanced plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();

        for (String w : config.getStringList("lag-prevention.hopper-limiter.worlds")) {
            worldFilter.add(w);
        }

        // increment our tick counter every tick and prune old entries periodically
        this.taskId = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            tick++;
            if ((tick & 63) == 0) { // every 64 ticks
                int cutoff = tick - Math.max(1, getCooldown());
                blockHopperLastTick.entrySet().removeIf(e -> e.getValue() < cutoff);
                minecartHopperLastTick.entrySet().removeIf(e -> e.getValue() < cutoff);
            }
        }, 1L, 1L);
    }

    public void shutdown() {
        if (taskId != -1) {
            plugin.getServer().getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }

    private boolean enabled() {
        return config.getBoolean("lag-prevention.hopper-limiter.enabled", true);
    }

    private int getCooldown() {
        return Math.max(1, config.getInt("lag-prevention.hopper-limiter.transfer-cooldown", 8));
    }

    private boolean isWorldAllowed(World world) {
        return worldFilter.isEmpty() || worldFilter.contains(world.getName());
    }

    private boolean shouldThrottle(InventoryHolder holder, Chunk chunk) {
        if (!enabled()) return false;
        if (!isWorldAllowed(chunk.getWorld())) return false;
        int cd = getCooldown();

        if (holder instanceof HopperMinecart) {
            HopperMinecart cart = (HopperMinecart) holder;
            UUID id = cart.getUniqueId();
            int last = minecartHopperLastTick.getOrDefault(id, Integer.MIN_VALUE);
            if (tick - last < cd) return true;
            minecartHopperLastTick.put(id, tick);
            return false;
        }

        if (holder instanceof Hopper) {
            Hopper hop = (Hopper) holder;
            Location l = hop.getLocation();
            BlockKey key = new BlockKey(l.getWorld().getName(), l.getBlockX(), l.getBlockY(), l.getBlockZ());
            int last = blockHopperLastTick.getOrDefault(key, Integer.MIN_VALUE);
            if (tick - last < cd) return true;
            blockHopperLastTick.put(key, tick);
            return false;
        }

        return false;
    }

    // Triggered when inventories move items (hopper push/pull)
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryMove(InventoryMoveItemEvent event) {
        Inventory initiatorInv = event.getInitiator();
        if (initiatorInv == null) return;

        InventoryHolder initiator = initiatorInv.getHolder();
        if (initiator == null) return;

        Chunk chunk = null;
        if (initiator instanceof HopperMinecart) {
            chunk = ((HopperMinecart) initiator).getLocation().getChunk();
        } else if (initiator instanceof Hopper) {
            chunk = ((Hopper) initiator).getLocation().getChunk();
        }
        if (chunk == null) return;

        if (shouldThrottle(initiator, chunk)) {
            event.setCancelled(true);
            if (config.getBoolean("debug", false)) {
                plugin.getLogger().info("[HopperLimiter] Cancelled hopper move at chunk " + chunk.getX() + "," + chunk.getZ());
            }
        }
    }

    // Triggered when an inventory (like a hopper) picks up a ground item
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryPickup(InventoryPickupItemEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof Hopper || holder instanceof HopperMinecart)) return;

        Chunk chunk;
        if (holder instanceof HopperMinecart) {
            chunk = ((HopperMinecart) holder).getLocation().getChunk();
        } else {
            Hopper hop = (Hopper) holder;
            chunk = hop.getLocation().getChunk();
        }

        if (shouldThrottle(holder, chunk)) {
            event.setCancelled(true);
            if (config.getBoolean("debug", false)) {
                plugin.getLogger().info("[HopperLimiter] Cancelled hopper pickup at chunk " + chunk.getX() + "," + chunk.getZ());
            }
        }
    }

    private static final class BlockKey {
        final String world;
        final int x, y, z;
        BlockKey(String world, int x, int y, int z) { this.world = world; this.x = x; this.y = y; this.z = z; }
        @Override public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof BlockKey)) return false; BlockKey k=(BlockKey)o; return x==k.x&&y==k.y&&z==k.z&&world.equals(k.world);}    
        @Override public int hashCode() { int r = world.hashCode(); r = 31*r + x; r = 31*r + y; r = 31*r + z; return r; }
    }
}
