package org.busybee.clearlaggenhanced;

import org.busybee.clearlaggenhanced.commands.CommandManager;
import org.busybee.clearlaggenhanced.config.ConfigurationManager;
import org.busybee.clearlaggenhanced.modules.ModuleManager;
import org.busybee.clearlaggenhanced.scheduler.AsyncTaskManager;
import org.busybee.clearlaggenhanced.utils.Logger;
import org.busybee.clearlaggenhanced.utils.ServerVersion;
import org.busybee.clearlaggenhanced.utils.LanguageManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * ClearLaggEnhanced - The ultimate performance management plugin
 * 
 * A complete rewrite of the classic ClearLag plugin with modern features:
 * - Intelligent entity management and culling
 * - Proactive chunk garbage collection
 * - AI-powered pathfinding throttling
 * - Real-time performance monitoring
 * - Heuristics-based auto-optimization
 * - Comprehensive diagnostic toolkit
 */
public final class ClearLaggEnhanced extends JavaPlugin {
    
    private static ClearLaggEnhanced instance;
    
    // Core managers
    private ConfigurationManager configManager;
    private AsyncTaskManager taskManager;
    private ModuleManager moduleManager;
    private CommandManager commandManager;
    private LanguageManager languageManager;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Initialize core components
        Logger.info("Starting ClearLaggEnhanced v" + getDescription().getVersion());
        Logger.info("Server version: " + ServerVersion.getVersion());
        
        try {
            // Phase 1: Initialize core systems
            initializeCoreManagers();
            
            // Phase 2: Load configuration
            loadConfiguration();
            
            // Phase 3: Initialize modules
            initializeModules();
            
            // Phase 4: Register commands
            registerCommands();
            
            
            Logger.info("ClearLaggEnhanced has been successfully enabled!");
            Logger.info("Use '/cle menu' to access the control panel");
            
        } catch (Exception e) {
            Logger.severe("Failed to initialize ClearLaggEnhanced: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }
    
    @Override
    public void onDisable() {
        Logger.info("Disabling ClearLaggEnhanced...");
        
        try {
            // Shutdown in reverse order
            if (commandManager != null) {
                commandManager.shutdown();
            }
            
            if (moduleManager != null) {
                moduleManager.shutdown();
            }
            
            if (taskManager != null) {
                taskManager.shutdown();
            }
            
            if (configManager != null) {
                configManager.shutdown();
            }
            
            Logger.info("ClearLaggEnhanced has been disabled.");
            
        } catch (Exception e) {
            Logger.severe("Error during shutdown: " + e.getMessage());
            e.printStackTrace();
        }
        
        instance = null;
    }
    
    private void initializeCoreManagers() {
        Logger.info("Initializing core managers...");
        
        // Initialize task manager first (other components depend on it)
        taskManager = new AsyncTaskManager(this);
        
        // Initialize configuration manager
        configManager = new ConfigurationManager(this);
        
        // Initialize module manager
        moduleManager = new ModuleManager(this);
    }
    
    private void loadConfiguration() {
        Logger.info("Loading configuration files...");
        configManager.loadAll();
        initializeLanguage();
    }
    
    private void initializeLanguage() {
        Logger.info("Loading language files...");
        languageManager = new org.busybee.clearlaggenhanced.utils.LanguageManager(this);
        languageManager.load();
    }
    
    private void initializeModules() {
        Logger.info("Initializing performance modules...");
        moduleManager.initializeAll();
    }
    
    private void registerCommands() {
        Logger.info("Registering commands...");
        commandManager = new CommandManager(this);
        commandManager.registerAll();
    }
    
    // Getters for core components
    public static ClearLaggEnhanced getInstance() {
        return instance;
    }
    
    public ConfigurationManager getConfigManager() {
        return configManager;
    }
    
    public AsyncTaskManager getTaskManager() {
        return taskManager;
    }
    
    public ModuleManager getModuleManager() {
        return moduleManager;
    }
    
    public CommandManager getCommandManager() {
        return commandManager;
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }
}