package org.busybee.clearlaggenhanced.utils;

import org.busybee.clearlaggenhanced.ClearLaggEnhanced;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class LanguageManager {

    private final ClearLaggEnhanced plugin;
    private final Map<String, String> messages = new HashMap<>();

    public LanguageManager(ClearLaggEnhanced plugin) {
        this.plugin = plugin;
    }

    public void load() {
        String lang = plugin.getConfigManager().getMainConfig().getGeneral().getLanguage();
        if (lang == null || lang.isBlank()) {
            lang = "en";
        }
        loadLanguageFile("messages_" + lang + ".yml");
        if (messages.isEmpty() && !"en".equalsIgnoreCase(lang)) {
            loadLanguageFile("messages_en.yml");
        }
    }

    private void loadLanguageFile(String fileName) {
        try {
            Path dataPath = plugin.getDataFolder().toPath().resolve(fileName);
            if (!Files.exists(dataPath)) {
                try (InputStream in = plugin.getResource("messages/" + fileName)) {
                    if (in != null) {
                        Files.createDirectories(dataPath.getParent());
                        Files.copy(in, dataPath);
                    }
                } catch (IOException ignored) {}
            }

            if (Files.exists(dataPath)) {
                YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                        .path(dataPath)
                        .nodeStyle(NodeStyle.BLOCK)
                        .build();
                ConfigurationNode root = loader.load();
                for (Map.Entry<Object, ? extends ConfigurationNode> entry : root.childrenMap().entrySet()) {
                    String parent = String.valueOf(entry.getKey());
                    ConfigurationNode section = entry.getValue();
                    if (section.isMap()) {
                        for (Map.Entry<Object, ? extends ConfigurationNode> e2 : section.childrenMap().entrySet()) {
                            String key = parent + "." + e2.getKey();
                            String value = e2.getValue().getString("");
                            messages.put(key, value);
                        }
                    } else {
                        String value = section.getString("");
                        messages.put(parent, value);
                    }
                }
            }
        } catch (Exception e) {
            Logger.severe("Failed to load language file '" + fileName + "': " + e.getMessage());
        }
    }

    public String get(String key) {
        return messages.getOrDefault(key, key);
    }

    public String get(String key, Map<String, String> placeholders) {
        String msg = get(key);
        if (placeholders != null) {
            for (Map.Entry<String, String> e : placeholders.entrySet()) {
                msg = msg.replace("{" + e.getKey() + "}", String.valueOf(e.getValue()));
            }
        }
        return msg;
    }
}
