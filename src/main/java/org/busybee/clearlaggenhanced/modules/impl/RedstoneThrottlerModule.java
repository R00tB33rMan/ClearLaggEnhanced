package org.busybee.clearlaggenhanced.modules.impl;

import org.busybee.clearlaggenhanced.ClearLaggEnhanced;
import org.busybee.clearlaggenhanced.config.configs.ModuleConfig;
import org.busybee.clearlaggenhanced.modules.PerformanceModule;
import org.busybee.clearlaggenhanced.utils.Logger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;

import java.util.concurrent.atomic.AtomicLong;

public class RedstoneThrottlerModule extends PerformanceModule implements Listener {
    
    private final ModuleConfig config;
    private final AtomicLong redstoneEvents = new AtomicLong(0);
    private final AtomicLong throttledEvents = new AtomicLong(0);
    
    public RedstoneThrottlerModule(ClearLaggEnhanced plugin) {
        super(plugin, "redstone-throttler");
        this.config = plugin.getConfigManager().getModuleConfig(this.name);
    }
    
    @Override
    public void initialize() {
        Logger.info("Initializing Redstone Throttler Module...");

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        
        Logger.info("Redstone Throttler Module initialized");
    }
    
    @EventHandler
    public void onRedstoneEvent(BlockRedstoneEvent event) {
        redstoneEvents.incrementAndGet();
        int maxUpdatesPerTick = config.getInt("max-updates-per-tick");
        if (redstoneEvents.get() % 100 < maxUpdatesPerTick) {
        } else {
            throttledEvents.incrementAndGet();
            event.setNewCurrent(event.getOldCurrent());
        }
    }
    
    @Override
    public void shutdown() {
        Logger.info("Shutting down Redstone Throttler Module...");
        Logger.info("Redstone Throttler Module shutdown complete");
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
            redstoneEvents.get(),
            "Events: " + redstoneEvents.get() + ", Throttled: " + throttledEvents.get()
        );
    }
}
