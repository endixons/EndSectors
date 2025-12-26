package pl.endixon.sectors.common.app;

import pl.endixon.sectors.common.Common;
import pl.endixon.sectors.common.util.AppLogger;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

public final class AppBootstrap {

    public static void main(String[] args) {
        Common.initInstance();
        Common app = Common.getInstance();
        AppLogger logger = app.getLogger();

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

            logger.info(">> [1/4] Connecting to NATS Infrastructure...");
            logger.info("  ");
            app.initializeNats("nats://127.0.0.1:4222", "common-app");

            logger.info("  ");
            logger.info(">> [2/4] Activating Sniffer Responder...");
            logger.info("  ");
            app.getFlowLogger().enable(true);

            logger.info("  ");
            logger.info(">> [3/4] Activating Heartbeat Responder...");
            logger.info("  ");
            app.startHeartbeat();

            logger.info("  ");
            logger.info(">> [4/4] Starting Resource Monitor (RAM & CPU) every 5 minutes...");
            logger.info("  ");
            startResourceMonitor(5 * 60 * 1000);


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

}
