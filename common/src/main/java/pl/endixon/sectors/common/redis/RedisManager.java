package pl.endixon.sectors.common.redis;

import com.google.gson.Gson;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.sync.RedisPubSubCommands;
import io.lettuce.core.resource.DefaultClientResources;
import java.io.Serializable;
import java.nio.CharBuffer;
import java.util.*;
import java.util.function.Consumer;
import pl.endixon.sectors.common.packet.Packet;
import pl.endixon.sectors.common.packet.PacketListener;
import pl.endixon.sectors.common.util.Logger;

public class RedisManager {

    private RedisClient redisClient;
    private StatefulRedisPubSubConnection<String, String> pubSubConnection;
    private RedisAsyncCommands<String, String> publishCommands;
    private final Gson gson = new Gson();
    private final Set<String> onlinePlayers = Collections.synchronizedSet(new HashSet<>());

    public void initialize(String host, int port, String password) {

        RedisURI uri = RedisURI.builder().withHost(host).withPort(port).withPassword(CharBuffer.wrap(password)).build();

        DefaultClientResources resources = DefaultClientResources.builder().ioThreadPoolSize(4).build();
        redisClient = RedisClient.create(resources, uri);

        ClientOptions options = ClientOptions.builder().autoReconnect(true).publishOnScheduler(true).build();
        redisClient.setOptions(options);
        publishCommands = redisClient.connect().async();
        pubSubConnection = redisClient.connectPubSub();
    }

    public <T extends Serializable> void subscribe(String channel, PacketListener<T> listener, Class<T> type) {
        RedisPubSubCommands<String, String> sync = pubSubConnection.sync();
        sync.subscribe(channel);

        pubSubConnection.addListener(new RedisPubSubAdapter<>() {
            @Override
            public void message(String ch, String msg) {
                if (ch.equals(channel)) {
                    T packet = gson.fromJson(msg, type);
                    listener.handle(packet);
                }
            }
        });
    }

    public void publish(String channel, Packet packet) {
        String json = gson.toJson(packet);
        publishCommands.publish(channel, json);
    }

    public void addOnlinePlayer(String name) {
        if (name != null && !name.isEmpty()) {
            onlinePlayers.add(name);
            publishCommands.sadd("online_players", name).exceptionally(ex -> {
                Logger.info("Failed to add player to Redis online set: " + name, ex);
                return null;
            });
        }
    }

    public void removeOnlinePlayer(String name) {
        if (name != null && !name.isEmpty()) {
            onlinePlayers.remove(name);
            publishCommands.srem("online_players", name).exceptionally(ex -> {
                Logger.info("Failed to remove player from Redis online set: " + name, ex);
                return null;
            });
        }
    }

    public void getOnlinePlayers(Consumer<List<String>> callback) {
        publishCommands.smembers("online_players").thenAccept(players -> callback.accept(new ArrayList<>(players))).exceptionally(ex -> {
            Logger.info("Failed to fetch online players from Redis", ex);
            callback.accept(Collections.emptyList());
            return null;
        });
    }

    public void isPlayerOnline(String name, Consumer<Boolean> callback) {
        publishCommands.sismember("online_players", name).thenAccept(callback).exceptionally(ex -> {
            Logger.info("Failed to check if player is online in Redis: " + name, ex);
            callback.accept(false);
            return null;
        });
    }

    public void hset(String key, Map<String, String> map) {
        if (map == null || map.isEmpty())
            return;
        publishCommands.hset(key, map);
    }

    public Map<String, String> hgetAll(String key) {
        try {
            return publishCommands.hgetall(key).get();
        } catch (Exception e) {
            Logger.info("Redis hgetAll failed for key: " + key, e);
            return Collections.emptyMap();
        }
    }

    public void del(String key) {
        publishCommands.del(key);
    }

    public boolean exists(String key) {
        try {
            return publishCommands.exists(key).get() > 0;
        } catch (Exception e) {
            Logger.info("Redis exists failed: " + key, e);
            return false;
        }
    }

    public void shutdown() {
        if (pubSubConnection != null)
            pubSubConnection.close();
        if (publishCommands != null)
            publishCommands.getStatefulConnection().close();
        if (redisClient != null)
            redisClient.shutdown();
    }
}
