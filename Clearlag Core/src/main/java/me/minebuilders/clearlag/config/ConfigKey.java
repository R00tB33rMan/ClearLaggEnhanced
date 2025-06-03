package me.minebuilders.clearlag.config;

/**
 * Centralized configuration paths for Clearlag
 */
public enum ConfigKey {
    // General settings
    CONFIG_VERSION("config-version"),
    AUTO_UPDATE("settings.auto-update"),
    
    // Module settings
    TNT_REDUCER_ENABLED("tnt-reducer.enabled"),
    TNT_REDUCER_CHECK_RADIUS("tnt-reducer.check-radius"),
    TNT_REDUCER_MAX_PRIMED("tnt-reducer.max-primed"),
    
    // Entity clearing settings
    ENTITY_CLEAR_INTERVAL("auto-removal.interval"),
    ENTITY_CLEAR_WARN_TIMES("auto-removal.warn-times"),
    
    // Special value for default/none
    NONE(""),
    
    // Add more configuration paths as needed
    ;

    private final String path;

    ConfigKey(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}