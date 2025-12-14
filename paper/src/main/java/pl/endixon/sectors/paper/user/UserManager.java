package pl.endixon.sectors.paper.user;

import lombok.NonNull;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;
import pl.endixon.sectors.common.sector.SectorType;
import pl.endixon.sectors.common.util.ChatUtil;
import pl.endixon.sectors.paper.PaperSector;
import pl.endixon.sectors.paper.sector.Sector;
import pl.endixon.sectors.paper.util.Logger;

import java.time.Duration;
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
