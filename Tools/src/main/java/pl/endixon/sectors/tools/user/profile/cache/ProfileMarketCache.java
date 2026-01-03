package pl.endixon.sectors.tools.user.profile.cache;

import pl.endixon.sectors.tools.user.profile.player.PlayerMarketProfile;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


public class ProfileMarketCache {

    private static final Map<UUID, PlayerMarketProfile> CACHE = new ConcurrentHashMap<>();

    public static PlayerMarketProfile get(UUID id) {
        return CACHE.get(id);
    }

    public static void put(PlayerMarketProfile offer) {
        CACHE.put(offer.getId(), offer);
    }

    public static void remove(UUID id) {
        CACHE.remove(id);
    }

    public static Collection<PlayerMarketProfile> getValues() {
        return CACHE.values();
    }
}