/*
 *
 *  EndSectors  Non-Commercial License
 *  (c) 2025 Endixon
 *
 *  Permission is granted to use, copy, and
 *  modify this software **only** for personal
 *  or educational purposes.
 *
 *  Commercial use, redistribution, claiming
 *  this work as your own, or copying code
 *  without explicit permission is strictly
 *  prohibited.
 *
 *  Visit https://github.com/Endixon/EndSectors
 *  for more info.
 *
 */

package pl.endixon.sectors.paper.user.listeners;

import lombok.AllArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import pl.endixon.sectors.common.sector.SectorType;
import pl.endixon.sectors.paper.PaperSector;
import pl.endixon.sectors.paper.sector.Sector;
import pl.endixon.sectors.paper.user.profile.UserProfile;
import pl.endixon.sectors.paper.user.profile.UserProfileRepository;
import pl.endixon.sectors.paper.util.ChatAdventureUtil;
import pl.endixon.sectors.paper.util.ConfigurationUtil;

@AllArgsConstructor
public class PlayerRespawnListener implements Listener {

    private final PaperSector paperSector;
    private final ChatAdventureUtil CHAT = new ChatAdventureUtil();

    @EventHandler
    void onPlayerDeath(PlayerDeathEvent event) {
        event.deathMessage(Component.text(""));

        final Player victim = event.getEntity();
        final Sector currentSector = paperSector.getSectorManager().getCurrentSector();

        if (currentSector.getType() == SectorType.QUEUE) {
            event.setCancelled(true);
            return;
        }

        paperSector.getServer().getScheduler().scheduleSyncDelayedTask(paperSector, victim.spigot()::respawn, 1L);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        final Player player = event.getPlayer();
        final UserProfile user = UserProfileRepository.getUser(player).orElse(null);
        final Sector spawnSector = PaperSector.getInstance().getSectorManager().find(SectorType.SPAWN);
        final Sector currentSector = paperSector.getSectorManager().getCurrentSector();

        if (currentSector.getType() == SectorType.QUEUE) {
            return;
        }

        if (user == null) {
            player.kick(CHAT.toComponent(ConfigurationUtil.playerDataNotFoundMessage));
            return;
        }

        if (spawnSector == null) {
            player.kick(CHAT.toComponent(ConfigurationUtil.spawnSectorNotFoundMessage));
            return;
        }

        paperSector.getSectorTeleport().teleportToSector(player, user, spawnSector, false, false);
    }
}
