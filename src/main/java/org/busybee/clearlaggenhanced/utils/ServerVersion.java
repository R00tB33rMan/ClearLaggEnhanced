package org.busybee.clearlaggenhanced.utils;

import org.bukkit.Bukkit;

/**
 * Server version detection utility
 */
public class ServerVersion {
    
    private static final String version = Bukkit.getServer().getClass().getPackage().getName();
    private static final String bukkitVersion = Bukkit.getBukkitVersion();
    
    public static String getVersion() {
        return bukkitVersion;
    }
    
    public static String getNMSVersion() {
        return version.substring(version.lastIndexOf('.') + 1);
    }
    
    public static boolean isVersionAtLeast(int major, int minor) {
        try {
            String[] parts = bukkitVersion.split("-")[0].split("\\.");
            int serverMajor = Integer.parseInt(parts[1]);
            int serverMinor = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
            
            return serverMajor > minor || (serverMajor == minor && serverMinor >= minor);
        } catch (Exception e) {
            return false;
        }
    }
    
    public static boolean isPaper() {
        try {
            Class.forName("com.destroystokyo.paper.PaperConfig");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
    
    public static boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}