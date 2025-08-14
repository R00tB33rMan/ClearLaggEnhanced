package org.busybee.clearlaggenhanced.modules;

import org.busybee.clearlaggenhanced.ClearLaggEnhanced;

public abstract class PerformanceModule {

    protected final ClearLaggEnhanced plugin;
    protected final String name;

    public PerformanceModule(ClearLaggEnhanced plugin, String name) {
        this.plugin = plugin;
        this.name = name;
    }

    public abstract void initialize();

    public abstract void shutdown();

    public String getName() {
        return name;
    }

    public abstract boolean isEnabled();

    public record ModuleStats(String moduleName, boolean active, long operations, String status) {}
    public abstract ModuleStats getStats();
}
