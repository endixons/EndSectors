package pl.endixon.sectors.paper.user;

import lombok.NonNull;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class UserManager {


    public static Optional<UserRedis> getUser(@NonNull Player player) {
        return RedisUserCache.load(player.getName().toLowerCase())
                .map(UserRedis::new);
    }

    public static CompletableFuture<Optional<UserRedis>> getUserAsync(@NonNull String name) {
        return CompletableFuture.supplyAsync(() ->
                RedisUserCache.load(name.toLowerCase()).map(UserRedis::new)
        );
    }

    public static UserRedis getOrCreate(@NonNull Player player) {
        return getUser(player).orElseGet(() -> new UserRedis(player));
    }

    public static CompletableFuture<UserRedis> getOrCreateAsync(@NonNull String name) {
        return getUserAsync(name).thenApply(optional -> optional.orElseGet(() -> new UserRedis(name)));
    }

    public static CompletableFuture<UserRedis> getOrCreateAsync(@NonNull Player player) {
        return getUserAsync(player.getName()).thenApply(optional -> optional.orElseGet(() -> new UserRedis(player)));
    }
}
