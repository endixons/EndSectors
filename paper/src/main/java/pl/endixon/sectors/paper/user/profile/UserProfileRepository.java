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

    import lombok.NonNull;
    import org.bukkit.entity.Player;
    import pl.endixon.sectors.paper.util.LoggerUtil;

    import java.util.Optional;
    import java.util.concurrent.CompletableFuture;

    public final class UserProfileRepository {

        private UserProfileRepository() {
        }

        public static Optional<UserProfile> getUser(@NonNull Player player) {
            return getUser(player.getName());
        }

        public static Optional<UserProfile> getUser(@NonNull String name) {
            final String key = name.toLowerCase();
            final UserProfile cached = UserProfileCache.getFromCache(key);

            if (cached == null) {
                 LoggerUtil.info(String.format("[ProfileRepo] Cache miss for '%s'. Loading from database.", key));
                return reloadFromRedis(key);
            }

            final long remoteVersion = UserProfileCache.getRemoteVersion(key);

            if (remoteVersion == -2L) {
                LoggerUtil.info(String.format("[ProfileRepo] Database unreachable. Serving stale data for '%s'.", key));
                return Optional.of(cached);
            }

            if (remoteVersion > cached.getDataVersion()) {
                LoggerUtil.info(String.format("[ProfileRepo] Version mismatch for '%s' (v%d -> v%d). Synchronizing...", key, cached.getDataVersion(), remoteVersion));
                return reloadFromRedis(key);
            }

            return Optional.of(cached);
        }

        public static Optional<UserProfile> reloadFromRedis(@NonNull String name) {
            return UserProfileCache.load(name.toLowerCase()).map(data -> {
                UserProfile profile = new UserProfile(data);
                UserProfileCache.addToCache(profile);
                return profile;
            });
        }

        public static Optional<UserProfile> getIfPresent(@NonNull String name) {
            return Optional.ofNullable(UserProfileCache.getFromCache(name));
        }

        public static CompletableFuture<Optional<UserProfile>> getUserAsync(@NonNull String name) {
            return CompletableFuture.supplyAsync(() -> getUser(name));
        }
    }