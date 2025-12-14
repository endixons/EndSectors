package pl.endixon.sectors.paper.user;

import pl.endixon.sectors.paper.PaperSector;
import pl.endixon.sectors.paper.util.Logger;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public final class RedisUserCache {

    private static final String PREFIX = "user:";

    private RedisUserCache() {}

    private static String getKey(String name) {
        return PREFIX + name.toLowerCase();
    }

    public static void save(UserRedis user) {
        runSafely(() -> {
            long start = System.currentTimeMillis();

            PaperSector.getInstance()
                    .getRedisService()
                    .hset(getKey(user.getName()), user.toRedisMap());

            long duration = System.currentTimeMillis() - start;
            Logger.info(() ->
                    "[RedisUserCache] Saved user '" + user.getName() + "' in " + duration + "ms"
            );
        }, () -> "[RedisUserCache] Failed to save user '" + user.getName() + "'");
    }

    public static Optional<Map<String, String>> load(String name) {
        return supplySafely(() -> {
            long start = System.currentTimeMillis();

            Map<String, String> data = PaperSector.getInstance()
                    .getRedisService()
                    .hgetAll(getKey(name));

            long duration = System.currentTimeMillis() - start;

            if (data == null || data.isEmpty()) {
                Logger.info(() ->
                        "[RedisUserCache] No Redis data found for user '" + name + "' (" + duration + "ms)"
                );
                return null;
            }

            Logger.info(() ->
                    "[RedisUserCache] Loaded user '" + name + "' from Redis in " + duration + "ms"
            );
            return data;
        }, () -> "[RedisUserCache] Failed to load user '" + name + "'");
    }

    public static void delete(String name) {
        runSafely(() -> {
            long start = System.currentTimeMillis();

            PaperSector.getInstance()
                    .getRedisService()
                    .del(getKey(name));

            long duration = System.currentTimeMillis() - start;
            Logger.info(() ->
                    "[RedisUserCache] Deleted user '" + name + "' from Redis in " + duration + "ms"
            );
        }, () -> "[RedisUserCache] Failed to delete user '" + name + "'");
    }

    private static void runSafely(Runnable action, Supplier<String> error) {
        try {
            action.run();
        } catch (Exception e) {
            Logger.info(error.get() + " (" + e.getClass().getSimpleName() + "): " + e.getMessage());
        }
    }

    private static <T> Optional<T> supplySafely(Supplier<T> action, Supplier<String> error) {
        try {
            return Optional.ofNullable(action.get());
        } catch (Exception e) {
            Logger.info(error.get() + " (" + e.getClass().getSimpleName() + "): " + e.getMessage());
            return Optional.empty();
        }
    }
}
