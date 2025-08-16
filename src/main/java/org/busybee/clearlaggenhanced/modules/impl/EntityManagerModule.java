package org.busybee.clearlaggenhanced.modules.impl;

import org.busybee.clearlaggenhanced.ClearLaggEnhanced;
import org.busybee.clearlaggenhanced.config.configs.ModuleConfig;
import org.busybee.clearlaggenhanced.modules.PerformanceModule;
import org.busybee.clearlaggenhanced.utils.Logger;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.atomic.AtomicLong;

public class EntityManagerModule extends PerformanceModule {

    private final ModuleConfig config;
    private final AtomicLong operations = new AtomicLong(0);
    private BukkitTask scanTask;

    public EntityManagerModule(ClearLaggEnhanced plugin) {
        super(plugin, "entity-manager");
        this.config = plugin.getConfigManager().getModuleConfig(this.name);
    }

    @Override
    public void initialize() {
        Logger.info("Initializing Entity Manager Module...");

    }

    @Override
    public void shutdown() {
        if (scanTask != null) {
            scanTask.cancel();
            scanTask = null;
        }
        Logger.info("Entity Manager Module shutdown.");
    }

    @Override
    public boolean isEnabled() {
        return config != null && config.isEnabled();
    }

    @Override
    public ModuleStats getStats() {
        boolean active = scanTask != null && !scanTask.isCancelled();
        return new ModuleStats(this.name, active, operations.get(), "Ready");
    }
}
