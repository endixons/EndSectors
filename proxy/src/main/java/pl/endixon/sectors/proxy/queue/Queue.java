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

package pl.endixon.sectors.proxy.queue;

import com.velocitypowered.api.proxy.Player;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.Getter;

public class Queue {

    @Getter
    private String sector;
    private final List<Player> players;

    public Queue(String sector) {
        this.sector = sector;
        this.players = new CopyOnWriteArrayList<>();
    }

    public List<Player> getPlayers() {
        return players;
    }

    public synchronized boolean addPlayer(Player player) {
        if (players.stream().anyMatch(p -> p.getUsername().equals(player.getUsername()))) {
            return false;
        }
        players.add(player);
        return true;
    }

    public void removePlayer(Player player) {
        players.removeIf(p -> p.getUsername().equals(player.getUsername()));
    }

    public boolean hasPlayer(Player player) {
        return players.stream().anyMatch(p -> p.getUsername().equals(player.getUsername()));
    }

    public void setSector(String sector) {
        this.sector = sector;
    }
}
