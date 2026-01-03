/*
 *
 *  EndSectors  Non-Commercial License
 *  (c) 2025 Endixon
 *
 *  Permission is granted to use, copy, and
 *  modify this software **only** for personal
 *  or educational purposes.
 *
 *   Commercial use, redistribution, claiming
 *  this work as your own, or copying code
 *  without explicit permission is strictly
 *  prohibited.
 *
 *  Visit https://github.com/Endixon/EndSectors
 *  for more info.
 *
 */

package pl.endixon.sectors.tools.user.profile.cache;

import pl.endixon.sectors.tools.user.profile.player.PlayerProfile;

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
