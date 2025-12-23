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
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
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

@RequiredArgsConstructor
public class PlayerMoveListener implements Listener {

    private final PaperSector paperSector;

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();

        if (event.isCancelled()) {
            return;
        }

        SectorManager sectorManager = paperSector.getSectorManager();
        Sector current = sectorManager.getCurrentSector();

        if (current == null || current.getType() == SectorType.QUEUE || current.getType() == SectorType.NETHER) {
            return;
        }

        UserProfile userProfile = UserProfileRepository.getUser(player).orElse(null);
        if (userProfile == null) {
            LoggerUtil.info(() -> "UserProfile not found for player: " + player.getName());
            return;
        }

        Sector targetSector = sectorManager.getSector(event.getTo());
        if (targetSector == null) {

            return;
        }

        SectorChangeEvent sectorChangeEvent = new SectorChangeEvent(player, targetSector);
        Bukkit.getPluginManager().callEvent(sectorChangeEvent);

        if (sectorChangeEvent.isCancelled()) {
            return;
        }

        Bukkit.getScheduler().runTask(paperSector, () -> processSectorTransfer(player, userProfile, current, targetSector));
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        SectorManager sectorManager = paperSector.getSectorManager();
        Sector current = sectorManager.getCurrentSector();

        if (current == null || current.getType() == SectorType.QUEUE || current.getType() == SectorType.NETHER) {
            return;
        }

        if (event.getFrom().getBlockX() == event.getTo().getBlockX() && event.getFrom().getBlockY() == event.getTo().getBlockY() && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        UserProfile userProfile = UserProfileRepository.getUser(player).orElse(null);
        if (userProfile == null) {
            LoggerUtil.info(() -> "UserProfile not found for player: " + player.getName());
            return;
        }

        Sector targetSector = sectorManager.getSector(event.getTo());
        if (targetSector == null) {
            return;
        }

        processSectorTransfer(player, userProfile, current, targetSector);
    }

    private void processSectorTransfer(Player player, UserProfile userProfile, Sector currentSector, Sector sector) {
        if (sector.getType() == SectorType.SPAWN) {
            processSpawnSectorTransfer(player, userProfile, currentSector);
            return;
        }

        var config = this.paperSector.getConfiguration();

        if (!currentSector.equals(sector) && !(currentSector.getType() == SectorType.SPAWN && sector.getType() == SectorType.SPAWN)) {

            if (!sector.isOnline()) {
                Component title = MessagesUtil.SECTOR_ERROR_TITLE.get();
                Component subtitle = MessagesUtil.SECTOR_DISABLED_SUBTITLE.get();

                Title.Times times = Title.Times.times(
                        Duration.ofMillis(500),
                        Duration.ofMillis(2000),
                        Duration.ofMillis(500)
                );
                player.showTitle(Title.title(title, subtitle, times));
                currentSector.knockBorder(player, config.knockBorderForce);
                return;
            }

            if (Sector.isSectorFull(sector)) {
                player.showTitle(Title.title(
                        MessagesUtil.SECTOR_ERROR_TITLE.get(),
                        MessagesUtil.SECTOR_FULL_SUBTITLE.get(),
                        Title.Times.times(Duration.ofMillis(500),
                                Duration.ofMillis(2000),
                                Duration.ofMillis(500))
                ));

                currentSector.knockBorder(player, config.knockBorderForce);
                return;
            }

            boolean inTransfer = userProfile.getLastSectorTransfer() > 0;

            if (System.currentTimeMillis() < userProfile.getTransferOffsetUntil() && !inTransfer) {
                long remaining = userProfile.getTransferOffsetUntil() - System.currentTimeMillis();
                player.showTitle(Title.title(
                        MessagesUtil.SECTOR_ERROR_TITLE.get(),
                        MessagesUtil.TITLE_WAIT_TIME.get("{SECONDS}", String.valueOf(remaining)),
                        Title.Times.times(
                                Duration.ofMillis(500),
                                Duration.ofMillis(2000),
                                Duration.ofMillis(500))
                ));
                currentSector.knockBorder(player, config.knockBorderForce);
                return;
            }

            if (System.currentTimeMillis() - userProfile.getLastSectorTransfer() < config.transferDelayMillis) {
                return;
            }

            userProfile.setLastSectorTransfer(true);
            userProfile.setLastTransferTimestamp(System.currentTimeMillis());
            userProfile.activateTransferOffset();

            SectorChangeEvent ev = new SectorChangeEvent(player, sector);
            Bukkit.getPluginManager().callEvent(ev);
            if (ev.isCancelled())
                return;

            paperSector.getSectorTeleport().teleportToSector(player, userProfile, sector, false, false);
        }
    }

    private void processSpawnSectorTransfer(Player player, UserProfile userProfile, Sector currentSector) {

        Sector spawnToTeleport = paperSector.getSectorManager().getBalancedRandomSpawnSector();
        var config = this.paperSector.getConfiguration();

        if (spawnToTeleport == null) {
            player.sendMessage(MessagesUtil.spawnSectorNotFoundMessage.get());
            player.showTitle(Title.title(
                    MessagesUtil.SECTOR_ERROR_TITLE.get(),
                    MessagesUtil.spawnSectorNotFoundMessage.get(),
                    Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(2000), Duration.ofMillis(500))
            ));
            currentSector.knockBorder(player, config.knockBorderForce);
            return;
        }

        if (!spawnToTeleport.isOnline()) {
            player.showTitle(Title.title(
                    MessagesUtil.SECTOR_ERROR_TITLE.get(),
                    MessagesUtil.SECTOR_DISABLED_SUBTITLE.get(),
                    Title.Times.times(
                            Duration.ofMillis(500),
                            Duration.ofMillis(2000),
                            Duration.ofMillis(500))
            ));
            currentSector.knockBorder(player, config.knockBorderForce); return;
        }

        if (Sector.isSectorFull(spawnToTeleport)) {
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

        boolean inTransfer = userProfile.getLastSectorTransfer() > 0;
        if (System.currentTimeMillis() < userProfile.getTransferOffsetUntil() && !inTransfer) {
            long remaining = userProfile.getTransferOffsetUntil() - System.currentTimeMillis();
            player.showTitle(Title.title(
                    MessagesUtil.SECTOR_ERROR_TITLE.get(),
                    MessagesUtil.TITLE_WAIT_TIME.get("{SECONDS}", String.valueOf(remaining / 1000 + 1)),
                    Title.Times.times(
                            Duration.ofMillis(500),
                            Duration.ofMillis(2000),
                            Duration.ofMillis(500))
            )); currentSector.knockBorder(player, config.knockBorderForce);
            return;
        }

        if (System.currentTimeMillis() - userProfile.getLastSectorTransfer() < config.transferDelayMillis) {
            return;
        }

        userProfile.setLastSectorTransfer(true);
        userProfile.activateTransferOffset();
        userProfile.setLastTransferTimestamp(System.currentTimeMillis());

        SectorChangeEvent ev = new SectorChangeEvent(player, spawnToTeleport);
        Bukkit.getPluginManager().callEvent(ev);

        if (ev.isCancelled()) {
            return;
        }

        paperSector.getSectorTeleport().teleportToSector(player, userProfile, spawnToTeleport, false, false);
    }
}
