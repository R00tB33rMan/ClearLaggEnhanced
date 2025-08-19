package com.clearlagenhanced.managers;

import com.clearlagenhanced.ClearLaggEnhanced;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class ConfigManager {
    
    private final ClearLaggEnhanced plugin;
    private FileConfiguration config;
    private boolean debuggingInProgress = false;
    
    public ConfigManager(ClearLaggEnhanced plugin) {
        this.plugin = plugin;
        reload();
    }
    
    public void reload() {
        plugin.reloadConfig();
        config = plugin.getConfig();
        debugLog("Configuration reloaded");
    }
    
    private void debugLog(String message) {
        if (debuggingInProgress) {
            return; // Prevent recursive calls
        }
        
        debuggingInProgress = true;
        try {
            if (config.getBoolean("debug", false)) {
                plugin.getLogger().info("[DEBUG] ConfigManager: " + message);
            }
        } finally {
            debuggingInProgress = false;
        }
    }
    
    public boolean getBoolean(String path, boolean defaultValue) {
        boolean value = config.getBoolean(path, defaultValue);
        debugLog("Getting boolean " + path + " = " + value);
        return value;
    }
    
    public int getInt(String path, int defaultValue) {
        int value = config.getInt(path, defaultValue);
        debugLog("Getting int " + path + " = " + value);
        return value;
    }
    
    public double getDouble(String path, double defaultValue) {
        double value = config.getDouble(path, defaultValue);
        debugLog("Getting double " + path + " = " + value);
        return value;
    }
    
    public String getString(String path, String defaultValue) {
        String value = config.getString(path, defaultValue);
        debugLog("Getting string " + path + " = " + value);
        return value;
    }
    
    public List<String> getStringList(String path) {
        List<String> value = config.getStringList(path);
        debugLog("Getting string list " + path + " = " + value);
        return value;
    }
    
    public List<Integer> getIntegerList(String path) {
        List<Integer> value = config.getIntegerList(path);
        debugLog("Getting integer list " + path + " = " + value);
        return value;
    }
    
    public void set(String path, Object value) {
        debugLog("Setting " + path + " = " + value);
        config.set(path, value);
        
        // Save the config synchronously to ensure it's written immediately
        try {
            plugin.saveConfig();
            debugLog("Configuration saved successfully");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to save configuration: " + e.getMessage());
        }
    }
    
    public FileConfiguration getConfig() {
        return config;
    }
}