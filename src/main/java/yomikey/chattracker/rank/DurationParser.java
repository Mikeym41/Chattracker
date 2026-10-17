package yomikey.chattracker.rank;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Parses and formats short durations like "30m", "1h", "7d" or "1d12h". */
public final class DurationParser {

    private static final Pattern PART = Pattern.compile("(\\d+)([smhdw])");

    private DurationParser() {
    }

    /** Returns the duration in seconds, or -1 if the text isn't a valid duration. */
    public static long parseSeconds(String text) {
        if (text == null || text.isBlank()) return -1;
        String input = text.toLowerCase().trim();
        Matcher matcher = PART.matcher(input);
        long total = 0;
        int consumed = 0;
        while (matcher.find()) {
            if (matcher.start() != consumed) return -1;
            long amount = Long.parseLong(matcher.group(1));
            total += amount * switch (matcher.group(2)) {
                case "s" -> 1L;
                case "m" -> 60L;
                case "h" -> 3_600L;
                case "d" -> 86_400L;
                default -> 604_800L; // w
            };
            consumed = matcher.end();
        }
        return consumed == input.length() && total > 0 ? total : -1;
    }

    public static String format(long seconds) {
        long d = seconds / 86_400, h = (seconds % 86_400) / 3_600, m = (seconds % 3_600) / 60, s = seconds % 60;
        StringBuilder sb = new StringBuilder();
        if (d > 0) sb.append(d).append('d');
        if (h > 0) sb.append(h).append('h');
        if (m > 0) sb.append(m).append('m');
        if (s > 0 || sb.isEmpty()) sb.append(s).append('s');
        return sb.toString();
    }
}
