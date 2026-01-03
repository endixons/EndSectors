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

package pl.endixon.sectors.paper.user.profile;

import java.util.Map;
import java.util.Optional;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import lombok.NonNull;
import pl.endixon.sectors.common.Common;
import pl.endixon.sectors.paper.PaperSector;
import pl.endixon.sectors.paper.util.LoggerUtil;

public final class UserProfileCache {

    private static final String PREFIX = "user:";
    private static final Map<String, UserProfile> LOCAL_CACHE = new ConcurrentHashMap<>();

    private UserProfileCache() {
    }

    private static String getKey(@NonNull String name) {
        return PREFIX + name.toLowerCase();
    }

    public static void save(@NonNull UserProfile user) {
        try {
            Common.getInstance().getRedisManager().hset(getKey(user.getName()), user.toRedisMap());
        } catch (Exception e) {
            LoggerUtil.error(String.format("[ProfileCache] Critical failure during save for user '%s': %s", user.getName(), e.getMessage()));
        }
    }

    public static long getRemoteVersion(@NonNull String name) {
        try {
            String version = Common.getInstance().getRedisManager().hget(getKey(name), "dataVersion");

            if (version == null) {
                return -1L;
            }

            return Long.parseLong(version);
        } catch (NumberFormatException e) {
            LoggerUtil.warn(String.format("[ProfileCache] Corrupted dataVersion for '%s'. Value is not a valid Long.", name));
            return 0L;
        } catch (Exception e) {
            LoggerUtil.error(String.format("[ProfileCache] Redis connection error during version check for '%s'", name));
            return -2L;
        }
    }

    public static Optional<Map<String, String>> load(@NonNull String name) {
        try {
            Map<String, String> data = Common.getInstance().getRedisManager().hgetAll(getKey(name));
            if (data == null || data.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(data);
        } catch (Exception e) {
            LoggerUtil.error(String.format("[ProfileCache] Critical failure during load for user '%s': %s", name, e.getMessage()));
            return Optional.empty();
        }
    }

    public static void warmup() {
        LoggerUtil.info("[ProfileCache] Starting database warmup...");
        long start = System.currentTimeMillis();

        try {
            List<String> keys = Common.getInstance().getRedisManager().getKeys(PREFIX + "*");

            if (keys == null || keys.isEmpty()) {
                LoggerUtil.info("[ProfileCache] Warmup aborted: No profile data found in Redis.");
                return;
            }

            keys.forEach(fullKey -> {
                String name = fullKey.substring(PREFIX.length());
                load(name).ifPresent(data -> addToCache(new UserProfile(data)));
            });

            LoggerUtil.info(String.format("[ProfileCache] Warmup completed. Loaded %utils profiles in %dms.", LOCAL_CACHE.size(), (System.currentTimeMillis() - start)));
        } catch (Exception e) {
            LoggerUtil.error("[ProfileCache] Critical failure during database warmup: " + e.getMessage());
        }
    }

    public static void addToCache(@NonNull UserProfile profile) {
        LOCAL_CACHE.put(profile.getName().toLowerCase(), profile);
    }

    public static UserProfile getFromCache(@NonNull String name) {
        return LOCAL_CACHE.get(name.toLowerCase());
    }

    public static void removeFromCache(@NonNull String name) {
        LOCAL_CACHE.remove(name.toLowerCase());
    }

    public static void clearCache() {
        LOCAL_CACHE.clear();
    }
}