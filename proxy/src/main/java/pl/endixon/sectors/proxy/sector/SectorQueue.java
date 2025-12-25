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

package pl.endixon.sectors.proxy.sector;

import com.velocitypowered.api.proxy.Player;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.Getter;
import lombok.Setter;

@Getter
public class SectorQueue {

    @Setter
    private String sector;
    private final List<Player> players;

    public SectorQueue(final String sector) {
        this.sector = sector;
        this.players = new CopyOnWriteArrayList<>();
    }

    public void addPlayer(final Player player) {
        if (this.hasPlayer(player)) {
            return;
        }
        this.players.add(player);
    }

    public void removePlayer(final Player player) {
        this.players.removeIf(p -> p.getUniqueId().equals(player.getUniqueId()));
    }

    public boolean hasPlayer(final Player player) {
        return this.players.stream().anyMatch(p -> p.getUniqueId().equals(player.getUniqueId()));
    }
}