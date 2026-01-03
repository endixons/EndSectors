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

package pl.endixon.sectors.tools.user.repository;

import static com.mongodb.client.model.Filters.eq;

import com.mongodb.client.MongoCollection;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import pl.endixon.sectors.tools.user.profile.player.PlayerProfile;


@RequiredArgsConstructor
public class PlayerRepository {

    private final MongoCollection<PlayerProfile> collection;

    public Optional<PlayerProfile> find(UUID uuid) {
        return Optional.ofNullable(collection.find(eq("_id", uuid)).first());
    }

    public PlayerProfile create(UUID uuid, String name) {
        PlayerProfile profile = new PlayerProfile(
                uuid,
                name,
                0,
                0,
                new HashMap<>(),
                0L,
                0.0
        );

        collection.insertOne(profile);
        return profile;
    }


    public void save(PlayerProfile profile) {
        collection.replaceOne(eq("_id", profile.getUuid()), profile);
    }
}
