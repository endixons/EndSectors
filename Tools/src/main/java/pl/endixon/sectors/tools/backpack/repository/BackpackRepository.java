package pl.endixon.sectors.tools.backpack.repository;

import static com.mongodb.client.model.Filters.eq;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.ReplaceOptions;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import pl.endixon.sectors.tools.user.profile.cache.ProfileBackpackCache;
import pl.endixon.sectors.tools.user.profile.player.PlayerBackpackProfile;

@RequiredArgsConstructor
public class BackpackRepository {

    private final MongoCollection<PlayerBackpackProfile> collection;

    public void warmup() {
        for (PlayerBackpackProfile backpack : collection.find()) {
            ProfileBackpackCache.put(backpack);
        }
    }

    public Optional<PlayerBackpackProfile> find(UUID uuid) {
        return Optional.ofNullable(collection.find(eq("_id", uuid)).first());
    }

    public PlayerBackpackProfile create(UUID uuid, String name) {
        PlayerBackpackProfile backpack = new PlayerBackpackProfile(uuid, name);
        this.save(backpack);
        return backpack;
    }

    public void save(PlayerBackpackProfile backpack) {
        collection.replaceOne(eq("_id", backpack.getUuid()), backpack, new ReplaceOptions().upsert(true));
        ProfileBackpackCache.put(backpack);
    }

    public void refreshCache(UUID uuid) {
        this.find(uuid).ifPresent(ProfileBackpackCache::put);
    }
}