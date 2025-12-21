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

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import lombok.NonNull;
import org.bukkit.entity.Player;

public class UserProfileRepository {

    public static Optional<UserProfile> getUser(@NonNull Player player) {
        return UserProfileCache.load(player.getName().toLowerCase()).map(UserProfile::new);
    }

    public static CompletableFuture<Optional<UserProfile>> getUserAsync(@NonNull String name) {
        return CompletableFuture.supplyAsync(() -> UserProfileCache.load(name.toLowerCase()).map(UserProfile::new));
    }

    public static CompletableFuture<UserProfile> getOrCreateAsync(@NonNull String name) {
        return getUserAsync(name).thenApply(optional -> optional.orElseGet(() -> new UserProfile(name)));
    }

    public static CompletableFuture<UserProfile> getOrCreateAsync(@NonNull Player player) {
        return getUserAsync(player.getName()).thenApply(optional -> optional.orElseGet(() -> new UserProfile(player)));
    }
}
