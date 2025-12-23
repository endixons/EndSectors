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

package pl.endixon.sectors.paper.user.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import pl.endixon.sectors.paper.PaperSector;
import pl.endixon.sectors.paper.manager.SectorManager;
import pl.endixon.sectors.paper.util.MessagesUtil;

public class PlayerLoginListener implements Listener {

    private final PaperSector paperSector;

    public PlayerLoginListener(PaperSector paperSector) {
        this.paperSector = paperSector;
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        SectorManager sectorManager = paperSector.getSectorManager();

        if (sectorManager.getSectors().isEmpty()) {
            paperSector.getLogger().warning("No sectors available. Kicking player %s".formatted(player.getName()));;
            event.kickMessage((MessagesUtil.SectorNotFoundMessage.get()));
            event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
        }
    }
}
