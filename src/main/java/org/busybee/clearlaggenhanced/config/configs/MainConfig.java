package org.busybee.clearlaggenhanced.config.configs;

import org.spongepowered.configurate.ConfigurationNode;

/**
 * Main plugin configuration
 */
public class MainConfig {
    
    private GeneralSettings general = new GeneralSettings();
    private MetricsSettings metrics = new MetricsSettings();
    private PerformanceSettings performance = new PerformanceSettings();
    
    public void loadFromNode(ConfigurationNode node) {
        general.loadFromNode(node.node("general"));
        metrics.loadFromNode(node.node("metrics"));
        performance.loadFromNode(node.node("performance"));
    }
    
    public void saveToNode(ConfigurationNode node) {
        general.saveToNode(node.node("general"));
        metrics.saveToNode(node.node("metrics"));
        performance.saveToNode(node.node("performance"));
    }
    
    // Getters
    public GeneralSettings getGeneral() { return general; }
    public MetricsSettings getMetrics() { return metrics; }
    public PerformanceSettings getPerformance() { return performance; }
    
    public static class GeneralSettings {
        private String language = "en";
        private boolean debugMode = false;
        private boolean verboseLogging = false;
        
        public void loadFromNode(ConfigurationNode node) {
            language = node.node("language").getString("en");
            debugMode = node.node("debug-mode").getBoolean(false);
            verboseLogging = node.node("verbose-logging").getBoolean(false);
        }
        
        public void saveToNode(ConfigurationNode node) {
            node.node("language").raw("en");
            node.node("debug-mode").raw(false);
            node.node("verbose-logging").raw(false);
        }
        
        public String getLanguage() { return language; }
        public boolean isDebugMode() { return debugMode; }
        public boolean isVerboseLogging() { return verboseLogging; }
    }
    
    public static class MetricsSettings {
        private boolean enabled = true;
        
        public void loadFromNode(ConfigurationNode node) {
            enabled = node.node("enabled").getBoolean(true);
        }
        
        public void saveToNode(ConfigurationNode node) {
            node.node("enabled").raw(true);
        }
        
        public boolean isEnabled() { return enabled; }
    }
    
    public static class PerformanceSettings {
        private int asyncThreads = 2;
        private int maxTicksPerSecond = 20;
        private double lowTpsThreshold = 18.0;
        private double criticalTpsThreshold = 15.0;
        
        public void loadFromNode(ConfigurationNode node) {
            asyncThreads = node.node("async-threads").getInt(2);
            maxTicksPerSecond = node.node("max-ticks-per-second").getInt(20);
            lowTpsThreshold = node.node("low-tps-threshold").getDouble(18.0);
            criticalTpsThreshold = node.node("critical-tps-threshold").getDouble(15.0);
        }
        
        public void saveToNode(ConfigurationNode node) {
            node.node("async-threads").raw(2);
            node.node("max-ticks-per-second").raw(20);
            node.node("low-tps-threshold").raw(18.0);
            node.node("critical-tps-threshold").raw(15.0);
        }
        
        public int getAsyncThreads() { return asyncThreads; }
        public int getMaxTicksPerSecond() { return maxTicksPerSecond; }
        public double getLowTpsThreshold() { return lowTpsThreshold; }
        public double getCriticalTpsThreshold() { return criticalTpsThreshold; }
    }
}