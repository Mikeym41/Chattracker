package yomikey.chattracker;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import yomikey.chattracker.rank.ConfigRankProvider;
import yomikey.chattracker.rank.DurationParser;
import yomikey.chattracker.rank.LuckPermsRankProvider;
import yomikey.chattracker.rank.RankInfo;
import yomikey.chattracker.rank.RankProvider;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class Chattracker extends JavaPlugin implements Listener {

    private final Set<UUID> staffChatToggled = new HashSet<>();

    private ConfigRankProvider configRanks;
    /** Null when LuckPerms isn't installed or is turned off in config.yml. */
    private LuckPermsRankProvider luckPermsRanks;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        saveDefaultConfig();
        configRanks = new ConfigRankProvider(this);
        setupRankProvider();

        registerCommand("chattracker", new MainCommand(), null);
        registerCommand("staffchat", new StaffChatCommand(), null);
        registerCommand("setrank", new SetRankCommand(), new RankTabCompleter(true));
        registerCommand("resetrank", new ResetRankCommand(), new RankTabCompleter(false));
        registerCommand("report", new ReportCommand(), null);
        registerCommand("punish", new PunishCommand(), null);

        getLogger().info("ChatTracker enabled! Rank source: " + activeRanks().name());
    }

    private void registerCommand(String name, CommandExecutor executor, TabCompleter completer) {
        PluginCommand command = getCommand(name);
        if (command == null) return;
        command.setExecutor(executor);
        if (completer != null) command.setTabCompleter(completer);
    }

    /** Hooks into LuckPerms if it's installed and enabled in config, otherwise uses config.yml ranks. */
    private void setupRankProvider() {
        luckPermsRanks = null;
        boolean wanted = getConfig().getBoolean("luckperms.enabled", true);
        boolean installed = getServer().getPluginManager().isPluginEnabled("LuckPerms");

        if (wanted && installed) {
            luckPermsRanks = LuckPermsRankProvider.create(this);
            if (luckPermsRanks == null) {
                getLogger().warning("LuckPerms is installed but its API isn't available. Falling back to config.yml ranks.");
            }
        } else if (wanted) {
            getLogger().info("LuckPerms not found. Using ranks from config.yml.");
        }
    }

    private RankProvider activeRanks() {
        return luckPermsRanks != null ? luckPermsRanks : configRanks;
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
        RankInfo rank = activeRanks().resolve(player);
        String fullMessage = formatName(player, rank) + "§7: §r"
                + ColorUtil.colorize(rank.messageColor()) + playerText(player, message);

        // Clickable hover message for reports
        TextComponent chatComponent = new TextComponent(TextComponent.fromLegacyText(fullMessage));
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

    /** prefix + colored name + suffix, all colorized. */
    private String formatName(Player player, RankInfo rank) {
        return ColorUtil.colorize(rank.prefix())
                + ColorUtil.colorize(rank.nameColor()) + player.getName()
                + ColorUtil.colorize(rank.suffix());
    }

    /** Players only get '&' color codes in their own messages with chattracker.chatcolor. */
    private String playerText(Player player, String message) {
        return player.hasPermission("chattracker.chatcolor") ? ColorUtil.colorize(message) : message;
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
        RankInfo rank = activeRanks().resolve(sender);
        String fullMessage = "§8[§bStaffChat§8] " + formatName(sender, rank) + "§7: §f" + playerText(sender, message);

        for (Player p : getServer().getOnlinePlayers()) {
            if (p.hasPermission("chattracker.staffchat")) {
                p.spigot().sendMessage(TextComponent.fromLegacyText(fullMessage));
            }
        }
        getLogger().info("[StaffChat] " + sender.getName() + ": " + message);
    }

    /** LuckPerms finishes its work off the main thread; send the result back on it. */
    private void replyWhenDone(CommandSender sender, CompletableFuture<String> result) {
        result.whenComplete((text, error) -> Bukkit.getScheduler().runTask(this, () -> {
            if (error != null) {
                sender.sendMessage("§cLuckPerms error: " + error.getMessage());
                getLogger().warning("LuckPerms operation failed: " + error);
            } else {
                sender.sendMessage(text);
            }
        }));
    }

    // ----- Commands -----
    private class MainCommand implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("chattracker.admin")) {
                    sender.sendMessage("§cYou don't have permission to reload ChatTracker.");
                    return true;
                }
                reloadConfig();
                setupRankProvider();
                sender.sendMessage("§aChatTracker reloaded. Rank source: §e" + activeRanks().name());
                return true;
            }
            sender.sendMessage("§aChatTracker " + getDescription().getVersion()
                    + " is working! Rank source: §e" + activeRanks().name());
            return true;
        }
    }

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
            if (!sender.hasPermission("chattracker.setrank")) {
                sender.sendMessage("§cYou don't have permission to use this command.");
                return true;
            }

            // ----- LuckPerms: real temporary group that expires on its own -----
            if (luckPermsRanks != null) {
                if (args.length < 2 || args.length > 3) {
                    sender.sendMessage("§eUsage: /setrank <player> <group> [duration, e.g. 30m, 2h, 7d]");
                    return true;
                }
                String group = args[1].toLowerCase();
                if (!luckPermsRanks.groupExists(group)) {
                    sender.sendMessage("§cLuckPerms has no group named " + group + ".");
                    return true;
                }
                String durationText = args.length == 3 ? args[2]
                        : getConfig().getString("luckperms.setrank-default-duration", "1h");
                long seconds = DurationParser.parseSeconds(durationText);
                if (seconds <= 0) {
                    sender.sendMessage("§cInvalid duration: " + durationText + ". Try 30m, 2h, 7d or 1d12h.");
                    return true;
                }
                replyWhenDone(sender, luckPermsRanks.addTemporaryGroup(args[0], group, seconds));
                return true;
            }

            // ----- No LuckPerms: original in-memory override -----
            if (args.length != 2) {
                sender.sendMessage("§eUsage: /setrank <player> <group>");
                return true;
            }
            String group = args[1].toLowerCase();
            if (!configRanks.groupNames().contains(group)) {
                sender.sendMessage("§cThat group does not exist!");
                return true;
            }
            configRanks.setTempRank(args[0], group);
            sender.sendMessage("§a" + args[0] + " is now temporarily in group: " + group);
            return true;
        }
    }

    private class ResetRankCommand implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!sender.hasPermission("chattracker.setrank")) {
                sender.sendMessage("§cYou don't have permission to use this command.");
                return true;
            }

            if (luckPermsRanks != null) {
                if (args.length < 1 || args.length > 2) {
                    sender.sendMessage("§eUsage: /resetrank <player> [group]");
                    return true;
                }
                String group = args.length == 2 ? args[1].toLowerCase() : null;
                replyWhenDone(sender, luckPermsRanks.removeTemporaryGroups(args[0], group));
                return true;
            }

            if (args.length != 1) {
                sender.sendMessage("§eUsage: /resetrank <player>");
                return true;
            }
            configRanks.clearTempRank(args[0]);
            sender.sendMessage("§a" + args[0] + " has been reset to their config-defined rank.");
            return true;
        }
    }

    /** Suggests online players for the first argument and group names for the second. */
    private class RankTabCompleter implements TabCompleter {
        private final boolean suggestDurations;

        RankTabCompleter(boolean suggestDurations) {
            this.suggestDurations = suggestDurations;
        }

        @Override
        public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
            if (!sender.hasPermission("chattracker.setrank")) return Collections.emptyList();
            if (args.length == 1) return null; // Bukkit fills in online player names

            Collection<String> options;
            if (args.length == 2) {
                options = luckPermsRanks != null ? luckPermsRanks.groupNames() : configRanks.groupNames();
            } else if (args.length == 3 && suggestDurations && luckPermsRanks != null) {
                options = List.of("30m", "1h", "12h", "1d", "7d", "30d");
            } else {
                return Collections.emptyList();
            }
            String typed = args[args.length - 1].toLowerCase();
            return options.stream().filter(o -> o.toLowerCase().startsWith(typed)).toList();
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

            // Notify staff with clickable punish options
            for (Player p : getServer().getOnlinePlayers()) {
                if (p.hasPermission("chattracker.moderate")) {
                    TextComponent msg = new TextComponent(reportText + " ");
                    TextComponent warn = new TextComponent("[WARN]");
                    warn.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
                    warn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            new ComponentBuilder("Click to WARN this player").create()));
                    warn.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/punish " + targetName + " warn"));

                    TextComponent kick = new TextComponent("[KICK]");
                    kick.setColor(net.md_5.bungee.api.ChatColor.RED);
                    kick.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            new ComponentBuilder("Click to KICK this player").create()));
                    kick.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/punish " + targetName + " kick"));

                    TextComponent ban = new TextComponent("[BAN]");
                    ban.setColor(net.md_5.bungee.api.ChatColor.DARK_RED);
                    ban.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            new ComponentBuilder("Click to BAN this player").create()));
                    ban.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/punish " + targetName + " ban"));

                    msg.addExtra(" ");
                    msg.addExtra(warn);
                    msg.addExtra(" ");
                    msg.addExtra(kick);
                    msg.addExtra(" ");
                    msg.addExtra(ban);

                    p.spigot().sendMessage(msg);
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

    private class PunishCommand implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Only players can use this command.");
                return true;
            }
            if (!player.hasPermission("chattracker.moderate")) {
                player.sendMessage("§cYou don't have permission to punish players.");
                return true;
            }
            if (args.length != 2) {
                player.sendMessage("§eUsage: /punish <player> <warn/kick/ban>");
                return true;
            }

            String targetName = args[0];
            String action = args[1].toLowerCase();
            Player target = Bukkit.getPlayerExact(targetName);

            switch (action) {
                case "warn":
                    if (target != null) target.sendMessage("§cYou have been warned by staff.");
                    player.sendMessage("§aYou warned " + targetName);
                    logPunishment(player.getName(), targetName, "WARN");
                    break;

                case "kick":
                    if (target != null) {
                        target.kickPlayer("§cYou have been kicked by staff.");
                        player.sendMessage("§aYou kicked " + targetName);
                        logPunishment(player.getName(), targetName, "KICK");
                    } else {
                        player.sendMessage("§cPlayer not found.");
                    }
                    break;

                case "ban":
                    Bukkit.getBanList(org.bukkit.BanList.Type.NAME).addBan(targetName, "Banned by staff", null, player.getName());
                    if (target != null) target.kickPlayer("§cYou have been banned by staff.");
                    player.sendMessage("§aYou banned " + targetName);
                    logPunishment(player.getName(), targetName, "BAN");
                    break;

                default:
                    player.sendMessage("§cInvalid punishment type. Use warn, kick, or ban.");
            }
            return true;
        }

        private void logPunishment(String staffName, String targetName, String action) {
            try {
                File folder = new File(getDataFolder(), "punishments");
                if (!folder.exists() && !folder.mkdirs()) getLogger().warning("Failed to create punishments folder!");
                String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                File logFile = new File(folder, "punishments-" + date + ".txt");
                try (FileWriter writer = new FileWriter(logFile, true)) {
                    writer.write(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + " - " +
                            staffName + " " + action + " " + targetName + "\n");
                }
            } catch (IOException e) {
                getLogger().severe("Error writing punishment log: " + e.getMessage());
            }
        }
    }
}
