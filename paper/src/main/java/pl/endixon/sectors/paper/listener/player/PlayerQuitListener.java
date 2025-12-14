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

package pl.endixon.sectors.paper.listener.player;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import pl.endixon.sectors.common.sector.SectorType;
import pl.endixon.sectors.paper.PaperSector;
import pl.endixon.sectors.paper.sector.Sector;
import pl.endixon.sectors.paper.user.UserManager;
import pl.endixon.sectors.paper.user.UserRedis;

public class PlayerQuitListener implements Listener {

    @EventHandler
    public void onQuitPlayer(final PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UserRedis user = UserManager.getUser(player).orElse(null);
        if (user == null) return;

        Sector currentSector = PaperSector.getInstance().getSectorManager().getCurrentSector();
        if (currentSector == null || currentSector.getType() == SectorType.QUEUE) return;
        if (System.currentTimeMillis() - user.getLastSectorTransfer() < 3000L) return;

        user.updateAndSave(player, currentSector);
    }
}
