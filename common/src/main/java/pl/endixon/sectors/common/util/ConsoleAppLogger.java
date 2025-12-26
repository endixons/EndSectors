package pl.endixon.sectors.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ConsoleAppLogger implements AppLogger {

    private final Logger logger;

    public ConsoleAppLogger(String name) {
        this.logger = LoggerFactory.getLogger(name);
    }

    @Override
    public void info(String message) {
        logger.info(message);
    }

    @Override
    public void warn(String message) {
        logger.warn(message);
    }

    @Override
    public void error(String message) {
        logger.error(message);
    }
}