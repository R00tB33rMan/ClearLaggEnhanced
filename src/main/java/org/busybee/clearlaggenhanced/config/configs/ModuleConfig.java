package org.busybee.clearlaggenhanced.config.configs;

import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for individual modules
 */
public class ModuleConfig {
    
    private final String moduleName;
    private boolean enabled = true;
    private int priority = 100;
    private final Map<String, Object> settings = new HashMap<>();
    
    public ModuleConfig(String moduleName) {
        this.moduleName = moduleName;
        initializeDefaults();
    }
    
    private void initializeDefaults() {
        switch (moduleName) {
            case "entity-manager":
                settings.put("max-entities-per-chunk", 50);
                settings.put("culling-radius", 100);
                settings.put("safe-entity-types", new String[]{"PLAYER", "VILLAGER", "ITEM_FRAME"});
                settings.put("ai-throttling.enabled", true);
                settings.put("ai-throttling.distance-threshold", 32);
                settings.put("ai-throttling.throttle-percentage", 50);
                break;
                
            case "chunk-manager":
                settings.put("auto-unload.enabled", true);
                settings.put("auto-unload.inactive-time", 300); // 5 minutes
                settings.put("auto-unload.check-interval", 60); // 1 minute
                settings.put("keep-spawn-loaded", true);
                settings.put("spawn-protection-radius", 10);
                break;
                
            case "redstone-throttler":
                settings.put("max-updates-per-tick", 100);
                settings.put("throttle-delay", 2);
                settings.put("clock-detection.enabled", true);
                settings.put("clock-detection.threshold", 20);
                break;
                
            case "hopper-optimizer":
                settings.put("transfer-cooldown", 8);
                settings.put("chunk-loading-prevention", true);
                settings.put("item-grouping", true);
                settings.put("max-items-per-transfer", 64);
                break;
                
            case "heuristics-engine":
                enabled = true;
                settings.put("monitoring-interval", 5);
                settings.put("auto-adjust", true);
                settings.put("response-sensitivity", 0.5);
                settings.put("max-adjustment-percentage", 25);
                break;
                
            case "diagnostics":
                settings.put("tps-monitoring", true);
                settings.put("memory-monitoring", true);
                settings.put("entity-counting", true);
                settings.put("chunk-analysis", true);
                break;
        }
    }
    
    public void loadFromNode(ConfigurationNode node) {
        enabled = node.node("enabled").getBoolean(true);
        priority = node.node("priority").getInt(100);
        
        ConfigurationNode settingsNode = node.node("settings");
        for (String key : settings.keySet()) {
            Object defaultValue = settings.get(key);
            
            if (key.contains(".")) {
                // Nested setting
                String[] parts = key.split("\\.");
                ConfigurationNode current = settingsNode;
                for (int i = 0; i < parts.length - 1; i++) {
                    current = current.node(parts[i]);
                }
                
                if (defaultValue instanceof Boolean) {
                    settings.put(key, current.node(parts[parts.length - 1]).getBoolean((Boolean) defaultValue));
                } else if (defaultValue instanceof Integer) {
                    settings.put(key, current.node(parts[parts.length - 1]).getInt((Integer) defaultValue));
                } else if (defaultValue instanceof Double) {
                    settings.put(key, current.node(parts[parts.length - 1]).getDouble((Double) defaultValue));
                } else if (defaultValue instanceof String[]) {
                    try {
                        java.util.List<String> list = current.node(parts[parts.length - 1]).getList(String.class, java.util.Arrays.asList((String[]) defaultValue));
                        settings.put(key, list.toArray(new String[0]));
                    } catch (SerializationException e) {
                        settings.put(key, (String[]) defaultValue);
                    }
                }
            } else {
                // Simple setting
                if (defaultValue instanceof Boolean) {
                    settings.put(key, settingsNode.node(key).getBoolean((Boolean) defaultValue));
                } else if (defaultValue instanceof Integer) {
                    settings.put(key, settingsNode.node(key).getInt((Integer) defaultValue));
                } else if (defaultValue instanceof Double) {
                    settings.put(key, settingsNode.node(key).getDouble((Double) defaultValue));
                } else if (defaultValue instanceof String[]) {
                    try {
                        java.util.List<String> list = settingsNode.node(key).getList(String.class, java.util.Arrays.asList((String[]) defaultValue));
                        settings.put(key, list.toArray(new String[0]));
                    } catch (SerializationException e) {
                        settings.put(key, (String[]) defaultValue);
                    }
                }
            }
        }
    }
    
    public void saveToNode(ConfigurationNode node) {
        node.node("enabled").raw(enabled);
        node.node("priority").raw(priority);
        
        ConfigurationNode settingsNode = node.node("settings");
        
        for (Map.Entry<String, Object> entry : settings.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            if (key.contains(".")) {
                // Nested setting
                String[] parts = key.split("\\.");
                ConfigurationNode current = settingsNode;
                for (int i = 0; i < parts.length - 1; i++) {
                    current = current.node(parts[i]);
                }
                current.node(parts[parts.length - 1]).raw(value);
            } else {
                // Simple setting
                settingsNode.node(key).raw(value);
            }
        }
    }
    
    // Getters
    public String getModuleName() { return moduleName; }
    public boolean isEnabled() { return enabled; }
    public int getPriority() { return priority; }
    
    public boolean getBoolean(String key) {
        return (Boolean) settings.getOrDefault(key, false);
    }
    
    public int getInt(String key) {
        return (Integer) settings.getOrDefault(key, 0);
    }
    
    public double getDouble(String key) {
        return (Double) settings.getOrDefault(key, 0.0);
    }
    
    public String[] getStringArray(String key) {
        return (String[]) settings.getOrDefault(key, new String[0]);
    }
    
    public void set(String key, Object value) {
        settings.put(key, value);
    }
}