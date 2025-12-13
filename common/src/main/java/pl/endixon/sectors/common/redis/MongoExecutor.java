package pl.endixon.sectors.common.redis;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MongoExecutor {
    // 2 wątki dla asynchronicznych operacji – wystarczające dla większości serwerów Minecraft
    public static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(2);

    public static void shutdown() {
        EXECUTOR.shutdown();
    }
}
