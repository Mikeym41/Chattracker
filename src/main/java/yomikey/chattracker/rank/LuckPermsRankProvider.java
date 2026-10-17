package yomikey.chattracker.rank;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Reads chat formatting from LuckPerms and manages temporary groups through it.
 *
 * Only this class touches the LuckPerms API, and it is only loaded when LuckPerms
 * is installed, so ChatTracker still runs fine on servers without it.
 *
 * Resolution order for each value:
 *   prefix / suffix   -> LuckPerms prefix/suffix  -> config ranks.<group>  -> ""
 *   name color        -> LuckPerms meta "name-color"    -> config ranks.<group>.color -> &f
 *   message color     -> LuckPerms meta "message-color" -> config ranks.<group>.message-color -> &f
 * where <group> is the player's LuckPerms primary group.
 */
public final class LuckPermsRankProvider implements RankProvider {

    public static final String NAME_COLOR_META = "name-color";
    public static final String MESSAGE_COLOR_META = "message-color";

    private final JavaPlugin plugin;
    private final LuckPerms luckPerms;

    public LuckPermsRankProvider(JavaPlugin plugin, LuckPerms luckPerms) {
        this.plugin = plugin;
        this.luckPerms = luckPerms;
    }

    /** Returns null if the LuckPerms service isn't registered (e.g. it failed to enable). */
    public static LuckPermsRankProvider create(JavaPlugin plugin) {
        var registration = plugin.getServer().getServicesManager().getRegistration(LuckPerms.class);
        return registration == null ? null : new LuckPermsRankProvider(plugin, registration.getProvider());
    }

    @Override
    public RankInfo resolve(Player player) {
        CachedMetaData meta = luckPerms.getPlayerAdapter(Player.class).getMetaData(player);
        String group = meta.getPrimaryGroup() != null ? meta.getPrimaryGroup() : "default";
        RankInfo fallback = ConfigRankProvider.fromConfig(plugin, group);
        boolean usePrefixes = plugin.getConfig().getBoolean("luckperms.use-prefixes", true);

        String prefix = usePrefixes ? firstNonNull(meta.getPrefix(), fallback.prefix()) : fallback.prefix();
        String suffix = usePrefixes ? firstNonNull(meta.getSuffix(), fallback.suffix()) : fallback.suffix();
        String nameColor = firstNonNull(meta.getMetaValue(NAME_COLOR_META), fallback.nameColor());
        String messageColor = firstNonNull(meta.getMetaValue(MESSAGE_COLOR_META), fallback.messageColor());

        return new RankInfo(group, prefix, suffix, nameColor, messageColor);
    }

    @Override
    public String name() {
        return "LuckPerms";
    }

    public boolean groupExists(String group) {
        return luckPerms.getGroupManager().getGroup(group) != null;
    }

    public Set<String> groupNames() {
        return luckPerms.getGroupManager().getLoadedGroups().stream()
                .map(Group::getName)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    /**
     * Adds the player to a group for a limited time. LuckPerms removes it automatically
     * when it expires, and the change persists across restarts and syncs across a network.
     * Completes with a message describing the result.
     */
    public CompletableFuture<String> addTemporaryGroup(String playerName, String group, long seconds) {
        return lookup(playerName).thenCompose(uuid -> {
            if (uuid == null) {
                return CompletableFuture.completedFuture("§cLuckPerms has no record of player " + playerName + ".");
            }
            InheritanceNode node = InheritanceNode.builder(group)
                    .expiry(seconds, TimeUnit.SECONDS)
                    .build();
            return luckPerms.getUserManager()
                    .modifyUser(uuid, user -> user.data().add(node))
                    .thenApply(v -> "§a" + playerName + " is now in group §e" + group
                            + "§a for §e" + DurationParser.format(seconds) + "§a (via LuckPerms).");
        });
    }

    /**
     * Removes temporary group memberships. With a group name, only that group;
     * without one, every temporary group the player has. Permanent groups are never touched.
     */
    public CompletableFuture<String> removeTemporaryGroups(String playerName, String groupOrNull) {
        return lookup(playerName).thenCompose(uuid -> {
            if (uuid == null) {
                return CompletableFuture.completedFuture("§cLuckPerms has no record of player " + playerName + ".");
            }
            return luckPerms.getUserManager().loadUser(uuid).thenCompose(user -> {
                List<InheritanceNode> toRemove = user.getNodes(NodeType.INHERITANCE).stream()
                        .filter(InheritanceNode::hasExpiry)
                        .filter(n -> groupOrNull == null || n.getGroupName().equalsIgnoreCase(groupOrNull))
                        .toList();
                if (toRemove.isEmpty()) {
                    String which = groupOrNull == null ? "temporary groups" : "temporary membership in " + groupOrNull;
                    return CompletableFuture.completedFuture("§e" + playerName + " has no " + which + ".");
                }
                toRemove.forEach(n -> user.data().remove(n));
                String names = toRemove.stream().map(InheritanceNode::getGroupName).collect(Collectors.joining(", "));
                return luckPerms.getUserManager().saveUser(user)
                        .thenApply(v -> "§aRemoved temporary group(s) from " + playerName + ": §e" + names);
            });
        });
    }

    private CompletableFuture<UUID> lookup(String playerName) {
        Player online = plugin.getServer().getPlayerExact(playerName);
        if (online != null) {
            return CompletableFuture.completedFuture(online.getUniqueId());
        }
        return luckPerms.getUserManager().lookupUniqueId(playerName);
    }

    private static String firstNonNull(String value, String fallback) {
        return value != null ? value : fallback;
    }
}
