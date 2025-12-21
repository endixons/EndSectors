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
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import pl.endixon.sectors.paper.PaperSector;
import pl.endixon.sectors.paper.event.SectorChangeEvent;
import pl.endixon.sectors.paper.manager.SectorManager;
import pl.endixon.sectors.paper.sector.Sector;
import pl.endixon.sectors.paper.user.profile.UserProfile;
import pl.endixon.sectors.paper.user.profile.UserProfileRepository;
import pl.endixon.sectors.paper.util.ChatAdventureUtil;
import pl.endixon.sectors.paper.util.ConfigurationUtil;
import pl.endixon.sectors.paper.util.LoggerUtil;

@RequiredArgsConstructor
public class PlayerPortalListener implements Listener {

    private final PaperSector paperSector;
    private static final long TRANSFER_DELAY = 5000L;
    private static final double KNOCK_BORDER_FORCE = 1.35;
    private final ChatAdventureUtil CHAT = new ChatAdventureUtil();

    @EventHandler
    public void onPortalEnter(PlayerPortalEvent event) {
        Player player = event.getPlayer();
        UserProfile userProfile = UserProfileRepository.getUser(player).orElse(null);

        event.setCancelled(true);
        event.setCanCreatePortal(false);
        player.teleport(event.getFrom());


        if (userProfile == null) {
            LoggerUtil.info(() -> "UserProfile not found for player: " + player.getName() + " while entering portal at location: " + event.getFrom());
            return;
        }

        Bukkit.getScheduler().runTask(paperSector, () -> processPortalTransfer(player, userProfile));
    }

    private void processPortalTransfer(Player player, UserProfile userProfile) {
        SectorManager sectorManager = paperSector.getSectorManager();
        Sector currentSector = sectorManager.getCurrentSector();

        if (currentSector == null) {
            LoggerUtil.info(String.format("[PortalTransfer] Player '%s' attempted portal transfer but current sector is null.", player.getName()));
            return;
        }

        Sector targetSector;

        switch (currentSector.getType()) {
            case SPAWN -> targetSector = sectorManager.getSector("nether01");
            case NETHER -> targetSector = sectorManager.getBalancedRandomSpawnSector();
            default -> targetSector = null;
        }

        if (targetSector == null) {
            LoggerUtil.info(() -> "Target sector could not be determined for current sector: " + currentSector.getName());
            return;
        }

        SectorChangeEvent ev = new SectorChangeEvent(player, targetSector);
        Bukkit.getPluginManager().callEvent(ev);
        if (ev.isCancelled())
            return;

        if (!targetSector.isOnline()) {
            player.showTitle(Title.title(CHAT.toComponent(ConfigurationUtil.SECTOR_ERROR_TITLE), CHAT.toComponent(ConfigurationUtil.SECTOR_DISABLED_SUBTITLE), Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(2000), Duration.ofMillis(500))));
            currentSector.knockBorder(player, KNOCK_BORDER_FORCE);
            return;
        }

        if (Sector.isSectorFull(targetSector)) {
            player.showTitle(Title.title(CHAT.toComponent(ConfigurationUtil.SECTOR_ERROR_TITLE), CHAT.toComponent(ConfigurationUtil.SECTOR_FULL_SUBTITLE), Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(2000), Duration.ofMillis(500))));
            currentSector.knockBorder(player, KNOCK_BORDER_FORCE);
            return;
        }

        boolean inTransfer = userProfile.getLastSectorTransfer() > 0;
        if (System.currentTimeMillis() < userProfile.getTransferOffsetUntil() && !inTransfer) {
            long remaining = userProfile.getTransferOffsetUntil() - System.currentTimeMillis();
            player.showTitle(Title.title(CHAT.toComponent(ConfigurationUtil.SECTOR_ERROR_TITLE), CHAT.toComponent(ConfigurationUtil.TITLE_WAIT_TIME.replace("{SECONDS}", String.valueOf(remaining / 1000 + 1))), Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(2000), Duration.ofMillis(500))));
            currentSector.knockBorder(player, KNOCK_BORDER_FORCE);
            return;
        }

        if (System.currentTimeMillis() - userProfile.getLastSectorTransfer() < TRANSFER_DELAY) {
            return;
        }

        userProfile.setLastSectorTransfer(true);
        userProfile.setLastTransferTimestamp(System.currentTimeMillis());
        userProfile.activateTransferOffset();
        paperSector.getSectorTeleport().teleportToSector(player, userProfile, targetSector, false, false);
    }
}
