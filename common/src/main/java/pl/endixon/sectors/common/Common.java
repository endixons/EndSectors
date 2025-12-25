package pl.endixon.sectors.common;

import lombok.Getter;
import pl.endixon.sectors.common.nats.NatsManager;
import pl.endixon.sectors.common.redis.RedisManager;
import pl.endixon.sectors.common.util.LoggerUtil;

@Getter
public final class Common {

    @Getter
    private static Common instance;
    private final RedisManager redisManager;
    private final NatsManager natsManager;

    public Common() {
        if (instance != null) {
            throw new IllegalStateException("Common already initialized");
        }
        instance = this;
        this.redisManager = new RedisManager();
        this.natsManager = new NatsManager();
    }

    public void initializeRedis(String host, int port, String password) {
        LoggerUtil.info("Initializing Redis at " + host + ":" + port);
        this.redisManager.initialize(host, port, password);
        LoggerUtil.info("Redis initialized successfully!");
    }

    public void initializeNats(String url, String connectionName) {
        LoggerUtil.info("Initializing NATS connection '" + connectionName + "' at " + url);
        this.natsManager.initialize(url, connectionName);
        LoggerUtil.info("NATS initialized successfully!");
    }


    public static void initInstance() {
        if (instance == null) {
            new Common();
        }
    }


    public void shutdown() {
        LoggerUtil.info("Shutting down NATS...");
        natsManager.shutdown();
        LoggerUtil.info("NATS shutdown complete.");
        LoggerUtil.info("Shutting down Redis...");
        redisManager.shutdown();
        LoggerUtil.info("Redis shutdown complete.");
    }

}
