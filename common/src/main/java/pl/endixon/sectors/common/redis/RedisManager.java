package pl.endixon.sectors.common.redis;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.resource.DefaultClientResources;
import java.nio.CharBuffer;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import pl.endixon.sectors.common.util.LoggerUtil;

public class RedisManager {

    private RedisClient redisClient;
    private StatefulRedisConnection<String, String> connection;
    private StatefulRedisPubSubConnection<String, String> pubSubConnection;
    private RedisCommands<String, String> syncCommands;
    private RedisAsyncCommands<String, String> asyncCommands;
    private final Set<String> onlinePlayers = Collections.synchronizedSet(new HashSet<>());

    public void initialize(String host, int port, String password) {
        try {
            RedisURI uri = RedisURI.builder()
                    .withHost(host)
                    .withPort(port)
                    .withPassword(CharBuffer.wrap(password))
                    .withDatabase(0)
                    .build();

            DefaultClientResources resources = DefaultClientResources.builder()
                    .ioThreadPoolSize(4)
                    .build();

            this.redisClient = RedisClient.create(resources, uri);

            ClientOptions options = ClientOptions.builder()
                    .autoReconnect(true)
                    .publishOnScheduler(true)
                    .build();

            this.redisClient.setOptions(options);
            this.connection = this.redisClient.connect();
            this.syncCommands = this.connection.sync();
            this.asyncCommands = this.connection.async();
            this.pubSubConnection = this.redisClient.connectPubSub();

            LoggerUtil.info("Redis initialized. Ready for duty.");
        } catch (Exception e) {
            LoggerUtil.error("Redis initialization failed: " + e.getMessage());
        }
    }


    public void hset(String key, Map<String, String> map) {

        if (key == null || map == null || this.syncCommands == null) {
            return;
        }

        try {
            this.syncCommands.hset(key, map);
        } catch (Exception e) {
            LoggerUtil.error("Redis Sync HSET failure: " + e.getMessage());
        }
    }

    public String hget(String key, String field) {

        if (key == null || field == null || this.syncCommands == null) {
            return null;
        }

        try {
            return this.syncCommands.hget(key, field);

        } catch (Exception e) {
            LoggerUtil.info("Redis hget failed: " + e.getMessage());
            return null;
        }
    }

    public Map<String, String> hgetAll(String key) {

        if (key == null || this.syncCommands == null) {
            return Collections.emptyMap();
        }

        try {
            Map<String, String> result = this.syncCommands.hgetall(key);
            return (result != null) ? result : Collections.emptyMap();

        } catch (Exception e) {
            LoggerUtil.info("Redis hgetAll failed for key " + key + ": " + e.getMessage());
            return Collections.emptyMap();
        }
    }


    public void hdel(String key, String... fields) {
        if (key == null || fields == null || this.syncCommands == null) {
            return;
        }

        try {
            this.syncCommands.hdel(key, fields);
        } catch (Exception e) {
            LoggerUtil.error("Redis Sync HDEL failed: " + e.getMessage());
        }
    }


    public void hsetAsync(String key, String field, String value) {
        if (key == null || field == null || this.asyncCommands == null) {
            return;
        }

        this.asyncCommands.hset(key, field, value).exceptionally(ex -> {
            LoggerUtil.error("Async HSET failed: " + ex.getMessage());
            return null;
        });
    }

    public void addOnlinePlayer(String name) {
        if (name == null || name.isEmpty() || this.asyncCommands == null) {
            return;
        }

        this.onlinePlayers.add(name);
        this.asyncCommands.sadd("online_players", name);
    }

    public void removeOnlinePlayer(String name) {
        if (name == null || name.isEmpty() || this.asyncCommands == null) {
            return;
        }

        this.onlinePlayers.remove(name);
        this.asyncCommands.srem("online_players", name);
    }


    public void getOnlinePlayers(Consumer<List<String>> callback) {
        if (asyncCommands == null) return;
        asyncCommands.smembers("online_players").thenAccept(players -> callback.accept(new ArrayList<>(players)));
    }

    public void isPlayerOnline(String name, Consumer<Boolean> callback) {
        if (asyncCommands == null) return;
        asyncCommands.sismember("online_players", name).thenAccept(callback);
    }


    public void updateProxyCountAsync(String proxyId, int count) {
        this.hsetAsync("proxy_online", proxyId, String.valueOf(count));
    }

    public CompletableFuture<Integer> getGlobalOnlineCountAsync() {

        if (this.asyncCommands == null) {
            return CompletableFuture.completedFuture(0);
        }

        return this.asyncCommands.hvals("proxy_online")
                .toCompletableFuture()
                .thenApply(this::calculateTotal)
                .exceptionally(ex -> {
                    LoggerUtil.error("Redis Async HVALS failed: " + ex.getMessage());
                    return 0;
                });
    }


    private int calculateTotal(Collection<String> values) {
        if (values == null || values.isEmpty()) {
            return 0;
        }

        return values.stream()
                .mapToInt(this::safeParseInt)
                .sum();
    }

    private int safeParseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }


    public void shutdown() {
        try {
            if (this.pubSubConnection != null) {
                this.pubSubConnection.close();
            }

            if (this.connection != null) {
                this.connection.close();
            }

            if (this.redisClient != null) {
                this.redisClient.shutdown();
                this.redisClient.getResources().shutdown();
            }

            LoggerUtil.info("Redis shutdown complete.");
        } catch (Exception e) {
            LoggerUtil.error("[RedisManager] Shutdown failed: " + e.getMessage());
        }
    }

}