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

import java.time.Duration;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import pl.endixon.sectors.common.sector.SectorType;
import pl.endixon.sectors.paper.PaperSector;
import pl.endixon.sectors.paper.event.SectorChangeEvent;
import pl.endixon.sectors.paper.manager.SectorManager;
import pl.endixon.sectors.paper.sector.Sector;
import pl.endixon.sectors.paper.user.profile.UserProfile;
import pl.endixon.sectors.paper.user.profile.UserProfileRepository;
import pl.endixon.sectors.paper.util.ChatAdventureUtil;
import pl.endixon.sectors.paper.util.MessagesUtil;
import pl.endixon.sectors.paper.util.LoggerUtil;

public class PlayerTeleportListener implements Listener {

    private final PaperSector paperSector;

    public PlayerTeleportListener(PaperSector paperSector) {
        this.paperSector = paperSector;
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.isCancelled()) {
            return;
        }

        var config = this.paperSector.getConfiguration();

        Sector queue = paperSector.getSectorManager().getCurrentSector();

        if (queue.getType() == SectorType.QUEUE) {
            return;
        }

        if (event.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            return;
        }

        Player player = event.getPlayer();
        Location to = event.getTo();

        UserProfile user = UserProfileRepository.getUser(player).orElse(null);
        if (user == null) {
            LoggerUtil.info(() -> "UserProfile not found for player: " + player.getName() + " while attempting Ender Pearl teleport from: " + event.getFrom());
            return;
        }

        SectorManager sectorManager = paperSector.getSectorManager();
        Sector currentSector = sectorManager.getSector(player.getLocation());
        Sector targetSector = sectorManager.getSector(to);
        if (currentSector == null || targetSector == null) {
            LoggerUtil.info(() -> "Teleport aborted: currentSector or targetSector is null for player " + player.getName() + ". Current location: " + player.getLocation() + ", Target location: " + to);
            return;
        }
        if (targetSector.getType() == SectorType.SPAWN) {
            targetSector = sectorManager.find(SectorType.SPAWN);
        }

        SectorChangeEvent ev = new SectorChangeEvent(player, targetSector);
        Bukkit.getPluginManager().callEvent(ev);

        if (ev.isCancelled()) {
            return;
        }

        if (!targetSector.isOnline()) {
            player.showTitle(Title.title(
                    MessagesUtil.SECTOR_ERROR_TITLE.get(),
                    MessagesUtil.SECTOR_DISABLED_SUBTITLE.get(),
                    Title.Times.times(
                            Duration.ofMillis(500),
                            Duration.ofMillis(2000),
                            Duration.ofMillis(500))
            ));
            currentSector.knockBorder(player, config.knockBorderForce);
            return;
        }

        if (Sector.isSectorFull(targetSector)) {
            player.showTitle(Title.title(
                    MessagesUtil.SECTOR_ERROR_TITLE.get(),
                    MessagesUtil.SECTOR_FULL_SUBTITLE.get(),
                    Title.Times.times(
                            Duration.ofMillis(500),
                            Duration.ofMillis(2000),
                            Duration.ofMillis(500))
            ));
            currentSector.knockBorder(player, config.knockBorderForce);
            return;
        }

        boolean inTransfer = user.getLastSectorTransfer() > 0;
        if (System.currentTimeMillis() < user.getTransferOffsetUntil() && !inTransfer) {
            long remaining = user.getTransferOffsetUntil() - System.currentTimeMillis();
            player.showTitle(Title.title(
                    MessagesUtil.SECTOR_ERROR_TITLE.get(),
                    MessagesUtil.TITLE_WAIT_TIME.get("{SECONDS}", String.valueOf(remaining / 1000 + 1)),
                    Title.Times.times(
                            Duration.ofMillis(500),
                            Duration.ofMillis(2000),
                            Duration.ofMillis(500))
            ));
            currentSector.knockBorder(player, config.knockBorderForce);
            return;
        }

        if (System.currentTimeMillis() - user.getLastSectorTransfer() < config.transferDelayMillis) {
            return;
        }

        user.setLastSectorTransfer(true);
        user.activateTransferOffset();
        user.setLastTransferTimestamp(System.currentTimeMillis());

        paperSector.getSectorTeleport().teleportToSector(player, user, targetSector, false, false);
    }
}
