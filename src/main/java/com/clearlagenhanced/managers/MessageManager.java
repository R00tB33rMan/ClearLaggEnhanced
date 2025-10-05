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

    private static final int CURRENT_MESSAGES_VERSION = 2;

    private final ClearLaggEnhanced plugin;
    private FileConfiguration messages;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.legacyAmpersand();
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
        checkMessagesVersion();
    }

    private void checkMessagesVersion() {
        int messagesVersion = messages.getInt("version", 1);

        if (messagesVersion < CURRENT_MESSAGES_VERSION) {
            plugin.getLogger().info("Messages file version " + messagesVersion + " is outdated. Updating to version " + CURRENT_MESSAGES_VERSION + "...");
            migrateMessages(messagesVersion, CURRENT_MESSAGES_VERSION);
        } else if (messagesVersion > CURRENT_MESSAGES_VERSION) {
            plugin.getLogger().warning("Messages file version " + messagesVersion + " is newer than supported version " + CURRENT_MESSAGES_VERSION + "!");
        }
    }

    private void migrateMessages(int fromVersion, int toVersion) {
        try {
            File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
            File backupFile = new File(plugin.getDataFolder(), "messages.yml.backup-v" + fromVersion);

            if (messagesFile.exists()) {
                YamlConfiguration oldMessages = YamlConfiguration.loadConfiguration(messagesFile);
                oldMessages.save(backupFile);
                plugin.getLogger().info("Backed up old messages to: " + backupFile.getName());
            }

            File tempNewFile = new File(plugin.getDataFolder(), "messages.yml.new");
            plugin.saveResource("messages.yml", true);
            File defaultFile = new File(plugin.getDataFolder(), "messages.yml");
            defaultFile.renameTo(tempNewFile);

            YamlConfiguration oldMessages = YamlConfiguration.loadConfiguration(backupFile);
            YamlConfiguration newMessages = YamlConfiguration.loadConfiguration(tempNewFile);

            mergeMessages(oldMessages, newMessages);

            newMessages.set("version", CURRENT_MESSAGES_VERSION);
            newMessages.save(messagesFile);

            tempNewFile.delete();
            messages = YamlConfiguration.loadConfiguration(messagesFile);

            plugin.getLogger().info("Messages successfully updated to version " + CURRENT_MESSAGES_VERSION);
            plugin.getLogger().info("Added " + countNewKeys(oldMessages, newMessages) + " new messages while preserving your customizations");

        } catch (IOException e) {
            plugin.getLogger().severe("Failed to migrate messages: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void mergeMessages(FileConfiguration oldMessages, FileConfiguration newMessages) {
        for (String key : oldMessages.getKeys(true)) {
            if (key.equals("version")) {
                continue;
            }

            if (newMessages.contains(key) && !oldMessages.isConfigurationSection(key)) {
                Object value = oldMessages.get(key);
                newMessages.set(key, value);
            }
        }
    }

    private int countNewKeys(FileConfiguration oldMessages, FileConfiguration newMessages) {
        int count = 0;
        for (String key : newMessages.getKeys(true)) {
            if (!key.equals("version") && !oldMessages.contains(key) && !newMessages.isConfigurationSection(key)) {
                count++;
            }
        }
        return count;
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

        String normalized = normalizeToMiniMessage(message);

        try {
            return miniMessage.deserialize(normalized);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to parse MiniMessage. Falling back to legacy. Message: " + message);
        }

        return legacySerializer.deserialize(message);
    }

    private String normalizeToMiniMessage(String input) {
        if (input == null || input.isEmpty()) return "";

        String msg = input.replace('§', '&'); // treat section as &
        msg = convertLegacyXHex(msg);
        msg = AMP_HEX.matcher(msg).replaceAll(mr -> "<#" + mr.group(1) + ">");
        msg = msg.replaceAll("(?i)(?<!<)#([A-F0-9]{6})", "<#$1>");
        msg = convertLegacyCodesToMini(msg);

        return msg;
    }

    private String convertLegacyXHex(String msg) {
        Matcher m = LEGACY_X_HEX.matcher(msg);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String full = m.group(); // e.g., &x&F&F&0&0&0&0
            StringBuilder hex = new StringBuilder(6);
            for (int i = 3; i < full.length(); i += 2) {
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
