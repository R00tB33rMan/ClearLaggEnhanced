package org.busybee.clearlaggenhanced.modules.impl;

import org.busybee.clearlaggenhanced.ClearLaggEnhanced;
import org.busybee.clearlaggenhanced.config.configs.ModuleConfig;
import org.busybee.clearlaggenhanced.modules.PerformanceModule;
import org.busybee.clearlaggenhanced.utils.Logger;

import java.util.concurrent.atomic.AtomicLong;

public class HopperOptimizerModule extends PerformanceModule {
    
    private final ModuleConfig config;
    private final AtomicLong optimizedTransfers = new AtomicLong(0);
    
    public HopperOptimizerModule(ClearLaggEnhanced plugin) {
        super(plugin, "hopper-optimizer");
        this.config = plugin.getConfigManager().getModuleConfig(this.name);
    }
    
    @Override
    public void initialize() {
        Logger.info("Initializing Hopper Optimizer Module...");
        
        Logger.info("Hopper Optimizer Module initialized");
    }
    
    @Override
    public void shutdown() {
        Logger.info("Shutting down Hopper Optimizer Module...");
        Logger.info("Hopper Optimizer Module shutdown complete");
    }
    
    @Override
    public boolean isEnabled() {
        return config != null && config.isEnabled();
    }
    
    @Override
    public ModuleStats getStats() {
        return new ModuleStats(
            getName(),
            true,
            optimizedTransfers.get(),
            "Optimized transfers: " + optimizedTransfers.get()
        );
    }
}
