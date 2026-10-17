package yomikey.chattracker.rank;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The original ChatTracker behavior: ranks defined in config.yml, plus in-memory
 * temporary overrides from /setrank. Used when LuckPerms isn't installed or is disabled.
 */
public final class ConfigRankProvider implements RankProvider {

    private final JavaPlugin plugin;
    private final Map<String, String> tempRanks = new ConcurrentHashMap<>();

    public ConfigRankProvider(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public RankInfo resolve(Player player) {
        String key = player.getName().toLowerCase();
        String group = tempRanks.getOrDefault(key,
                plugin.getConfig().getString("ranks." + key + ".group", "default"));
        return fromConfig(plugin, group);
    }

    @Override
    public String name() {
        return "config.yml ranks";
    }

    public Set<String> groupNames() {
        ConfigurationSection ranks = plugin.getConfig().getConfigurationSection("ranks");
        return ranks == null ? Collections.emptySet() : ranks.getKeys(false);
    }

    public void setTempRank(String playerName, String group) {
        tempRanks.put(playerName.toLowerCase(), group.toLowerCase());
    }

    public boolean clearTempRank(String playerName) {
        return tempRanks.remove(playerName.toLowerCase()) != null;
    }

    /** Formatting for a group as defined under ranks.<group> in config.yml. */
    public static RankInfo fromConfig(JavaPlugin plugin, String group) {
        String path = "ranks." + group.toLowerCase();
        return new RankInfo(
                group,
                plugin.getConfig().getString(path + ".prefix", ""),
                plugin.getConfig().getString(path + ".suffix", ""),
                plugin.getConfig().getString(path + ".color", "&f"),
                plugin.getConfig().getString(path + ".message-color", "&f"));
    }
}
