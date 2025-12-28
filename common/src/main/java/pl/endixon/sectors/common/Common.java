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

package pl.endixon.sectors.common;

import lombok.Getter;
import lombok.Setter;
import pl.endixon.sectors.common.app.AppHeartbeat;
import pl.endixon.sectors.common.nats.NatsManager;
import pl.endixon.sectors.common.redis.RedisManager;
import pl.endixon.sectors.common.util.AppLogger;
import pl.endixon.sectors.common.util.ConsoleAppLogger;
import pl.endixon.sectors.common.util.PacketFlowLoggerUtil;

@Getter
@Setter
public final class Common {

    private static Common instance;
    private final RedisManager redisManager;
    private final NatsManager natsManager;
    private final PacketFlowLoggerUtil flowLogger;
    private final AppHeartbeat heartbeat;
    private final AppLogger logger;
    private boolean AppBootstrap = false;

    public Common() {
        if (instance != null) {
            throw new IllegalStateException("Common already initialized! Enterprise systems hate duplicates.");
        }
        instance = this;
        this.logger = new ConsoleAppLogger("EndSectors-Common");
        this.redisManager = new RedisManager();
        this.natsManager = new NatsManager();
        this.flowLogger = new PacketFlowLoggerUtil();
        this.heartbeat = new AppHeartbeat(natsManager);
    }

    public static void initInstance() {
        if (instance == null) {
            new Common();
        }
    }

    public static Common getInstance() {
        if (instance == null) {
            initInstance();
        }
        return instance;
    }

    public void initializeRedis(String host, int port, String password) {
        this.logger.info("Initializing Redis at " + host + ":" + port);
        this.redisManager.initialize(host, port, password);
        this.logger.info("Redis initialized successfully!");
    }

    public void initializeNats(String url, String connectionName) {
        this.logger.info("Initializing NATS connection '" + connectionName + "' at " + url);
        this.natsManager.initialize(url, connectionName);
        this.logger.info("NATS initialized successfully!");
    }

    public void startHeartbeat() {
        this.heartbeat.start();
    }

    public void shutdown() {
        this.logger.warn("Starting graceful shutdown sequence...");
        this.heartbeat.stop();
        this.logger.info("Heartbeat stopped.");
        this.natsManager.shutdown();
        this.logger.info("NATS connection closed.");
        this.redisManager.shutdown();
        this.logger.info("Redis connection closed.");
        this.logger.info("Shutdown complete. System is offline.");
    }
}