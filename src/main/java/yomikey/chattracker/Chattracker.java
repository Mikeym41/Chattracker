package yomikey.chattracker;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public final class Chattracker extends JavaPlugin implements Listener {

    private final Set<UUID> staffChatToggled = new HashSet<>();
    private final Map<String, String> tempRanks = new HashMap<>();

    @Override
    public void onEnable() {
        getLogger().info("ChatTracker enabled!");

        getServer().getPluginManager().registerEvents(this, this);
        saveDefaultConfig();

        // Register commands
        if (getCommand("chattracker") != null)
            getCommand("chattracker").setExecutor((sender, command, label, args) -> {
                sender.sendMessage("§aChatTracker is working!");
                return true;
            });

        if (getCommand("staffchat") != null)
            getCommand("staffchat").setExecutor(new StaffChatCommand());

        if (getCommand("setrank") != null)
            getCommand("setrank").setExecutor(new SetRankCommand());

        if (getCommand("resetrank") != null)
            getCommand("resetrank").setExecutor(new ResetRankCommand());

        if (getCommand("report") != null)
            getCommand("report").setExecutor(new ReportCommand());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        getLogger().info(event.getPlayer().getName() + " has joined the server!");
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();
        String message = event.getMessage();

        // ====== STAFF CHAT CHECK ======
        if (staffChatToggled.contains(player.getUniqueId()) && player.hasPermission("chattracker.staffchat")) {
            event.setCancelled(true);
            sendStaffChatMessage(player, message);
            return;
        }

        // ====== CHAT FORMATTING ======
        String group = tempRanks.getOrDefault(playerName.toLowerCase(),
                getConfig().getString("ranks." + playerName.toLowerCase() + ".group", "default"));

        String prefix = getConfig().getString("ranks." + group + ".prefix", "");
        String suffix = getConfig().getString("ranks." + group + ".suffix", "");
        String nameColor = getConfig().getString("ranks." + group + ".color", "&f");
        String messageColor = getConfig().getString("ranks." + group + ".message-color", "&f"); // NEW

        // Full chat line with name coloring
        String fullMessage = ChatColor.translateAlternateColorCodes('&',
                prefix + ChatColor.translateAlternateColorCodes('&', nameColor) + playerName + suffix + "&7: &r" +
                        ChatColor.translateAlternateColorCodes('&', messageColor) + message);

        // Clickable hover message
        TextComponent chatComponent = new TextComponent(fullMessage);
        chatComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder("§cClick to report this message").create()));
        chatComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                "/report " + playerName + " " + message));

        event.setCancelled(true);
        player.getServer().spigot().broadcast(chatComponent);

        // Logging
        logMessage(playerName, message);
        checkFlaggedWords(player, message);
    }

    private void logMessage(String playerName, String message) {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String formatted = "[" + time + "] " + playerName + ": " + message;
        getLogger().info(formatted);

        try {
            File folder = new File(getDataFolder(), "chatlogs");
            if (!folder.exists() && !folder.mkdirs()) getLogger().warning("Failed to create chatlogs folder!");
            String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            File logFile = new File(folder, "chatlog-" + date + ".txt");
            try (FileWriter writer = new FileWriter(logFile, true)) {
                writer.write(formatted + "\n");
            }
        } catch (IOException e) {
            getLogger().severe("Error writing chat logs: " + e.getMessage());
        }
    }

    private void checkFlaggedWords(Player player, String message) {
        List<String> bannedWords = getConfig().getStringList("banned-words");
        boolean flagged = bannedWords.stream().anyMatch(word -> message.toLowerCase().contains(word.toLowerCase()));
        if (!flagged) return;

        for (Player p : getServer().getOnlinePlayers()) {
            if (p.hasPermission("chattracker.moderate")) {
                p.sendMessage("§c[ChatTracker] Potential abuse by " + player.getName() + ": " + message);
            }
        }

        try {
            File folder = new File(getDataFolder(), "chatlogs");
            if (!folder.exists() && !folder.mkdirs()) getLogger().warning("Failed to create chatlogs folder!");
            String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            File flaggedFile = new File(folder, "flags-" + date + ".txt");
            try (FileWriter flaggedWriter = new FileWriter(flaggedFile, true)) {
                flaggedWriter.write("[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + "] "
                        + player.getName() + ": " + message + "\n");
            }
        } catch (IOException e) {
            getLogger().severe("Error writing flagged logs: " + e.getMessage());
        }
    }

    private void sendStaffChatMessage(Player sender, String message) {
        String group = tempRanks.getOrDefault(sender.getName().toLowerCase(),
                getConfig().getString("ranks." + sender.getName().toLowerCase() + ".group", "default"));
        String prefix = getConfig().getString("ranks." + group + ".prefix", "");
        String suffix = getConfig().getString("ranks." + group + ".suffix", "");
        String colorCode = getConfig().getString("ranks." + group + ".color", "&f");

        String fullMessage = ChatColor.translateAlternateColorCodes('&',
                "§8[§bStaffChat§8] " + prefix + ChatColor.translateAlternateColorCodes('&', colorCode)
                        + sender.getName() + suffix + "§7: §f" + message);

        for (Player p : getServer().getOnlinePlayers()) {
            if (p.hasPermission("chattracker.staffchat")) {
                p.sendMessage(fullMessage);
            }
        }
        getLogger().info("[StaffChat] " + sender.getName() + ": " + message);
    }

    // ----- Commands -----
    private class StaffChatCommand implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Only players can use this command.");
                return true;
            }
            if (!player.hasPermission("chattracker.staffchat")) {
                player.sendMessage("§cYou don't have permission to use Staff Chat.");
                return true;
            }

            if (args.length == 0) {
                if (staffChatToggled.contains(player.getUniqueId())) {
                    staffChatToggled.remove(player.getUniqueId());
                    player.sendMessage("§cStaff Chat toggled OFF.");
                } else {
                    staffChatToggled.add(player.getUniqueId());
                    player.sendMessage("§aStaff Chat toggled ON.");
                }
            } else {
                sendStaffChatMessage(player, String.join(" ", args));
            }
            return true;
        }
    }

    private class SetRankCommand implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Only players can use this command.");
                return true;
            }
            if (!player.hasPermission("chattracker.setrank")) {
                player.sendMessage("§cYou don't have permission to use this command.");
                return true;
            }
            if (args.length != 2) {
                player.sendMessage("§eUsage: /setrank <player> <group>");
                return true;
            }

            String target = args[0].toLowerCase();
            String group = args[1].toLowerCase();
            if (!getConfig().getConfigurationSection("ranks").getKeys(false).contains(group)) {
                player.sendMessage("§cThat group does not exist!");
                return true;
            }
            tempRanks.put(target, group);
            player.sendMessage("§a" + target + " is now temporarily in group: " + group);
            return true;
        }
    }

    private class ResetRankCommand implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Only players can use this command.");
                return true;
            }
            if (!player.hasPermission("chattracker.setrank")) {
                player.sendMessage("§cYou don't have permission to use this command.");
                return true;
            }
            if (args.length != 1) {
                player.sendMessage("§eUsage: /resetrank <player>");
                return true;
            }

            tempRanks.remove(args[0].toLowerCase());
            player.sendMessage("§a" + args[0] + " has been reset to their config-defined rank.");
            return true;
        }
    }

    private class ReportCommand implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Only players can use this command.");
                return true;
            }
            if (args.length < 2) {
                player.sendMessage("§eUsage: /report <player> <message>");
                return true;
            }

            String targetName = args[0];
            String reportedMessage = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            String reportText = "§c[Report] " + player.getName() + " reported " + targetName + ": " + reportedMessage;

            // Notify staff
            for (Player p : getServer().getOnlinePlayers()) {
                if (p.hasPermission("chattracker.moderate")) {
                    p.sendMessage(reportText);
                }
            }

            // Log report
            try {
                File folder = new File(getDataFolder(), "reports");
                if (!folder.exists() && !folder.mkdirs()) getLogger().warning("Failed to create reports folder!");
                String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                File reportFile = new File(folder, "reports-" + date + ".txt");
                try (FileWriter writer = new FileWriter(reportFile, true)) {
                    writer.write(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + " - " + reportText + "\n");
                }
            } catch (IOException e) {
                getLogger().severe("Error writing a report: " + e.getMessage());
            }

            player.sendMessage("§aYour report has been submitted. Thank you!");
            return true;
        }
    }
}






