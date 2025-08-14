package org.busybee.clearlaggenhanced.modules;

import org.busybee.clearlaggenhanced.ClearLaggEnhanced;
import org.busybee.clearlaggenhanced.modules.impl.*;
import org.busybee.clearlaggenhanced.utils.Logger;

import java.util.*;
import java.util.stream.Collectors;

public class ModuleManager {

    private final ClearLaggEnhanced plugin;
    private final Map<String, PerformanceModule> modules = new LinkedHashMap<>();
    private final Map<String, Boolean> moduleStates = new HashMap<>();

    public ModuleManager(ClearLaggEnhanced plugin) {
        this.plugin = plugin;
    }

    public void initializeAll() {
        Logger.info("Initializing performance modules...");

        registerModule(new EntityManagerModule(plugin));
        registerModule(new ChunkManagerModule(plugin));
        registerModule(new HeuristicsEngineModule(plugin));
        // registerModule(new RedstoneThrottlerModule(plugin));
        // registerModule(new HopperOptimizerModule(plugin));
        // registerModule(new DiagnosticsModule(plugin));

        for (PerformanceModule module : modules.values()) {
            try {
                if (module.isEnabled()) {
                    module.initialize();
                    moduleStates.put(module.getName(), true);
                    Logger.info("Initialized module: " + module.getName());
                } else {
                    moduleStates.put(module.getName(), false);
                    Logger.info("Skipped disabled module: " + module.getName());
                }
            } catch (Exception e) {
                Logger.severe("Failed to initialize module " + module.getName() + ": " + e.getMessage());
                moduleStates.put(module.getName(), false);
                e.printStackTrace();
            }
        }

        Logger.info("Module initialization complete. Active modules: " + getEnabledModuleCount());
    }

    private void registerModule(PerformanceModule module) {
        modules.put(module.getName(), module);
    }

    public void enableModule(String moduleName) {
        PerformanceModule module = modules.get(moduleName);
        if (module != null && !moduleStates.getOrDefault(moduleName, false)) {
            try {
                module.initialize();
                moduleStates.put(moduleName, true);
                Logger.info("Enabled module: " + moduleName);
            } catch (Exception e) {
                Logger.severe("Failed to enable module " + moduleName + ": " + e.getMessage());
            }
        }
    }

    public void disableModule(String moduleName) {
        PerformanceModule module = modules.get(moduleName);
        if (module != null && moduleStates.getOrDefault(moduleName, false)) {
            try {
                module.shutdown();
                moduleStates.put(moduleName, false);
                Logger.info("Disabled module: " + moduleName);
            } catch (Exception e) {
                Logger.severe("Failed to disable module " + moduleName + ": " + e.getMessage());
            }
        }
    }

    public void reloadAll() {
        Logger.info("Reloading all modules...");
        for (String moduleName : modules.keySet()) {
            boolean wasEnabled = moduleStates.getOrDefault(moduleName, false);
            if (wasEnabled) {
                disableModule(moduleName);
            }
            if (modules.get(moduleName).isEnabled()) {
                enableModule(moduleName);
            }
        }
        Logger.info("All modules reloaded");
    }

    public void shutdown() {
        Logger.info("Shutting down all modules...");
        for (PerformanceModule module : modules.values()) {
            if (moduleStates.getOrDefault(module.getName(), false)) {
                try {
                    module.shutdown();
                    Logger.info("Shutdown module: " + module.getName());
                } catch (Exception e) {
                    Logger.severe("Error shutting down module " + module.getName() + ": " + e.getMessage());
                }
            }
        }
        Logger.info("All modules shutdown complete");
    }

    public PerformanceModule getModule(String name) {
        return modules.get(name);
    }

    public Collection<PerformanceModule> getAllModules() {
        return modules.values();
    }

    public Set<String> getAllModuleNames() {
        return modules.keySet();
    }

    public int getEnabledModuleCount() {
        return (int) moduleStates.values().stream().filter(Boolean::booleanValue).count();
    }

    public boolean isModuleEnabled(String moduleName) {
        return moduleStates.getOrDefault(moduleName, false);
    }
}
