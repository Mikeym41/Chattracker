package yomikey.chattracker;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class Chattracker extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getLogger().info("ChatTracker enabled!");

        // Register events
        getServer().getPluginManager().registerEvents(this, this);

        // Ensure data folder exists
        File dataFolder = getDataFolder();
        if (!dataFolder.exists()) {
            if (!dataFolder.mkdirs()) {
                getLogger().warning("Failed to create plugin data folder!");
            }
        }

        // Register /chattracker command safely
        if (this.getCommand("chattracker") != null) {
            this.getCommand("chattracker").setExecutor((sender, command, label, args) -> {
                sender.sendMessage("§aChatTracker is working!");
                return true;
            });
        } else {
            getLogger().warning("Command /chattracker is not defined in plugin.yml!");
        }



        // Ensure config is loaded
        saveDefaultConfig();
    }

    // Log when a player joins
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        getLogger().info(event.getPlayer().getName() + " has joined the server!");
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        String playerName = event.getPlayer().getName();
        String message = event.getMessage();
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String formatted = "[" + time + "] " + playerName + ": " + message;

        // Log to console
        getLogger().info(formatted);

        try {
            File folder = new File(getDataFolder(), "chatlogs");
            if (!folder.exists() && !folder.mkdirs()) {
                getLogger().warning("Failed to create chatlogs folder!");
            }

            // Daily log file
            String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            File logFile = new File(folder, "chatlog-" + date + ".txt");
            try (FileWriter writer = new FileWriter(logFile, true)) {
                writer.write(formatted + "\n");
            }

            // Check for banned words
            List<String> bannedWords = getConfig().getStringList("banned-words");
            boolean flagged = false;
            for (String word : bannedWords) {
                if (message.toLowerCase().contains(word.toLowerCase())) {
                    flagged = true;
                    break;
                }
            }

            if (flagged) {
                // Notify moderators
                getServer().getOnlinePlayers().forEach(p -> {
                    if (p.hasPermission("chattracker.moderate")) {
                        p.sendMessage("§c[ChatTracker] Potential abuse by " + playerName + ": " + message);
                    }
                });

                // Log flagged messages
                File flaggedFile = new File(folder, "flags-" + date + ".txt");
                try (FileWriter flaggedWriter = new FileWriter(flaggedFile, true)) {
                    flaggedWriter.write(formatted + "\n");
                }
            }

        } catch (IOException e) {
            getLogger().severe("An error occurred while writing to chat logs: " + e.getMessage());

        }
    }
}











