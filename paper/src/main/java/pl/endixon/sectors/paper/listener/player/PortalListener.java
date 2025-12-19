package pl.endixon.sectors.paper.listener.player;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import pl.endixon.sectors.common.util.ChatUtil;
import pl.endixon.sectors.paper.PaperSector;
import pl.endixon.sectors.paper.event.sector.SectorChangeEvent;
import pl.endixon.sectors.paper.sector.Sector;
import pl.endixon.sectors.paper.sector.SectorManager;
import pl.endixon.sectors.paper.user.UserManager;
import pl.endixon.sectors.common.sector.SectorType;
import pl.endixon.sectors.paper.user.UserRedis;
import pl.endixon.sectors.paper.util.Configuration;
import pl.endixon.sectors.paper.util.Logger;

import java.time.Duration;

@RequiredArgsConstructor
public class PortalListener implements Listener {

    private final PaperSector paperSector;
    private static final long TRANSFER_DELAY = 5000L;
    private static final double KNOCK_BORDER_FORCE = 1.35;

    @EventHandler
    public void onPortalEnter(PlayerPortalEvent event) {
        Player player = event.getPlayer();
        event.setCancelled(true);
        player.setPortalCooldown(0);
        player.resetCooldown();

        UserRedis userRedis = UserManager.getUser(player).orElse(null);
        if (userRedis == null) return;

        Bukkit.getScheduler().runTask(paperSector, () -> processPortalTransfer(player, userRedis));
    }

    private void processPortalTransfer(Player player, UserRedis userRedis) {
        SectorManager sectorManager = paperSector.getSectorManager();
        Sector current = sectorManager.getCurrentSector();
        if (current == null) return;

        if (System.currentTimeMillis() < userRedis.getTransferOffsetUntil()) {
            long remaining = userRedis.getTransferOffsetUntil() - System.currentTimeMillis();
            player.showTitle(Title.title(
                    Component.text(Configuration.TITLE_SECTOR_UNAVAILABLE),
                    Component.text(Configuration.TITLE_WAIT_TIME.replace("{SECONDS}", String.valueOf(remaining / 1000 + 1))),
                    Title.Times.times(Duration.ofMillis(500),
                            Duration.ofMillis(2000),
                            Duration.ofMillis(500))
            ));
            current.knockBorder(player, KNOCK_BORDER_FORCE);
            return;
        }

        if (System.currentTimeMillis() - userRedis.getLastSectorTransfer() < TRANSFER_DELAY) return;

        userRedis.setLastSectorTransfer(true);
        userRedis.activateTransferOffset();
        userRedis.setLastTransferTimestamp(System.currentTimeMillis());

        Sector targetSector = null;
        if (current.getType() == SectorType.SPAWN) {
            targetSector = sectorManager.getSector("nether01");
        } else if (current.getType() == SectorType.NETHER) {
            targetSector = sectorManager.getBalancedRandomSpawnSector();
        }

        if (targetSector == null || !targetSector.isOnline()) {
            Logger.info("Could not find a valid sector to transfer the player: " + player.getName());
            player.showTitle(Title.title(
                    Component.text(ChatUtil.fixColors(Configuration.SECTOR_DISABLED_TITLE)),
                    Component.text(ChatUtil.fixColors(Configuration.SECTOR_DISABLED_SUBTITLE)),
                    Title.Times.times(Duration.ofMillis(500),
                            Duration.ofMillis(2000),
                            Duration.ofMillis(500))
            ));
            current.knockBorder(player, KNOCK_BORDER_FORCE);
            return;
        }

        SectorChangeEvent ev = new SectorChangeEvent(player, targetSector);
        Bukkit.getPluginManager().callEvent(ev);
        if (ev.isCancelled()) return;

        paperSector.getSectorTeleportService().teleportToSector(player, userRedis, targetSector, false, false);
    }
}
