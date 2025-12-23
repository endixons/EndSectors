package pl.endixon.sectors.paper.user.profile;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import lombok.NonNull;
import org.bukkit.entity.Player;

public final class UserProfileRepository {


    private static final Map<String, UserProfile> LOCAL_CACHE = new ConcurrentHashMap<>();

    private UserProfileRepository() {}

    public static Optional<UserProfile> getUser(@NonNull Player player) {
        return getUser(player.getName());
    }

    public static Optional<UserProfile> getUser(@NonNull String name) {
        final String lowerName = name.toLowerCase();

        if (LOCAL_CACHE.containsKey(lowerName)) {
            return Optional.of(LOCAL_CACHE.get(lowerName));
        }

        return UserProfileCache.load(lowerName)
                .map(data -> {
                    final UserProfile profile = new UserProfile(data);
                    LOCAL_CACHE.put(lowerName, profile);
                    return profile;
                });
    }

    public static CompletableFuture<Optional<UserProfile>> getUserAsync(@NonNull String name) {
        return CompletableFuture.supplyAsync(() -> getUser(name));
    }


    public static void addToCache(UserProfile profile) {
        LOCAL_CACHE.put(profile.getName().toLowerCase(), profile);
    }

    public static void removeFromCache(String name) {
        LOCAL_CACHE.remove(name.toLowerCase());
    }

    public static void clearCache() {
        LOCAL_CACHE.clear();
    }
}