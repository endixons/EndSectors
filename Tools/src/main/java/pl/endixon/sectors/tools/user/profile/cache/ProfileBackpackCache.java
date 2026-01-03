package pl.endixon.sectors.tools.user.profile.cache;

import pl.endixon.sectors.tools.user.profile.player.PlayerBackpackProfile;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ProfileBackpackCache {

    private static final Map<UUID, PlayerBackpackProfile> CACHE = new ConcurrentHashMap<>();

    public static PlayerBackpackProfile get(UUID uuid) {
        return CACHE.get(uuid);
    }

    public static void put(PlayerBackpackProfile backpack) {
        CACHE.put(backpack.getUuid(), backpack);
    }

    public static void remove(UUID uuid) {
        CACHE.remove(uuid);
    }

    public static Collection<PlayerBackpackProfile> getValues() {
        return CACHE.values();
    }

    public static void clear() {
        CACHE.clear();
    }
}