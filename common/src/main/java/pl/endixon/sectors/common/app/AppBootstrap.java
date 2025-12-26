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

package pl.endixon.sectors.common.app;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import pl.endixon.sectors.common.Common;
import pl.endixon.sectors.common.util.AppLogger;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class AppBootstrap {

    public static void main(String[] args) {
        System.setProperty("io.lettuce.core.epoll", "false");
        Common.initInstance();
        Common app = Common.getInstance();
        AppLogger logger = app.getLogger();
        AppConfig config = loadConfig(logger);

        logger.info("  ");
        logger.info("  ");
        logger.info("========================================");
        logger.info("    EndSectors - Common App Service     ");
        logger.info("           Status: STARTING             ");
        logger.info("========================================");
        logger.info("  ");
        logger.info("  ");

        try {
            app.setAppBootstrap(true);
            logger.info(">> [1/5] Connecting to NATS Infrastructure...");
            logger.info("  ");
            app.initializeNats(
                    config.getNatsUrl(),
                    config.getNatsClientName()
            );

            logger.info("  ");
            logger.info(">> [2/5] Connecting to Redis...");
            logger.info("  ");
            app.initializeRedis(
                    config.getRedisHost(),
                    config.getRedisPort(),
                    config.getRedisPassword()
            );


            logger.info("  ");


            logger.info("  ");
            logger.info(">> [3/5] Activating Sniffer Responder...");
            logger.info("  ");
            app.getFlowLogger().enable(config.isFlowLoggerEnabled());

            logger.info("  ");
            logger.info(">> [4/5] Activating Heartbeat Responder...");
            logger.info("  ");
            app.startHeartbeat();

            logger.info("  ");
            logger.info(">> [5/5] Starting Resource Monitor (RAM & CPU)");
            logger.info("  ");
            startResourceMonitor(config.getResourceMonitorInterval());


            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("  ");
                logger.info("  ");

                logger.warn("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                logger.warn("   SHUTDOWN SIGNAL - CLEANING UP...     ");

                if (app.getHeartbeat() != null) {
                    app.getHeartbeat().stop();
                }

                app.shutdown();
                logger.info("   Safe shutdown complete. Goodbye!     ");
                logger.warn("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                logger.info("  ");
                logger.info("  ");
            }, "Common-Shutdown-Thread"));

            logger.info("  ");
            logger.info("  ");
            logger.info("----------------------------------------");
            logger.info(">>> Common App is READY and LISTENING   ");
            logger.info(">>> System is stable and operational.   ");
            logger.info("----------------------------------------");
            logger.info("  ");
            logger.info("  ");

            Thread.currentThread().join();

        } catch (Exception exception) {
            logger.info("  ");
            logger.info("  ");
            logger.error("========================================");
            logger.error(" FATAL ERROR: Initialization failed!    ");
            logger.error(" Message: " + exception.getMessage());
            logger.error("========================================");
            logger.info("  ");
            logger.info("  ");

            if (app.getHeartbeat() != null) {
                app.getHeartbeat().broadcastEmergencyStop(exception.getMessage());
            }
            exception.printStackTrace();
            try {
                Thread.sleep(200);
            } catch (InterruptedException ignored) {}

            System.exit(1);
        }
    }

    public static void startResourceMonitor(long intervalMillis) {
        Thread monitorThread = new Thread(() -> {
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            Runtime runtime = Runtime.getRuntime();

            while (true) {
                try {
                    long usedMemory = runtime.totalMemory() - runtime.freeMemory();
                    long maxMemory = runtime.maxMemory();
                    double memoryPercent = usedMemory * 100.0 / maxMemory;

                    double cpuLoad = 0;
                    try {
                        cpuLoad = ((com.sun.management.OperatingSystemMXBean) osBean).getProcessCpuLoad() * 100.0;
                    } catch (Exception ignored) {}

                    Common.getInstance().getLogger().info(String.format(
                            "[RESOURCE MONITOR] RAM: %dMB / %dMB (%.2f%%) | CPU: %.2f%%",
                            usedMemory / (1024 * 1024),
                            maxMemory / (1024 * 1024),
                            memoryPercent,
                            cpuLoad
                    ));

                    Thread.sleep(intervalMillis);
                } catch (InterruptedException ignored) {}
            }
        }, "Resource-Monitor-Thread");

        monitorThread.setDaemon(true);
        monitorThread.start();
    }


    private static AppConfig loadConfig(AppLogger logger) {
        File configFile = new java.io.File("config.json");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try {
            if (!configFile.exists()) {
                AppConfig defaultConfig = new AppConfig();
                try (java.io.FileWriter writer = new java.io.FileWriter(configFile)) {
                    gson.toJson(defaultConfig, writer);
                }
                logger.info(">> Created default config.json");
                return defaultConfig;
            }

            try (java.io.FileReader reader = new java.io.FileReader(configFile)) {
                return gson.fromJson(reader, AppConfig.class);
            }
        } catch (Exception e) {
            logger.error("Could not load config.json, using defaults: " + e.getMessage());
            return new AppConfig();
        }
    }
}
