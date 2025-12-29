/*
 *
 * EndSectors – Non-Commercial License
 * (c) 2025 Endixon
 *
 * Permission is granted to use, copy, and
 * modify this software **only** for personal
 * or educational purposes.
 *
 * Commercial use, redistribution, claiming
 * this work as your own, or copying code
 * without explicit permission is strictly
 * prohibited.
 *
 * Visit https://github.com/Endixon/EndSectors
 * for more info.
 *
 */

package pl.endixon.sectors.common.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ChatUtil {
    private static final String COLOR = "§7";
    private static final String COLOR_MARK = "§c";
    private static final String COLOR_LOGGER = "§6";
    private static final String COLOR_LOGGER_MARK = "§c";
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    public static String fixColors(String message) {
        if (message == null) {
            return "";
        }
        return message.replace("%C", COLOR).replace("%M", COLOR_MARK).replace("&", "§");
    }

    public static String fixColorsLogger(String message) {
        if (message == null) {
            return "";
        }
        return message.replace("%C", COLOR_LOGGER).replace("%M", COLOR_LOGGER_MARK).replace("&", "§");
    }

    public static String fixHexColors(String message) {
        if (message == null || message.isEmpty())
            return "";

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
