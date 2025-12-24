package pl.endixon.sectors.proxy.util;

import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

public final class LoggerUtil {

    private static final org.slf4j.Logger LOGGER =
            LoggerFactory.getLogger("EndSectors-Proxy");

    private LoggerUtil() {
    }

    public static void info(String message) {
        LOGGER.info(message);
    }

    public static void info(String message, Throwable throwable) {
        LOGGER.info(message, throwable);
    }

    public static void warn(String message) {
        LOGGER.warn(message);
    }

    public static void error(String message) {
        LOGGER.error(message);
    }

    public static void error(String message, Throwable throwable) {
        LOGGER.error(message, throwable);
    }

    public static void debug(String message) {
        LOGGER.debug(message);
    }

    public static void info(Supplier<String> supplier) {
            LOGGER.info(supplier.get());
        }

    public static void info(String message, Object... args) {
        LOGGER.info(message, args);
    }

    public static void debug(Supplier<String> supplier) {
            LOGGER.debug(supplier.get());
        }
}
