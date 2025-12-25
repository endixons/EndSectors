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

package pl.endixon.sectors.proxy.manager;

import com.velocitypowered.api.proxy.Player;
import pl.endixon.sectors.proxy.sector.SectorQueue;
import pl.endixon.sectors.proxy.sector.SectorQueueRegistry;

import java.util.Optional;

public class QueueManager extends SectorQueueRegistry<String, SectorQueue> {

    public Optional<SectorQueue> findQueueByPlayer(Player player) {
        return this.getMap().values().stream().filter(sectorQueue -> sectorQueue.hasPlayer(player)).findFirst();
    }
}
