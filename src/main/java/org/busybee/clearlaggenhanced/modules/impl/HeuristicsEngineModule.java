package org.busybee.clearlaggenhanced.modules.impl;

import org.busybee.clearlaggenhanced.ClearLaggEnhanced;
import org.busybee.clearlaggenhanced.config.configs.ModuleConfig;
import org.busybee.clearlaggenhanced.modules.PerformanceModule;
import org.busybee.clearlaggenhanced.utils.Logger;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.atomic.AtomicLong;

public class HeuristicsEngineModule extends PerformanceModule {

    private final ModuleConfig config;
    private final AtomicLong operations = new AtomicLong(0);
    private BukkitTask monitorTask;

    public HeuristicsEngineModule(ClearLaggEnhanced plugin) {
        super(plugin, "heuristics-engine");
        this.config = plugin.getConfigManager().getModuleConfig(this.name);
    }

    @Override
    public void initialize() {
        Logger.info("Initializing Heuristics Engine Module...");
    }

    @Override
    public void shutdown() {
        if (monitorTask != null) {
            monitorTask.cancel();
            monitorTask = null;
        }
        Logger.info("Heuristics Engine Module shutdown.");
    }

    @Override
    public boolean isEnabled() {
        return config != null && config.isEnabled();
    }

    @Override
    public ModuleStats getStats() {
        boolean active = monitorTask != null && !monitorTask.isCancelled();
        return new ModuleStats(this.name, active, operations.get(), "Online");
    }
}
