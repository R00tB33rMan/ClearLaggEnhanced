package com.clearlagenhanced.managers;

import com.clearlagenhanced.ClearLaggEnhanced;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageManager {
    
    private final ClearLaggEnhanced plugin;
    private FileConfiguration messages;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.legacyAmpersand();
    private final Pattern hexPattern = Pattern.compile("#([A-Fa-f0-9]{6})");
    private boolean placeholderAPIEnabled;
    
    public MessageManager(ClearLaggEnhanced plugin) {
        this.plugin = plugin;
        this.placeholderAPIEnabled = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
        loadMessages();
    }
    
    private void loadMessages() {
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        
        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }
    
    public void reload() {
        loadMessages();
    }
    public FileConfiguration getConfig() {
        return messages;
    }
    public String getRawMessage(String path) {
        return messages.getString(path, "Message not found: " + path);
    }
    public String getRawMessage(String path, String defaultValue) {
        return messages.getString(path, defaultValue);
    }
    
    public Component getMessage(String path, Map<String, String> placeholders) {
        String message = getRawMessage(path);
        return parseMessage(message, placeholders, null);
    }
    
    public Component getMessage(String path, Map<String, String> placeholders, Player player) {
        String message = getRawMessage(path);
        return parseMessage(message, placeholders, player);
    }
    
    public Component getMessage(String path) {
        return getMessage(path, new HashMap<>());
    }
    
    public Component parseMessage(String message, Map<String, String> placeholders, Player player) {
        if (message == null || message.isEmpty()) {
            return Component.empty();
        }
        
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        
        if (placeholderAPIEnabled && player != null) {
            message = PlaceholderAPI.setPlaceholders(player, message);
        }
        
        if (message.contains("<") && message.contains(">")) {
            try {
                return miniMessage.deserialize(message);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to parse MiniMessage: " + message);
            }
        }
        
        if (hexPattern.matcher(message).find()) {
            message = parseHexColors(message);
        }
        
        return legacySerializer.deserialize(message);
    }
    
    private String parseHexColors(String message) {
        Matcher matcher = hexPattern.matcher(message);
        StringBuffer buffer = new StringBuffer();
        
        while (matcher.find()) {
            String hex = matcher.group(1);
            matcher.appendReplacement(buffer, "§x§" + 
                hex.charAt(0) + "§" + hex.charAt(1) + "§" + 
                hex.charAt(2) + "§" + hex.charAt(3) + "§" + 
                hex.charAt(4) + "§" + hex.charAt(5));
        }
        
        matcher.appendTail(buffer);
        return buffer.toString();
    }
    
    public void saveMessages() {
        try {
            messages.save(new File(plugin.getDataFolder(), "messages.yml"));
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save messages.yml: " + e.getMessage());
        }
    }
}
