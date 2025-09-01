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

public final class Chattracker extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getLogger().info("ChatTracker enabled!");

        // Register events
        getServer().getPluginManager().registerEvents(this, this);

        // Ensure data folder exists
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        // Register /chattracker command
        this.getCommand("chattracker").setExecutor((sender, command, label, args) -> {
            sender.sendMessage("§aChatTracker is working!");
            return true;
        });
    }

    // Log when a player joins
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        getLogger().info(event.getPlayer().getName() + " has joined the server!");
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        // Step 1: Get the message and player
        String playerName = event.getPlayer().getName();
        String message = event.getMessage();

        // Step 2: Create timestamp
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

        // Step 3: Format the message for logging
        String formatted = "[" + time + "] " + playerName + ": " + message;

        // Step 4: Log to console
        getLogger().info(formatted);

        // Step 5: Save to file
        try {
            File folder = new File(getDataFolder(), "chatlogs");
            if (!folder.exists()) folder.mkdirs(); // create folder if missing

            // Daily log file
            String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            File logFile = new File(folder, "chatlog-" + date + ".txt");

            FileWriter writer = new FileWriter(logFile, true); // true = append
            writer.write(formatted + "\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}










