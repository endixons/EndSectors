package pl.endixon.sectors.common.util;

import lombok.experimental.UtilityClass;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class ChatUtil {
    private static final String COLOR = "§7";
    private static final String COLOR_MARK = "§c";
    private static final String COLOR_LOGGER = "§6";
    private static final String COLOR_LOGGER_MARK = "§c";
    private static final String COLOR_TPS_GREEN = "§a";
    private static final String COLOR_TPS_YELLOW = "§e";
    private static final String COLOR_TPS_RED = "§c";
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    public static String fixColors(String message) {
        if (message == null) return "";
        return message.replace("%C", COLOR).replace("%M", COLOR_MARK).replace("&", "§");
    }

    public static String fixColorsLogger(String message) {
        if (message == null) return "";
        return message.replace("%C", COLOR_LOGGER).replace("%M", COLOR_LOGGER_MARK).replace("&", "§");
    }

    public static String fixAllColors(String message) {
        return ChatUtil.fixColors(fixHexColors(message));
    }

    public static String fixHexColors(String message) {
        if (message == null || message.isEmpty()) return "";

        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String hex = matcher.group(1);
            StringBuilder replacement = new StringBuilder("§x");

            for (char c : hex.toCharArray()) {
                replacement.append("§").append(c);
            }

            matcher.appendReplacement(buffer, replacement.toString());
        }

        matcher.appendTail(buffer);
        return buffer.toString();
    }

}
