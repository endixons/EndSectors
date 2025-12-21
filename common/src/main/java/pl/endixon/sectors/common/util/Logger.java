package pl.endixon.sectors.common.util;

public class Logger {

    private static final String PREFIX = "%M[EndSectors-Common] ";

    public static void info(Object object) {
        System.out.println(ChatUtil.fixColorsLogger(PREFIX + "§f" + object));
    }

    public static void warn(Object object) {
        System.out.println(ChatUtil.fixColorsLogger(PREFIX + "§e" + object));
    }

    public static void error(Object object) {
        System.err.println(ChatUtil.fixColorsLogger(PREFIX + "§c" + object));
    }

    public static void info(String message, Long count, String channel) {
        String formatted = String.format("%s §f%s (sent to %d subscribers on channel %s)", ChatUtil.fixColorsLogger("%M[EndSectors-Common]"), message, count, channel);
        System.out.println(formatted);
    }

    public static void info(String message, Throwable ex) {
        String formatted = String.format("%s §f%s", ChatUtil.fixColorsLogger("%M[EndSectors-Common]"), message);
        System.out.println(formatted);
        if (ex != null) {
            System.err.println(ChatUtil.fixColorsLogger(PREFIX + "§cException stack trace:"));
            ex.printStackTrace(System.err);
        }
    }
}
