package com.clearlagenhanced.listeners;

import com.clearlagenhanced.ClearLaggEnhanced;
import com.clearlagenhanced.managers.ConfigManager;
import com.tcoded.folialib.impl.PlatformScheduler;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Smart Redstone Limiter inspired by RedstoneLimiter plugin
 * Features:
 * - Block-level limiting (per individual block location)
 * - Chunk-level limiting (total activations per chunk)
 * - Time-based reset periods (configurable milliseconds)
 */
public class RedstoneLimiterListener implements Listener {

    private final ClearLaggEnhanced plugin;
    private final PlatformScheduler scheduler;
    private final Set<String> worldFilter = new HashSet<>();
    private WrappedTask resetTask;

    // Configuration
    private final boolean enabled;
    private final long resetPeriodMs;

    // Block-level limiting
    private final boolean blockLimitEnabled;
    private final Map<Material, Integer> blockThresholds = new HashMap<>();
    private final int globalBlockThreshold;
    private final Map<BlockKey, BlockData> blockActivations = new ConcurrentHashMap<>();

    // Chunk-level limiting
    private final boolean chunkLimitEnabled;
    private final int chunkThreshold;
    private final Map<ChunkKey, AtomicInteger> chunkActivations = new ConcurrentHashMap<>();

    // Piston push limit
    private final int maxPistonPush;

    // Tracking
    private long lastResetTime = System.currentTimeMillis();

    public RedstoneLimiterListener(@NotNull ClearLaggEnhanced plugin) {
        this.plugin = plugin;
        this.scheduler = ClearLaggEnhanced.scheduler();
        ConfigManager config = plugin.getConfigManager();

        // General settings
        this.enabled = config.getBoolean("lag-prevention.redstone-limiter.enabled", false);
        this.resetPeriodMs = config.getInt("lag-prevention.redstone-limiter.reset-period-ms", 3000);

        // Block-level settings
        this.blockLimitEnabled = config.getBoolean("lag-prevention.redstone-limiter.blocks.enabled", true);
        this.globalBlockThreshold = config.getInt("lag-prevention.redstone-limiter.blocks.threshold.GLOBAL", 2);

        // Load specific block thresholds
        Map<String, Object> thresholds = config.getConfigSection("lag-prevention.redstone-limiter.blocks.threshold");
        if (thresholds != null) {
            for (Map.Entry<String, Object> entry : thresholds.entrySet()) {
                if (entry.getKey().equals("GLOBAL")) continue;
                try {
                    Material material = Material.valueOf(entry.getKey().toUpperCase());
                    int threshold = Integer.parseInt(entry.getValue().toString());
                    blockThresholds.put(material, threshold);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid material in redstone limiter config: " + entry.getKey());
                }
            }
        }

        // Chunk-level settings
        this.chunkLimitEnabled = config.getBoolean("lag-prevention.redstone-limiter.chunks.enabled", true);
        this.chunkThreshold = config.getInt("lag-prevention.redstone-limiter.chunks.threshold", 1024);

        // Piston settings
        this.maxPistonPush = config.getInt("lag-prevention.redstone-limiter.max-piston-push", 12);

        // World filter
        worldFilter.addAll(config.getStringList("lag-prevention.redstone-limiter.worlds"));

        // Start reset task
        startResetTask();
    }

    private void startResetTask() {
        // Reset counters based on configured period
        long resetPeriodTicks = (resetPeriodMs / 50); // Convert ms to ticks
        this.resetTask = scheduler.runTimer(this::resetCounters, resetPeriodTicks, resetPeriodTicks);
    }

    private void resetCounters() {
        blockActivations.clear();
        chunkActivations.clear();
        lastResetTime = System.currentTimeMillis();
    }

    public void shutdown() {
        if (resetTask != null) {
            scheduler.cancelTask(resetTask);
            resetTask = null;
        }
    }

    private boolean isEnabled() {
        return enabled;
    }

    private boolean isWorldAllowed(@NotNull World world) {
        return worldFilter.isEmpty() || worldFilter.contains(world.getName());
    }

    private int getBlockThreshold(@NotNull Material material) {
        return blockThresholds.getOrDefault(material, globalBlockThreshold);
    }

    private boolean checkBlockLimit(@NotNull Block block) {
        if (!blockLimitEnabled) {
            return false; // Not exceeded
        }

        Material material = block.getType();
        int threshold = getBlockThreshold(material);
        if (threshold <= 0) {
            return false; // No limit
        }

        BlockKey key = new BlockKey(block.getLocation());
        BlockData data = blockActivations.computeIfAbsent(key, k -> new BlockData());

        int prev = data.count.getAndUpdate(v -> v < threshold ? v + 1 : v);
        return prev >= threshold;
    }

    private boolean checkChunkLimit(@NotNull Chunk chunk) {
        if (!chunkLimitEnabled) {
            return false; // Not exceeded
        }

        if (chunkThreshold <= 0) {
            return false; // No limit
        }

        ChunkKey key = new ChunkKey(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
        AtomicInteger counter = chunkActivations.computeIfAbsent(key, k -> new AtomicInteger(0));

        int prev = counter.getAndUpdate(v -> v < chunkThreshold ? v + 1 : v);
        return prev >= chunkThreshold;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onRedstoneChange(@NotNull BlockRedstoneEvent event) {
        if (!isEnabled()) return;

        Block block = event.getBlock();
        if (!isWorldAllowed(block.getWorld())) return;
        if (event.getNewCurrent() == event.getOldCurrent()) return;

        Chunk chunk = block.getChunk();

        // Check both block-level and chunk-level limits
        boolean blockExceeded = checkBlockLimit(block);
        boolean chunkExceeded = checkChunkLimit(chunk);

        if (blockExceeded || chunkExceeded) {
            event.setNewCurrent(event.getOldCurrent());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonExtend(@NotNull BlockPistonExtendEvent event) {
        if (!isEnabled()) return;

        Block block = event.getBlock();
        if (!isWorldAllowed(block.getWorld())) return;

        Chunk chunk = block.getChunk();

        // Check push limit
        if (maxPistonPush > 0 && event.getBlocks().size() > maxPistonPush) {
            event.setCancelled(true);
            return;
        }

        // Check redstone limits
        boolean blockExceeded = checkBlockLimit(block);
        boolean chunkExceeded = checkChunkLimit(chunk);

        if (blockExceeded || chunkExceeded) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonRetract(@NotNull BlockPistonRetractEvent event) {
        if (!isEnabled()) return;

        Block block = event.getBlock();
        if (!isWorldAllowed(block.getWorld())) return;

        Chunk chunk = block.getChunk();

        // Check push limit
        if (maxPistonPush > 0 && event.getBlocks().size() > maxPistonPush) {
            event.setCancelled(true);
            return;
        }

        // Check redstone limits
        boolean blockExceeded = checkBlockLimit(block);
        boolean chunkExceeded = checkChunkLimit(chunk);

        if (blockExceeded || chunkExceeded) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDispense(@NotNull BlockDispenseEvent event) {
        if (!isEnabled()) return;

        Block block = event.getBlock();
        if (!isWorldAllowed(block.getWorld())) return;

        Chunk chunk = block.getChunk();

        boolean blockExceeded = checkBlockLimit(block);
        boolean chunkExceeded = checkChunkLimit(chunk);

        if (blockExceeded || chunkExceeded) {
            event.setCancelled(true);
        }
    }

    // Block tracking key (unique per block location)
    private record BlockKey(@NotNull String world, int x, int y, int z) {
        BlockKey(@NotNull Location loc) {
            this(loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BlockKey k)) return false;
            return x == k.x && y == k.y && z == k.z && world.equals(k.world);
        }

        @Override
        public int hashCode() {
            return Objects.hash(world, x, y, z);
        }
    }

    // Chunk tracking key
    private record ChunkKey(@NotNull String world, int x, int z) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ChunkKey k)) return false;
            return x == k.x && z == k.z && world.equals(k.world);
        }

        @Override
        public int hashCode() {
            return Objects.hash(world, x, z);
        }
    }

    // Block activation data
    private static class BlockData {
        final AtomicInteger count = new AtomicInteger(0);
    }
}
