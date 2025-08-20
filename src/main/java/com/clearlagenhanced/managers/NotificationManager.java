package com.clearlagenhanced.managers;

import com.clearlagenhanced.ClearLaggEnhanced;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationManager {
    
    private final ClearLaggEnhanced plugin;
    private final ConfigManager configManager;
    private final MessageManager messageManager;
    private BukkitTask warningTask;
    
    public NotificationManager(ClearLaggEnhanced plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.messageManager = plugin.getMessageManager();
    }
    
    public void sendClearWarnings() {
        List<Integer> broadcastTimes = configManager.getIntegerList("notifications.broadcast-times");
        
        if (broadcastTimes.isEmpty()) {
            Bukkit.getScheduler().runTaskLater(plugin, this::performClear, 20L);
            return;
        }

        int maxTime = broadcastTimes.stream().max(Integer::compareTo).orElse(0);
        
        for (int seconds : broadcastTimes) {
            long delay = (maxTime - seconds) * 20L; // Convert to ticks
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                sendWarning(seconds);
            }, delay);
        }

        Bukkit.getScheduler().runTaskLater(plugin, this::performClear, maxTime * 20L);
    }
    
    private void sendWarning(int seconds) {
        String notificationType = configManager.getString("notifications.type", "ACTION_BAR").toUpperCase();
        boolean soundEnabled = configManager.getBoolean("notifications.sound.enabled", true);
        String soundName = configManager.getString("notifications.sound.name", "BLOCK_NOTE_BLOCK_PLING");
        float volume = (float) configManager.getDouble("notifications.sound.volume", 1.0);
        float pitch = (float) configManager.getDouble("notifications.sound.pitch", 1.2);

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("seconds", String.valueOf(seconds));
        
        Component message = messageManager.getMessage("warnings.entity-clear", placeholders);

        for (Player player : Bukkit.getOnlinePlayers()) {
            switch (notificationType) {
                case "CHAT":
                    player.sendMessage(message);
                    break;
                    
                case "ACTION_BAR":
                    player.sendActionBar(message);
                    break;
                    
                case "TITLE":
                    Component titleMain = Component.text("Entity Clear Warning");
                    Title title = Title.title(
                        titleMain,
                        message,
                        Title.Times.times(
                            Duration.ofMillis(500), // fade in
                            Duration.ofSeconds(2),  // stay
                            Duration.ofMillis(500)  // fade out
                        )
                    );
                    player.showTitle(title);
                    break;
                    
                default:
                    player.sendActionBar(message);
                    plugin.getLogger().warning("Invalid notification type: " + notificationType + ". Using ACTION_BAR as fallback.");
                    break;
            }

            if (soundEnabled) {
                try {
                    Sound sound = Sound.valueOf(soundName.toUpperCase());
                    player.playSound(player.getLocation(), sound, volume, pitch);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid sound name: " + soundName + ". Skipping sound playback.");
                }
            }
        }

        plugin.getLogger().info("Sent entity clear warning: " + seconds + " seconds remaining");
    }
    
    private void performClear() {
        long startTime = System.currentTimeMillis();
        int cleared = plugin.getEntityManager().clearEntities();
        long duration = System.currentTimeMillis() - startTime;

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("count", String.valueOf(cleared));
        placeholders.put("time", String.valueOf(duration));
        
        Component message = messageManager.getMessage("notifications.clear-complete", placeholders);

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(message);
        }
        
        plugin.getLogger().info("Cleared " + cleared + " entities in " + duration + "ms");
    }

    public void cancelWarnings() {
        if (warningTask != null && !warningTask.isCancelled()) {
            warningTask.cancel();
            warningTask = null;
        }
    }

    public void sendImmediateWarning(int seconds) {
        sendWarning(seconds);
    }
}
