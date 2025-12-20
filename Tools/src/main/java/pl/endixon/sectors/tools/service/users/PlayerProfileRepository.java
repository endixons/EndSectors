package pl.endixon.sectors.tools.service.users;

import com.mongodb.client.MongoCollection;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import static com.mongodb.client.model.Filters.eq;

@RequiredArgsConstructor
public class PlayerProfileRepository {

    private final MongoCollection<PlayerProfile> collection;

    public Optional<PlayerProfile> find(UUID uuid) {
        return Optional.ofNullable(
                collection.find(eq("_id", uuid)).first()
        );
    }

    public PlayerProfile create(UUID uuid, String name) {
        PlayerProfile profile = new PlayerProfile(
                uuid, name, 0, 0, new HashMap<>(), 0L
        );
        collection.insertOne(profile);
        return profile;
    }


    public void save(PlayerProfile profile) {
        collection.replaceOne(eq("_id", profile.getUuid()), profile);
    }
}
