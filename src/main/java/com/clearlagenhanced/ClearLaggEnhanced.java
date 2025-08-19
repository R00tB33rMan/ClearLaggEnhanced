package com.clearlagenhanced;

import com.clearlagenhanced.commands.LaggCommand;
import com.clearlagenhanced.database.DatabaseManager;
import com.clearlagenhanced.managers.*;
import com.clearlagenhanced.utils.MessageUtils;
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

    @Override
    public void onEnable() {
        instance = this;
        
        saveDefaultConfig();
        
        initializeManagers();
        registerCommands();
        
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

        // Start automatic entity clearing task after all managers are initialized
        getLogger().info("All managers initialized successfully!");
    }
    
    private void registerCommands() {
        getCommand("lagg").setExecutor(new LaggCommand(this));
        getCommand("lagg").setTabCompleter(new LaggCommand(this));
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