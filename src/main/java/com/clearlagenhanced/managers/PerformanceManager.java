package com.clearlagenhanced.managers;

import com.clearlagenhanced.ClearLaggEnhanced;
import com.clearlagenhanced.utils.MessageUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PerformanceManager {

    private final ClearLaggEnhanced plugin;
    private final ConfigManager configManager;

    public PerformanceManager(ClearLaggEnhanced plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
    }

    public double getTPS() {
        try {
            return Bukkit.getServer().getTPS()[0];
        } catch (Exception e) {
            return 20.0;
        }
    }

    public long getUsedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    public long getTotalMemory() {
        return Runtime.getRuntime().totalMemory();
    }

    public long getMaxMemory() {
        return Runtime.getRuntime().maxMemory();
    }

    public double getMemoryUsagePercentage() {
        return (double) getUsedMemory() / getMaxMemory() * 100.0;
    }

    public String getFormattedMemoryUsage() {
        long used = getUsedMemory() / 1024 / 1024; // Convert to MB
        long max = getMaxMemory() / 1024 / 1024;
        return used + "MB / " + max + "MB";
    }

    public int getTotalEntities() {
        int total = 0;
        for (org.bukkit.World world : Bukkit.getWorlds()) {
            total += world.getEntities().size();
        }
        return total;
    }

    public boolean isServerLagging() {
        return getTPS() < 18.0;
    }

    public boolean isMemoryHigh() {
        return getMemoryUsagePercentage() > 85.0;
    }

    public void findLaggyChunksAsync(Player player) {
        MessageUtils.sendMessage(player, Component.text("Scanning for laggy chunks...")
                .color(NamedTextColor.GREEN));

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            World world = player.getWorld();
            int radius = configManager.getInt("chunk-finder.radius", 10);
            int entityThreshold = configManager.getInt("chunk-finder.entity-threshold", 50);

            Chunk playerChunk = player.getLocation().getChunk();
            int playerX = playerChunk.getX();
            int playerZ = playerChunk.getZ();

            List<ChunkInfo> laggyChunks = new ArrayList<>();

            for (int x = playerX - radius; x <= playerX + radius; x++) {
                for (int z = playerZ - radius; z <= playerZ + radius; z++) {
                    if (world.isChunkLoaded(x, z)) {
                        Chunk chunk = world.getChunkAt(x, z);
                        Entity[] entities = chunk.getEntities();

                        if (entities.length >= entityThreshold) {
                            int distance = Math.max(Math.abs(x - playerX), Math.abs(z - playerZ));
                            laggyChunks.add(new ChunkInfo(x, z, entities.length, distance));
                        }
                    }
                }
            }

            Bukkit.getScheduler().runTask(plugin, () -> {
                if (laggyChunks.isEmpty()) {
                    MessageUtils.sendMessage(player, Component.text("No laggy chunks found within " + radius + " chunks!")
                            .color(NamedTextColor.GREEN));
                } else {
                    MessageUtils.sendMessage(player, Component.text("=== Top 10 Laggy Chunks Found ===")
                            .color(NamedTextColor.RED));

                    laggyChunks.sort((a, b) -> Integer.compare(b.entityCount, a.entityCount));

                    int maxResults = Math.min(laggyChunks.size(), 10);
                    for (int i = 0; i < maxResults; i++) {
                        ChunkInfo chunk = laggyChunks.get(i);
                        NamedTextColor color = chunk.entityCount >= 100 ? NamedTextColor.RED : 
                                             chunk.entityCount >= 75 ? NamedTextColor.GOLD : NamedTextColor.YELLOW;

                        MessageUtils.sendMessage(player, Component.text("Chunk [" + chunk.x + ", " + chunk.z + "] - " + 
                                chunk.entityCount + " entities (" + chunk.distance + " chunks away)")
                                .color(color));
                    }

                    if (laggyChunks.size() > 10) {
                        MessageUtils.sendMessage(player, Component.text("... and " + (laggyChunks.size() - 10) + " more")
                                .color(NamedTextColor.GRAY));
                    }
                }
            });
        });
    }

    private static class ChunkInfo {
        final int x, z, entityCount, distance;

        ChunkInfo(int x, int z, int entityCount, int distance) {
            this.x = x;
            this.z = z;
            this.entityCount = entityCount;
            this.distance = distance;
        }
    }
}
