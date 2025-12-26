package pl.endixon.sectors.common.app;

import pl.endixon.sectors.common.Common;
import pl.endixon.sectors.common.util.AppLogger;

public final class AppBootstrap {

    public static void main(String[] args) {
        Common.initInstance();
        Common app = Common.getInstance();
        AppLogger logger = app.getLogger();

        logger.info("========================================");
        logger.info("    EndSectors - Common App Service     ");
        logger.info("           Status: STARTING             ");
        logger.info("========================================");

        try {
            app.setAppBootstrap(true);

            logger.info(">> [1/3] Connecting to NATS Infrastructure...");
            app.initializeNats("nats://127.0.0.1:4222", "common-app");

            logger.info(">> [2/3] Activating Heartbeat Responder...");
            app.startHeartbeat();

            logger.info(">> [3/3] Enabling Traffic Monitoring...");
            app.getFlowLogger().enable();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.warn("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                logger.warn("   SHUTDOWN SIGNAL - CLEANING UP...     ");
                if (app.getHeartbeat() != null) {
                    app.getHeartbeat().stop();
                }

                app.shutdown();
                logger.info("   Safe shutdown complete. Goodbye!     ");
                logger.warn("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            }, "Common-Shutdown-Thread"));

            logger.info("----------------------------------------");
            logger.info(">>> Common App is READY and LISTENING   ");
            logger.info(">>> System is stable and operational.   ");
            logger.info("----------------------------------------");
            Thread.currentThread().join();

        } catch (Exception exception) {
            logger.error("========================================");
            logger.error(" FATAL ERROR: Initialization failed!    ");
            logger.error(" Message: " + exception.getMessage());
            logger.error("========================================");

            if (app.getHeartbeat() != null) {
                app.getHeartbeat().broadcastEmergencyStop(exception.getMessage());
            }

            exception.printStackTrace();
            try { Thread.sleep(200);
            }
            catch (InterruptedException ignored) {}
            System.exit(1);
        }
    }
}