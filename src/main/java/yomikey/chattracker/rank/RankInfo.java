package yomikey.chattracker.rank;

/**
 * Everything ChatTracker needs to format a player's name in chat.
 * Color strings use '&' codes (and &#RRGGBB hex) and are colorized at render time.
 */
public record RankInfo(String group, String prefix, String suffix, String nameColor, String messageColor) {

    public static RankInfo empty(String group) {
        return new RankInfo(group, "", "", "&f", "&f");
    }
}
