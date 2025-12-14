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
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import pl.endixon.sectors.common.sector.SectorType;
import pl.endixon.sectors.common.util.ChatUtil;
import pl.endixon.sectors.paper.PaperSector;
import pl.endixon.sectors.paper.sector.Sector;
import pl.endixon.sectors.paper.user.UserManager;
import pl.endixon.sectors.paper.user.UserRedis;
import pl.endixon.sectors.paper.util.Logger;

import java.time.Duration;

@AllArgsConstructor
public class PlayerLocallyJoinListener implements Listener {

    private final PaperSector paperSector;

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        event.joinMessage(Component.empty());
        player.setCollidable(false);

        UserRedis user = UserManager.getOrCreate(player);
        Sector currentSector = paperSector.getSectorManager().getCurrentSector();

        Bukkit.getScheduler().runTask(paperSector, () -> {
            if (currentSector == null) return;

            if (user.isFirstJoin() &&
                    currentSector.getType() != SectorType.QUEUE &&
                    currentSector.getType() != SectorType.NETHER &&
                    currentSector.getType() != SectorType.END) {

                user.setFirstJoin(false);
                user.updateFromPlayer(player, currentSector);
                sendSectorTitle(player, currentSector);
                user.setLastSectorTransfer(false);
                user.setLastTransferTimestamp(System.currentTimeMillis());

                boolean success = player.teleport(
                        paperSector.getSectorManager().randomLocation(currentSector)
                );
                if (success) sendSectorTitle(player, currentSector);
                else Logger.info(() -> "Failed to teleport player " + player.getName());

            } else {
                user.applyPlayerData();
                sendSectorTitle(player, currentSector);
                user.setLastSectorTransfer(false);
                user.setLastTransferTimestamp(System.currentTimeMillis());
            }
        });
    }




        private void sendSectorTitle(Player player, Sector sector) {
        player.showTitle(Title.title(
                Component.text(ChatUtil.fixColors("")),
                Component.text(ChatUtil.fixColors("&cPołączono się na sektor " + sector.getName())),
                Title.Times.times(
                        Duration.ofMillis(500),
                        Duration.ofMillis(2000),
                        Duration.ofMillis(500)
                )
        ));
    }
}

