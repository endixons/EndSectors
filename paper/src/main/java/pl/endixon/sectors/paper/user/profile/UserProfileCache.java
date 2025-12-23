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
import java.util.function.Supplier;
import pl.endixon.sectors.paper.PaperSector;
import pl.endixon.sectors.paper.util.LoggerUtil;

public final class UserProfileCache {

    private static final String PREFIX = "user:";

    private UserProfileCache() {
    }

    private static String getKey(String name) {
        return PREFIX + name.toLowerCase();
    }

    public static void save(UserProfile user) {
        try {
            PaperSector.getInstance().getRedisService().hset(getKey(user.getName()), user.toRedisMap());
        } catch (Exception e) {
            LoggerUtil.info("[UserProfileCache] Failed to save user '" + user.getName() + "' (" + e.getClass().getSimpleName() + "): " + e.getMessage());
        }
    }

    public static Optional<Map<String, String>> load(String name) {
        try {
            Map<String, String> data = PaperSector.getInstance().getRedisService().hgetAll(getKey(name));
            return Optional.ofNullable(data != null && !data.isEmpty() ? data : null);
        } catch (Exception e) {
            LoggerUtil.info("[UserProfileCache] Failed to load user '" + name + "' (" + e.getClass().getSimpleName() + "): " + e.getMessage());
            return Optional.empty();
        }
    }

}
