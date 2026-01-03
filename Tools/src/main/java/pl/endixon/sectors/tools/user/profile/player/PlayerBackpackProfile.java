package pl.endixon.sectors.tools.user.profile.player;

import lombok.*;
import org.bson.codecs.pojo.annotations.BsonId;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlayerBackpackProfile {

    @BsonId
    private UUID uuid;
    private String ownerName;
    private Map<String, String> pages = new HashMap<>();
    private int unlockedPages = 1;

    public PlayerBackpackProfile(UUID uuid, String ownerName) {
        this.uuid = uuid;
        this.ownerName = ownerName;
        this.pages = new HashMap<>();
    }
}