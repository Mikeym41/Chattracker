package yomikey.chattracker;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
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
import java.util.*;

public final class Chattracker extends JavaPlugin implements Listener {

    // Track players who toggled staff chat
    private final Set<UUID> staffChatToggled = new HashSet<>();

    @Override
    public void onEnable() {
        getLogger().info("ChatTracker enabled!");

        // Register events
        getServer().getPluginManager().registerEvents(this, this);

        // Ensure data folder exists
        File dataFolder = getDataFolder();
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            getLogger().warning("Failed to create plugin data folder!");
        }

        // Register commands
        if (this.getCommand("chattracker") != null) {
            this.getCommand("chattracker").setExecutor((sender, command, label, args) -> {
                sender.sendMessage("§aChatTracker is working!");
                return true;
            });
        }

        if (this.getCommand("staffchat") != null) {
            this.getCommand("staffchat").setExecutor(new StaffChatCommand());
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
        Player player = event.getPlayer();
        String playerName = player.getName();
        String message = event.getMessage();
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String formatted = "[" + time + "] " + playerName + ": " + message;

        // Check if player has staff chat toggled
        if (staffChatToggled.contains(player.getUniqueId()) && player.hasPermission("chattracker.staffchat")) {
            event.setCancelled(true);
            sendStaffChatMessage(player, message);
            return;
        }

        // Normal logging
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

    // Send a staff chat message
    private void sendStaffChatMessage(Player sender, String message) {
        String staffMessage = "§8[§bStaffChat§8] §e" + sender.getName() + ": §f" + message;
        getServer().getOnlinePlayers().forEach(p -> {
            if (p.hasPermission("chattracker.staffchat")) {
                p.sendMessage(staffMessage);
            }
        });
        getLogger().info("[StaffChat] " + sender.getName() + ": " + message);
    }

    // Command executor for /staffchat
    private class StaffChatCommand implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players can use this command.");
                return true;
            }

            Player player = (Player) sender;
            if (!player.hasPermission("chattracker.staffchat")) {
                player.sendMessage("§cYou don't have permission to use Staff Chat.");
                return true;
            }

            if (args.length == 0) {
                // Toggle staff chat
                if (staffChatToggled.contains(player.getUniqueId())) {
                    staffChatToggled.remove(player.getUniqueId());
                    player.sendMessage("§cStaff Chat toggled OFF.");
                } else {
                    staffChatToggled.add(player.getUniqueId());
                    player.sendMessage("§aStaff Chat toggled ON.");
                }
            } else {
                // Send one-time message
                String message = String.join(" ", args);
                sendStaffChatMessage(player, message);
            }
            return true;
        }
    }
}











