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

package pl.endixon.sectors.tools.user.profile.player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.*;
import org.bson.codecs.pojo.annotations.BsonId;
import pl.endixon.sectors.tools.user.profile.cache.ProfileHome;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlayerProfile {
    @BsonId
    private UUID uuid;
    private String name;
    private int kills;
    private int deaths;
    private Map<String, ProfileHome> homes = new HashMap<>();
    private long combatUntil;
    private double balance;
}
