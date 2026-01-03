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
    private Map<String, Long> pageExpirations = new HashMap<>();
    private int unlockedPages = 1;

    public PlayerBackpackProfile(UUID uuid, String ownerName) {
        this.uuid = uuid;
        this.ownerName = ownerName;
        this.pages = new HashMap<>();
        this.pageExpirations = new HashMap<>();
        this.pageExpirations.put("1", System.currentTimeMillis() + (1000L * 60 * 60 * 24 * 365 * 50));
    }
}