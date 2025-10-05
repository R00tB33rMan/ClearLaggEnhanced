package com.clearlagenhanced;

import com.clearlagenhanced.commands.LaggCommand;
import com.clearlagenhanced.database.DatabaseManager;
import com.clearlagenhanced.managers.*;
import com.clearlagenhanced.utils.MessageUtils;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public class ClearLaggEnhanced extends JavaPlugin {

    private static ClearLaggEnhanced instance;
    private DatabaseManager databaseManager;
    private ConfigManager configManager;
    private MessageManager messageManager;
    private EntityManager entityManager;
    private LagPreventionManager lagPreventionManager;
    private PerformanceManager performanceManager;
    private NotificationManager notificationManager;
    private GUIManager guiManager;
    private com.clearlagenhanced.managers.MiscEntitySweepService miscSweep;

    @Override
    public void onEnable() {
        instance = this;
        
        saveDefaultConfig();
        
        initializeManagers();
        registerCommands();
        getServer().getPluginManager().registerEvents(
                new com.clearlagenhanced.listeners.MobLimiterListener(this), this);
        getServer().getPluginManager().registerEvents(
                new com.clearlagenhanced.listeners.RedstoneLimiterListener(this), this);
        getServer().getPluginManager().registerEvents(
                new com.clearlagenhanced.listeners.HopperLimiterListener(this), this);
        getServer().getPluginManager().registerEvents(
                new com.clearlagenhanced.listeners.SpawnerLimiterListener(this), this);
        // Register update notifier
        getServer().getPluginManager().registerEvents(
                new com.clearlagenhanced.utils.VersionCheck(this), this);

        // Misc entity limiter (non-mobs): start sweeper and register proactive listener if enabled in config
        boolean miscEnabled = getConfigManager().getBoolean("lag-prevention.misc-entity-limiter.enabled", true);
        if (miscEnabled) {
            miscSweep = new com.clearlagenhanced.managers.MiscEntitySweepService(this, getConfigManager());
            miscSweep.start();
            getServer().getPluginManager().registerEvents(
                    new com.clearlagenhanced.listeners.MiscEntityLimiterListener(this, miscSweep), this);
        }
        
        getLogger().info("ClearLaggEnhanced has been enabled!");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.close();
        }
        
        if (entityManager != null) {
            entityManager.shutdown();
        }
        
        if (guiManager != null) {
            guiManager.shutdown();
        }

        if (miscSweep != null) {
            miscSweep.shutdown();
        }
        
        getLogger().info("ClearLaggEnhanced has been disabled!");
    }
    
    private void initializeManagers() {
        configManager = new ConfigManager(this);
        messageManager = new MessageManager(this);
        databaseManager = new DatabaseManager(this);
        entityManager = new EntityManager(this);
        lagPreventionManager = new LagPreventionManager(this);
        performanceManager = new PerformanceManager(this);
        notificationManager = new NotificationManager(this);
        guiManager = new GUIManager(this);
        
        MessageUtils.initialize(messageManager);
        
        getLogger().info("All managers initialized successfully!");
    }
    
    private void registerCommands() {
        getCommand("lagg").setExecutor(new LaggCommand(this));
        getCommand("lagg").setTabCompleter(new LaggCommand(this));
    }

    public void reloadAll() {
        HandlerList.unregisterAll(this);

        if (entityManager != null) {
            entityManager.shutdown();
        }
        if (guiManager != null) {
            guiManager.shutdown();
        }
        if (miscSweep != null) {
            miscSweep.shutdown();
            miscSweep = null;
        }

        if (configManager != null) {
            configManager.reload();
        }
        if (messageManager != null) {
            messageManager.reload();
        }
        MessageUtils.initialize(messageManager);

        entityManager = new EntityManager(this);
        lagPreventionManager = new LagPreventionManager(this);
        performanceManager = new PerformanceManager(this);
        notificationManager = new NotificationManager(this);
        guiManager = new GUIManager(this);

        getServer().getPluginManager().registerEvents(
                new com.clearlagenhanced.listeners.MobLimiterListener(this), this);
        getServer().getPluginManager().registerEvents(
                new com.clearlagenhanced.listeners.RedstoneLimiterListener(this), this);

        com.clearlagenhanced.listeners.HopperLimiterListener hopperListener =
                new com.clearlagenhanced.listeners.HopperLimiterListener(this);
        getServer().getPluginManager().registerEvents(hopperListener, this);
        try {
            hopperListener.rescanLoadedChunks();
        } catch (NoSuchMethodError | Exception ignored) {
        }

        getServer().getPluginManager().registerEvents(
                new com.clearlagenhanced.listeners.SpawnerLimiterListener(this), this);

        boolean miscEnabled = getConfigManager().getBoolean("lag-prevention.misc-entity-limiter.enabled", true);
        if (miscEnabled) {
            miscSweep = new com.clearlagenhanced.managers.MiscEntitySweepService(this, getConfigManager());
            miscSweep.start();
            getServer().getPluginManager().registerEvents(
                    new com.clearlagenhanced.listeners.MiscEntityLimiterListener(this, miscSweep), this);
        }

        getServer().getPluginManager().registerEvents(
                new com.clearlagenhanced.utils.VersionCheck(this), this);
    }

    public static ClearLaggEnhanced getInstance() {
        return instance;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public LagPreventionManager getLagPreventionManager() {
        return lagPreventionManager;
    }

    public PerformanceManager getPerformanceManager() {
        return performanceManager;
    }

    public NotificationManager getNotificationManager() {
        return notificationManager;
    }

    public GUIManager getGUIManager() {
        return guiManager;
    }
}
