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

            DefaultClientResources resources = DefaultClientResources.builder().ioThreadPoolSize(4).build();
            this.redisClient = RedisClient.create(resources, uri);
            ClientOptions options = ClientOptions.builder().autoReconnect(true).publishOnScheduler(true).build();
            this.redisClient.setOptions(options);
            this.connection = redisClient.connect();
            this.syncCommands = connection.sync();
            this.asyncCommands = connection.async();
            this.pubSubConnection = redisClient.connectPubSub();
        } catch (Exception e) {
            LoggerUtil.error("Redis initialization failed: " + e.getMessage());
        }
    }


    public void hset(String key, Map<String, String> map) {
        if (key == null || map == null || syncCommands == null) return;
        try {
            syncCommands.hset(key, map);
        } catch (Exception e) {
            LoggerUtil.error("Critical Sync HSET failure: " + e.getMessage());
        }
    }

    public List<String> getKeys(String pattern) {
        if (syncCommands == null) return Collections.emptyList();
        try {
            return syncCommands.keys(pattern);
        } catch (Exception e) {
            LoggerUtil.info("Redis getKeys failed for pattern " + pattern + ": " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public String hget(String key, String field) {
        if (key == null || field == null || syncCommands == null) return null;
        try {
            return syncCommands.hget(key, field);
        } catch (Exception e) {
            LoggerUtil.info("Redis hget failed for key " + key + ": " + e.getMessage());
            return null;
        }
    }

    public Map<String, String> hgetAll(String key) {
        if (key == null || syncCommands == null) return Collections.emptyMap();
        try {
            Map<String, String> result = syncCommands.hgetall(key);
            return (result == null) ? Collections.emptyMap() : result;
        } catch (Exception e) {
            LoggerUtil.info("Redis hgetAll failed for key " + key + ": " + e.getMessage());
            return Collections.emptyMap();
        }
    }

    public void addOnlinePlayer(String name) {
        if (name == null || name.isEmpty() || asyncCommands == null) return;
        onlinePlayers.add(name);
        asyncCommands.sadd("online_players", name);
    }

    public void removeOnlinePlayer(String name) {
        if (name == null || name.isEmpty() || asyncCommands == null) return;
        onlinePlayers.remove(name);
        asyncCommands.srem("online_players", name);
    }

    public void getOnlinePlayers(Consumer<List<String>> callback) {
        if (asyncCommands == null) return;
        asyncCommands.smembers("online_players").thenAccept(players -> callback.accept(new ArrayList<>(players)));
    }

    public void isPlayerOnline(String name, Consumer<Boolean> callback) {
        if (asyncCommands == null) return;
        asyncCommands.sismember("online_players", name).thenAccept(callback);
    }

    public void shutdown() {
        try {
            if (pubSubConnection != null) pubSubConnection.close();
        } catch (Exception e) {
            LoggerUtil.error("[RedisManager] Failed to close pubSubConnection: " + e.getMessage());
        }

        try {
            if (connection != null) connection.close();
        } catch (Exception e) {
            LoggerUtil.error("[RedisManager] Failed to close connection: " + e.getMessage());
        }

        try {
            if (redisClient != null) redisClient.shutdown();
        } catch (Exception e) {
            LoggerUtil.error("[RedisManager] Failed to shutdown redisClient: " + e.getMessage());
        }
    }

}