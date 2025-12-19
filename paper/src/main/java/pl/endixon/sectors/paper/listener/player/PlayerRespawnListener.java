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

import lombok.AllArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.entity.Player;
import pl.endixon.sectors.common.sector.SectorType;
import pl.endixon.sectors.paper.PaperSector;
import pl.endixon.sectors.paper.sector.Sector;
import pl.endixon.sectors.paper.user.UserManager;
import pl.endixon.sectors.paper.user.UserRedis;
import pl.endixon.sectors.paper.util.Configuration;

@AllArgsConstructor
public class PlayerRespawnListener implements Listener {

    private final PaperSector paperSector;

    @EventHandler
    void onPlayerDeath(PlayerDeathEvent event) {
        event.deathMessage(Component.text(""));
        final Player victim = event.getEntity();
        Sector currentSector = paperSector.getSectorManager().getCurrentSector();

        if (currentSector.getType() == SectorType.QUEUE)
            return;

        paperSector.getServer().getScheduler().scheduleSyncDelayedTask(
                paperSector,
                () -> victim.spigot().respawn(),
                1L
        );
    }
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        UserRedis user = UserManager.getUser(player).orElse(null);
        final Sector sector = PaperSector.getInstance().getSectorManager().find(SectorType.SPAWN);
        Sector currentSector = paperSector.getSectorManager().getCurrentSector();


        if (currentSector.getType() == SectorType.QUEUE)
            return;

        if (user == null) {
            player.kick(Component.text(Configuration.playerDataNotFoundMessage));
            return;
        }

        if (sector == null) {
            player.kick(Component.text(Configuration.spawnSectorNotFoundMessage));
            return;
        }

        paperSector.getSectorTeleportService().teleportToSector(player, user, sector, false,false);
    }
}

