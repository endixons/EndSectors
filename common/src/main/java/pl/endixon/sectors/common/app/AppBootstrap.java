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

            logger.info(">> [1/3] Connecting to NATS Infrastructure...");
            logger.info("  ");
            app.initializeNats("nats://127.0.0.1:4222", "common-app");

            logger.info("  ");
            logger.info(">> [2/3] Activating Sniffer Responder...");
            logger.info("  ");
            app.getFlowLogger().enable(true);

            logger.info("  ");
            logger.info(">> [3/3] Activating Heartbeat Responder...");
            logger.info("  ");
            app.startHeartbeat();

            new Thread(() -> {
                OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
                Runtime runtime = Runtime.getRuntime();

                while (true) {
                    try {
                        long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024;
                        long maxMemory = runtime.maxMemory() / 1024 / 1024;
                        double cpuLoad = -1;

                        if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
                            cpuLoad = ((com.sun.management.OperatingSystemMXBean) osBean).getProcessCpuLoad() * 100;
                        }

                        logger.info(String.format("SYSTEM STATS | RAM: %d/%d MB | CPU: %.2f%%",
                                usedMemory, maxMemory, cpuLoad));

                        Thread.sleep(5000);
                    } catch (InterruptedException ignored) {}
                }
            }, "System-Monitor-Thread").start();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
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
            logger.error("========================================");
            logger.error(" FATAL ERROR: Initialization failed!    ");
            logger.error(" Message: " + exception.getMessage());
            logger.error("========================================");
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
}
