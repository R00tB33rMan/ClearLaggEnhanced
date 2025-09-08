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

    // Patterns for hex and legacy hex variants
    private static final Pattern PLAIN_HEX = Pattern.compile("#([A-Fa-f0-9]{6})");
    private static final Pattern AMP_HEX = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final Pattern LEGACY_X_HEX = Pattern.compile("(?i)&x(?:&([0-9A-F])){6}");
    private static final Pattern LEGACY_COLOR_CODE = Pattern.compile("(?i)&([0-9A-FK-OR])");

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
        
        // 1) Apply placeholders
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        
        // 2) PlaceholderAPI
        if (placeholderAPIEnabled && player != null) {
            message = PlaceholderAPI.setPlaceholders(player, message);
        }

        // 3) Normalize all legacy/hex forms to MiniMessage tags so mixes work
        String normalized = normalizeToMiniMessage(message);

        // 4) Try MiniMessage first
        try {
            return miniMessage.deserialize(normalized);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to parse MiniMessage. Falling back to legacy. Message: " + message);
        }

        // 5) Fallback: try legacy deserializer (supports & codes and §)
        return legacySerializer.deserialize(message);
    }

    /**
     * Converts a message possibly containing a mix of MiniMessage, legacy (&/§) and hex
     * into a MiniMessage-friendly string by translating legacy tokens to MiniMessage tags.
     */
    private String normalizeToMiniMessage(String input) {
        if (input == null || input.isEmpty()) return "";

        String msg = input.replace('§', '&'); // treat section as &

        // Convert legacy &x hex sequences to <#RRGGBB>
        msg = convertLegacyXHex(msg);

        // Convert &#RRGGBB to <#RRGGBB>
        msg = AMP_HEX.matcher(msg).replaceAll(mr -> "<#" + mr.group(1) + ">");

        // Convert bare #RRGGBB (not already a tag like <#RRGGBB>) to <#RRGGBB>
        msg = msg.replaceAll("(?i)(?<!<)#([A-F0-9]{6})", "<#$1>");

        // Convert legacy & codes to MiniMessage tags
        msg = convertLegacyCodesToMini(msg);

        return msg;
    }

    private String convertLegacyXHex(String msg) {
        Matcher m = LEGACY_X_HEX.matcher(msg);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            // Extract the six captured hex chars from the entire match using a secondary matcher
            String full = m.group(); // e.g., &x&F&F&0&0&0&0
            StringBuilder hex = new StringBuilder(6);
            for (int i = 3; i < full.length(); i += 2) { // skip &x, then every second char after each &
                char ch = full.charAt(i);
                if (isHex(ch)) hex.append(ch);
            }
            if (hex.length() == 6) {
                m.appendReplacement(sb, '<' + "#" + hex + '>');
            } else {
                m.appendReplacement(sb, full); // leave as-is if not parsable
            }
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private boolean isHex(char c) {
        return (c >= '0' && c <= '9') || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f');
    }

    private String convertLegacyCodesToMini(String msg) {
        StringBuilder out = new StringBuilder(msg.length() + 16);
        for (int i = 0; i < msg.length(); i++) {
            char c = msg.charAt(i);
            if (c == '&' && i + 1 < msg.length()) {
                char code = Character.toLowerCase(msg.charAt(i + 1));
                String tag = mapLegacyToMiniTag(code);
                if (tag != null) {
                    out.append('<').append(tag).append('>');
                    i++; // skip code char
                    continue;
                }
            }
            out.append(c);
        }
        return out.toString();
    }

    private String mapLegacyToMiniTag(char code) {
        switch (code) {
            case '0': return "black";
            case '1': return "dark_blue";
            case '2': return "dark_green";
            case '3': return "dark_aqua";
            case '4': return "dark_red";
            case '5': return "dark_purple";
            case '6': return "gold";
            case '7': return "gray";
            case '8': return "dark_gray";
            case '9': return "blue";
            case 'a': return "green";
            case 'b': return "aqua";
            case 'c': return "red";
            case 'd': return "light_purple";
            case 'e': return "yellow";
            case 'f': return "white";
            case 'k': return "obfuscated";
            case 'l': return "bold";
            case 'm': return "strikethrough";
            case 'n': return "underlined";
            case 'o': return "italic";
            case 'r': return "reset";
            default: return null;
        }
    }

    public void saveMessages() {
        try {
            messages.save(new File(plugin.getDataFolder(), "messages.yml"));
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save messages.yml: " + e.getMessage());
        }
    }
}
