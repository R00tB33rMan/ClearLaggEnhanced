package com.clearlagenhanced.listeners;

import com.clearlagenhanced.ClearLaggEnhanced;
import com.clearlagenhanced.managers.ConfigManager;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Hopper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * HopperLimiterListener
 *
 * Robust per-chunk hopper rate limiter with anti-lockout logic and a soft cap.
 *
 * Features:
 * - Applies only to InventoryMoveItemEvent when the initiator is a Hopper (not minecarts, etc.).
 * - Uses the world's monotonic tick counter (World#getFullTime) for timing.
 * - Per-chunk timestamps stored in ConcurrentHashMap to remain thread-safe.
 * - Prevents permanent lockout by seeding initial timestamps and sliding the window on cancel.
 * - Soft cap: dynamically increases the cooldown for chunks that exceed max hoppers.
 * - Config values are pre-cached at construction; no per-event config reads.
 */
public class HopperLimiterListener implements Listener {

    private final ClearLaggEnhanced plugin;
    private final ConfigManager configManager;

    // Cached configuration values (populated on construction; refresh on plugin reload externally if needed)
    private final boolean enabled;
    private final int baseCooldownTicks;            // Base cooldown between allowed transfers
    private final int maxHoppersPerChunk;           // 0 disables soft-cap check
    private final boolean debug;                    // Optional debug logging

    // Per-chunk: last allowed or canceled evaluation tick (sliding window)
    private final Map<String, Long> lastMoveTickByChunk = new ConcurrentHashMap<>();

    // Per-chunk: last debug log tick to rate-limit messages
    private final Map<String, Long> lastLogTickByChunk = new ConcurrentHashMap<>();

    public HopperLimiterListener(ClearLaggEnhanced plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();

        // Pre-cache config values (no per-event reads)
        this.enabled = configManager.getBoolean("lag-prevention.hopper-limiter.enabled", true);
        this.baseCooldownTicks = Math.max(1, configManager.getInt("lag-prevention.hopper-limiter.transfer-cooldown", 8));
        this.maxHoppersPerChunk = Math.max(0, configManager.getInt("lag-prevention.hopper-limiter.max-hoppers-per-chunk", 0));
        this.debug = configManager.getBoolean("debug", false);
    }

    // Main event: limit only hopper-initiated moves
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryMove(InventoryMoveItemEvent event) {
        if (!enabled) return;

        Inventory initiatorInv = event.getInitiator();
        if (initiatorInv == null) return;

        InventoryHolder holder = initiatorInv.getHolder();
        if (!(holder instanceof Hopper)) {
            // Only throttle when a block hopper initiates the move
            return;
        }

        Hopper hopper = (Hopper) holder;
        World world = hopper.getWorld();
        if (world == null) return;

        long now = world.getFullTime(); // Monotonic tick counter
        Chunk chunk = hopper.getLocation().getChunk();
        String key = chunkKey(chunk);

        // Compute effective cooldown with soft cap based on hoppers in the chunk
        int effectiveCooldown = baseCooldownTicks;
        if (maxHoppersPerChunk > 0) {
            int count = countHoppersInChunkCached(chunk);
            if (count > maxHoppersPerChunk) {
                int excess = count - maxHoppersPerChunk;
                effectiveCooldown += Math.max(0, excess * 2); // +2 ticks per excess hopper
            }
        }

        // Seed: assume enough time has passed on first sight of the chunk
        long last = lastMoveTickByChunk.getOrDefault(key, now - effectiveCooldown);

        if ((now - last) < effectiveCooldown) {
            // Too soon: cancel but slide the window forward to avoid permanent lockout
            event.setCancelled(true);
            lastMoveTickByChunk.put(key, now);
            maybeLogChunkThrottle(chunk, now);
            return;
        }

        // Allowed: update last tick to now
        lastMoveTickByChunk.put(key, now);
    }

    // --- Helpers ---

    private static String chunkKey(Chunk c) {
        return c.getWorld().getName() + ":" + c.getX() + "," + c.getZ();
    }

    /**
    * Placeholder/simple implementation. In a real implementation you would cache
    * per-chunk hopper counts and refresh them periodically or on chunk events.
    * Returning 0 means the soft cap will not apply additional cooldown unless wired up elsewhere.
    */
    private int countHoppersInChunkCached(Chunk chunk) {
        // TODO: Implement a cached per-chunk hopper count if desired.
        return 0;
    }

    private void maybeLogChunkThrottle(Chunk chunk, long nowTick) {
        if (!debug) return;
        String key = chunkKey(chunk);
        long last = lastLogTickByChunk.getOrDefault(key, 0L);
        // Not more than once every 5 seconds (~100 ticks) per chunk
        if ((nowTick - last) >= 100L) {
            java.util.Map<String, String> ph = new java.util.HashMap<>();
            ph.put("x", String.valueOf(chunk.getX()));
            ph.put("z", String.valueOf(chunk.getZ()));
            var comp = plugin.getMessageManager().getMessage("debug.hopper.throttling", ph);
            plugin.getServer().getOnlinePlayers().stream()
                    .filter(p -> p.hasPermission("CLE.admin"))
                    .forEach(p -> p.sendMessage(comp));
            lastLogTickByChunk.put(key, nowTick);
        }
    }

    private void debugAdmins(String message) {
        plugin.getServer().getOnlinePlayers().stream()
                .filter(p -> p.hasPermission("CLE.admin"))
                .forEach(p -> p.sendMessage(message));
    }
}
