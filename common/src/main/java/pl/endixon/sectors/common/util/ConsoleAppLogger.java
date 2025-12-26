package pl.endixon.sectors.common.util;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public final class ConsoleAppLogger implements AppLogger {
    private final String name;
    private final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss");

    public ConsoleAppLogger(String name) {
        this.name = name;
    }

    @Override
    public void info(String message) { log("INFO", message); }

    @Override
    public void warn(String message) { log("WARN", message); }

    @Override
    public void error(String message) { log("ERROR", message); }

    private void log(String level, String msg) {System.out.printf("[%s] [%s] %s - %s%n", LocalTime.now().format(timeFormat), level, name, msg);
    }
}