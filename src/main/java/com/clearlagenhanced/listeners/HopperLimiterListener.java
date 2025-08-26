package com.clearlagenhanced.listeners;

import com.clearlagenhanced.ClearLaggEnhanced;
import com.clearlagenhanced.managers.ConfigManager;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Hopper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HopperLimiterListener implements Listener {

    private final ClearLaggEnhanced plugin;
    private final ConfigManager configManager;

    private final boolean enabled;
    private final int baseCooldownTicks;
    private final int maxHoppersPerChunk;
    private final boolean debug;
    private final Map<String, Long> lastMoveTickByChunk = new ConcurrentHashMap<>();
    private final Map<String, Long> lastLogTickByChunk = new ConcurrentHashMap<>();
    private final Map<String, Integer> hopperCountByChunk = new ConcurrentHashMap<>();

    public HopperLimiterListener(ClearLaggEnhanced plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();

        this.enabled = configManager.getBoolean("lag-prevention.hopper-limiter.enabled", true);
        this.baseCooldownTicks = Math.max(1, configManager.getInt("lag-prevention.hopper-limiter.transfer-cooldown", 8));
        this.maxHoppersPerChunk = Math.max(0, configManager.getInt("lag-prevention.hopper-limiter.max-hoppers-per-chunk", 0));
        this.debug = configManager.getBoolean("debug", false);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryMove(InventoryMoveItemEvent event) {
        if (!enabled) return;

        Inventory initiatorInv = event.getInitiator();
        if (initiatorInv == null) return;

        InventoryHolder holder = initiatorInv.getHolder();
        if (!(holder instanceof Hopper)) {
            return;
        }

        Hopper hopper = (Hopper) holder;
        World world = hopper.getWorld();
        if (world == null) return;

        long now = world.getFullTime();
        Chunk chunk = hopper.getLocation().getChunk();
        String key = chunkKey(chunk);

        int effectiveCooldown = baseCooldownTicks;
        if (maxHoppersPerChunk > 0) {
            int count = countHoppersInChunkCached(chunk);
            if (count > maxHoppersPerChunk) {
                int excess = count - maxHoppersPerChunk;
                effectiveCooldown += Math.max(0, excess * 2);
            }
        }

        long last = lastMoveTickByChunk.getOrDefault(key, now - effectiveCooldown);

        if ((now - last) < effectiveCooldown) {
            event.setCancelled(true);
            lastMoveTickByChunk.put(key, now);
            maybeLogChunkThrottle(chunk, now);
            return;
        }

        lastMoveTickByChunk.put(key, now);
    }

    private static String chunkKey(Chunk c) {
        return c.getWorld().getName() + ":" + c.getX() + "," + c.getZ();
    }

    private int countHoppersInChunkCached(Chunk chunk) {
        String key = chunkKey(chunk);
        Integer cached = hopperCountByChunk.get(key);
        if (cached != null) return cached;

        if (!chunk.isLoaded()) {
            return 0;
        }

        int count = scanHoppersInChunk(chunk);
        hopperCountByChunk.put(key, count);
        return count;
    }

    private static int scanHoppersInChunk(Chunk chunk) {
        try {
            BlockState[] tiles = chunk.getTileEntities();
            if (tiles != null && tiles.length > 0) {
                int c = 0;
                for (BlockState st : tiles) {
                    if (st instanceof org.bukkit.block.Hopper) {
                        c++;
                    }
                }
                return c;
            }
        } catch (NoSuchMethodError | UnsupportedOperationException ignored) {
        }

        int baseX = chunk.getX() << 4;
        int baseZ = chunk.getZ() << 4;
        int minY = chunk.getWorld().getMinHeight();
        int maxY = chunk.getWorld().getMaxHeight();

        int count = 0;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = minY; y < maxY; y++) {
                    Block b = chunk.getWorld().getBlockAt(baseX + x, y, baseZ + z);
                    if (b.getType() == Material.HOPPER) {
                        BlockState st = b.getState();
                        if (st instanceof org.bukkit.block.Hopper) {
                            count++;
                        }
                    }
                }
            }
        }
        return count;
    }

    private static String chunkKey(World w, int cx, int cz) {
        return w.getName() + ":" + cx + "," + cz;
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e) {
        if (!enabled) return;
        Chunk chunk = e.getChunk();
        hopperCountByChunk.put(chunkKey(chunk), scanHoppersInChunk(chunk));
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent e) {
        if (!enabled) return;
        Chunk chunk = e.getChunk();
        String key = chunkKey(chunk);
        hopperCountByChunk.remove(key);
        lastMoveTickByChunk.remove(key);
        lastLogTickByChunk.remove(key);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e) {
        if (!enabled) return;
        if (e.getBlockPlaced().getType() != Material.HOPPER) return;
        String key = chunkKey(e.getBlockPlaced().getChunk());
        hopperCountByChunk.merge(key, 1, Integer::sum);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        if (!enabled) return;
        if (e.getBlock().getType() != Material.HOPPER) return;
        String key = chunkKey(e.getBlock().getChunk());
        hopperCountByChunk.compute(key, (k, v) -> {
            int current = (v == null ? 0 : v);
            current = Math.max(0, current - 1);
            return current; // keep 0 to avoid repeated lazy scans
        });
    }

    public void rescanLoadedChunks() {
        for (org.bukkit.World w : plugin.getServer().getWorlds()) {
            for (org.bukkit.Chunk c : w.getLoadedChunks()) {
                String key = chunkKey(c);
                hopperCountByChunk.put(key, scanHoppersInChunk(c));
                lastMoveTickByChunk.remove(key);
                lastLogTickByChunk.remove(key);
            }
        }
    }

    private void maybeLogChunkThrottle(Chunk chunk, long nowTick) {
        if (!debug) return;
        String key = chunkKey(chunk);
        long last = lastLogTickByChunk.getOrDefault(key, 0L);
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
