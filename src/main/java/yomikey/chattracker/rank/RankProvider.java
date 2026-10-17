package yomikey.chattracker.rank;

import org.bukkit.entity.Player;

/**
 * Where a player's rank formatting comes from: the plugin's own config, or LuckPerms.
 */
public interface RankProvider {

    /** Resolve formatting for an online player. Must be safe to call from the async chat thread. */
    RankInfo resolve(Player player);

    /** Short name shown in /chattracker status output. */
    String name();
}
