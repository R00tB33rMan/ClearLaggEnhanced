package org.busybee.clearlaggenhanced.modules.impl;

import org.busybee.clearlaggenhanced.ClearLaggEnhanced;
import org.busybee.clearlaggenhanced.config.configs.ModuleConfig;
import org.busybee.clearlaggenhanced.modules.PerformanceModule;
import org.busybee.clearlaggenhanced.utils.Logger;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.atomic.AtomicLong;

public class ChunkManagerModule extends PerformanceModule {

    private final ModuleConfig config;
    private final AtomicLong operations = new AtomicLong(0);
    private BukkitTask scanTask;

    public ChunkManagerModule(ClearLaggEnhanced plugin) {
        super(plugin, "chunk-manager");
        this.config = plugin.getConfigManager().getModuleConfig(this.name);
    }

    @Override
    public void initialize() {
        Logger.info("Initializing Chunk Manager Module...");
        if (config.getBoolean("auto-unload.enabled")) {
            long interval = config.getInt("auto-unload.check-interval") * 20L;
        }
    }

    private void scanChunks() {
        operations.incrementAndGet();
    }

    @Override
    public void shutdown() {
        if (scanTask != null) {
            scanTask.cancel();
            scanTask = null;
        }
        Logger.info("Chunk Manager Module shutdown.");
    }

    @Override
    public boolean isEnabled() {
        return config != null && config.isEnabled();
    }

    @Override
    public ModuleStats getStats() {
        boolean active = scanTask != null && !scanTask.isCancelled();
        return new ModuleStats(this.name, active, operations.get(), "Monitoring Chunks");
    }
}
