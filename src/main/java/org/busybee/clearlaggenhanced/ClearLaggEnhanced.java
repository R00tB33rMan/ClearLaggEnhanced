package org.busybee.clearlaggenhanced;

import org.busybee.clearlaggenhanced.commands.ClearLaggCommand;
import org.busybee.clearlaggenhanced.commands.ClearLaggTabCompleter;
import org.busybee.clearlaggenhanced.config.ConfigurationManager;
import org.busybee.clearlaggenhanced.modules.ModuleManager;
import org.busybee.clearlaggenhanced.scheduler.AsyncTaskManager;
import org.busybee.clearlaggenhanced.utils.Logger;
import org.busybee.clearlaggenhanced.utils.ServerVersion;
import org.busybee.clearlaggenhanced.utils.LanguageManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class ClearLaggEnhanced extends JavaPlugin {

    private static ClearLaggEnhanced instance;
    private ConfigurationManager configManager;
    private AsyncTaskManager taskManager;
    private ModuleManager moduleManager;
    private LanguageManager languageManager;

    @Override
    public void onEnable() {
        instance = this;

        Logger.info("Starting ClearLaggEnhanced v" + getDescription().getVersion());
        Logger.info("Server version: " + ServerVersion.getVersion());

        try {
            initializeCoreManagers();
            loadConfiguration();
            initializeModules();
            registerCommands();

            Logger.info("ClearLaggEnhanced has been successfully enabled!");

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

        taskManager = new AsyncTaskManager(this);
        configManager = new ConfigurationManager(this);
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
        java.util.logging.Logger.getLogger("Minecraft").info("Registering commands..."); // Using java.util.logging.Logger for consistency if that's what your project uses
        org.bukkit.command.PluginCommand cmd = this.getCommand("clearlagg"); // base command name from plugin.yml
        if (cmd != null) {
            ClearLaggCommand executor = new ClearLaggCommand(this);
            cmd.setExecutor(executor);
            cmd.setTabCompleter(new ClearLaggTabCompleter(this)); // Register the tab completer
            java.util.logging.Logger.getLogger("Minecraft").info("Registered /" + cmd.getName() + " command executor and tab completer.");
        } else {
            java.util.logging.Logger.getLogger("Minecraft").severe("Failed to register commands: base command 'clearlagg' not found in plugin.yml.");
        }
    }

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
    public LanguageManager getLanguageManager() {
        return languageManager;
    }
}
