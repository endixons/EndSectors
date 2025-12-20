package pl.endixon.sectors.tools.service.users;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ProfileCache {

    private static final Map<UUID, PlayerProfile> CACHE = new ConcurrentHashMap<>();

    public static PlayerProfile get(UUID uuid) {
        return CACHE.get(uuid);
    }

    public static void put(PlayerProfile profile) {
        CACHE.put(profile.getUuid(), profile);
    }

    public static void remove(UUID uuid) {
        CACHE.remove(uuid);
    }
}
