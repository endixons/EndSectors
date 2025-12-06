package pl.endixon.sectors.common.util;

import lombok.experimental.UtilityClass;

import java.util.Locale;

@UtilityClass
public class ChatUtil {
    private static final String COLOR = "§7";
    private static final String COLOR_MARK = "§c";
    private static final String COLOR_LOGGER = "§6";
    private static final String COLOR_LOGGER_MARK = "§c";
    private static final String COLOR_TPS_GREEN = "§a";
    private static final String COLOR_TPS_YELLOW = "§e";
    private static final String COLOR_TPS_RED = "§c";

    public static String fixColors(String message) {
        if (message == null) return "";
        return message.replace("%C", COLOR).replace("%M", COLOR_MARK).replace("&", "§");
    }

    public static String fixColorsLogger(String message) {
        if (message == null) return "";
        return message.replace("%C", COLOR_LOGGER).replace("%M", COLOR_LOGGER_MARK).replace("&", "§");
    }

    public static String formatTps(double tps) {
        String color = COLOR_TPS_RED;

        if (tps >= 19.0) {
            color = COLOR_TPS_GREEN;
        } else if (tps >= 16.0) {
            color = COLOR_TPS_YELLOW;
        }

        return color + String.format(Locale.US, "%.2f", Math.min(tps, 20.0));
    }
}
