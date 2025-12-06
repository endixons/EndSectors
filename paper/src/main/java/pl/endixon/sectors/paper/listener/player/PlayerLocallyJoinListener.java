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
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import pl.endixon.sectors.common.util.ChatUtil;
import pl.endixon.sectors.paper.PaperSector;
import pl.endixon.sectors.paper.sector.Sector;
import pl.endixon.sectors.paper.user.UserMongo;
import pl.endixon.sectors.paper.user.UserManager;
import pl.endixon.sectors.paper.util.Logger;

import java.time.Duration;

@AllArgsConstructor
public class PlayerLocallyJoinListener implements Listener {

    private final PaperSector paperSector;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        event.joinMessage(Component.empty());
        player.setCollidable(false);

        UserManager.getUser(player.getName()).thenAccept(user -> {
            if (user == null) {
                UserMongo newUser = new UserMongo(player);
                UserManager.getUsers().put(player.getName().toLowerCase(), newUser);
                newUser.insert().thenRun(() ->
                        Logger.info(() -> "Inserted new player into Mongo and cache: " + player.getName()));
                user = newUser;
            }

            UserMongo finalUser = user;
            Bukkit.getScheduler().runTask(paperSector, finalUser::applyPlayerData);

            Bukkit.getScheduler().runTask(paperSector, () -> {
                Sector current = paperSector.getSectorManager().getCurrentSector();
                if (current == null) return;

                sendSectorTitle(player, current);
                finalUser.setLastSectorTransfer(false);
                finalUser.setLastTransferTimestamp(System.currentTimeMillis());

                if (finalUser.isFirstJoin()) {
                    finalUser.setFirstJoin(false);
                    finalUser.updatePlayerData(player, current);
                    player.teleportAsync(paperSector.getSectorManager().randomLocation(current))
                            .thenAccept(success -> {
                                if (success) {
                                    sendSectorTitle(player, current);
                                } else {
                                    Logger.info(() -> "Failed to teleport player " + player.getName());
                                }
                            });
                }
            });
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

