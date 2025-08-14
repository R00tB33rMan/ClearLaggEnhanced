package org.busybee.clearlaggenhanced.utils;

import org.busybee.clearlaggenhanced.ClearLaggEnhanced;

import java.util.logging.Level;

/**
 * Centralized logging utility
 */
public class Logger {
    
    private static final String PREFIX = "[ClearLaggEnhanced] ";
    
    public static void info(String message) {
        log(Level.INFO, message);
    }
    
    public static void warning(String message) {
        log(Level.WARNING, message);
    }
    
    public static void severe(String message) {
        log(Level.SEVERE, message);
    }
    
    public static void debug(String message) {
        if (ClearLaggEnhanced.getInstance() != null && 
            ClearLaggEnhanced.getInstance().getConfigManager() != null &&
            ClearLaggEnhanced.getInstance().getConfigManager().getMainConfig().getGeneral().isDebugMode()) {
            log(Level.INFO, "[DEBUG] " + message);
        }
    }
    
    public static void verbose(String message) {
        if (ClearLaggEnhanced.getInstance() != null && 
            ClearLaggEnhanced.getInstance().getConfigManager() != null &&
            ClearLaggEnhanced.getInstance().getConfigManager().getMainConfig().getGeneral().isVerboseLogging()) {
            log(Level.INFO, "[VERBOSE] " + message);
        }
    }
    
    private static void log(Level level, String message) {
        if (ClearLaggEnhanced.getInstance() != null) {
            ClearLaggEnhanced.getInstance().getLogger().log(level, message);
        } else {
            // Fallback for early initialization
            System.out.println(PREFIX + level.getName() + ": " + message);
        }
    }
}