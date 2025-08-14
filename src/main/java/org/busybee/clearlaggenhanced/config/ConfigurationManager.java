package org.busybee.clearlaggenhanced.config;

import org.busybee.clearlaggenhanced.ClearLaggEnhanced;
import org.busybee.clearlaggenhanced.config.configs.MainConfig;
import org.busybee.clearlaggenhanced.config.configs.ModuleConfig;
import org.busybee.clearlaggenhanced.utils.Logger;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages all configuration files using the modern Configurate library
 */
public class ConfigurationManager {
    
    private final ClearLaggEnhanced plugin;
    private final Path dataFolder;
    
    private MainConfig mainConfig;
    private final Map<String, ModuleConfig> moduleConfigs = new HashMap<>();
    
    public ConfigurationManager(ClearLaggEnhanced plugin) {
        this.plugin = plugin;
        this.dataFolder = plugin.getDataFolder().toPath();
    }
    
    public void loadAll() {
        try {
            // Create data folder if it doesn't exist
            if (!Files.exists(dataFolder)) {
                Files.createDirectories(dataFolder);
            }
            
            // Load main configuration
            loadMainConfig();
            
            // Load module configurations
            loadModuleConfigs();
            
            Logger.info("All configuration files loaded successfully");
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to load configuration files", e);
        }
    }
    
    private void loadMainConfig() throws IOException {
        Path configPath = dataFolder.resolve("config.yml");
        
        YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                .path(configPath)
                .nodeStyle(NodeStyle.BLOCK)
                .build();
        
        ConfigurationNode root;
        
        if (!Files.exists(configPath)) {
            // Create default config
            root = loader.createNode();
            mainConfig = new MainConfig();
            mainConfig.saveToNode(root);
            loader.save(root);
            Logger.info("Created default main configuration");
        } else {
            // Load existing config
            root = loader.load();
            mainConfig = new MainConfig();
            mainConfig.loadFromNode(root);
            Logger.info("Loaded main configuration");
        }
    }
    
    private void loadModuleConfigs() throws IOException {
        Path modulesPath = dataFolder.resolve("modules");
        
        if (!Files.exists(modulesPath)) {
            Files.createDirectories(modulesPath);
        }
        
        // Load each module config
        String[] modules = {
            "entity-manager",
            "chunk-manager", 
            "redstone-throttler",
            "hopper-optimizer",
            "heuristics-engine",
            "diagnostics"
        };
        
        for (String moduleName : modules) {
            loadModuleConfig(moduleName);
        }
    }
    
    private void loadModuleConfig(String moduleName) throws IOException {
        Path configPath = dataFolder.resolve("modules").resolve(moduleName + ".yml");
        
        YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                .path(configPath)
                .nodeStyle(NodeStyle.BLOCK)
                .build();
        
        ConfigurationNode root;
        ModuleConfig config = new ModuleConfig(moduleName);
        
        if (!Files.exists(configPath)) {
            // Create default config
            root = loader.createNode();
            config.saveToNode(root);
            loader.save(root);
            Logger.info("Created default configuration for module: " + moduleName);
        } else {
            // Load existing config
            root = loader.load();
            config.loadFromNode(root);
            Logger.info("Loaded configuration for module: " + moduleName);
        }
        
        moduleConfigs.put(moduleName, config);
    }
    
    public void reloadAll() {
        Logger.info("Reloading all configuration files...");
        
        try {
            loadAll();
            Logger.info("Configuration reload completed");
        } catch (Exception e) {
            Logger.severe("Failed to reload configuration: " + e.getMessage());
            throw new RuntimeException("Configuration reload failed", e);
        }
    }
    
    public void shutdown() {
        // Save any pending changes
        Logger.info("Configuration manager shutdown complete");
    }
    
    // Getters
    public MainConfig getMainConfig() {
        return mainConfig;
    }
    
    public ModuleConfig getModuleConfig(String moduleName) {
        return moduleConfigs.get(moduleName);
    }
    
    public Map<String, ModuleConfig> getAllModuleConfigs() {
        return new HashMap<>(moduleConfigs);
    }
}