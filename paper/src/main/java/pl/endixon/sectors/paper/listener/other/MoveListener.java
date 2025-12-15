package pl.endixon.sectors.paper.listener.other;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import pl.endixon.sectors.common.sector.SectorType;
import pl.endixon.sectors.common.util.ChatUtil;
import pl.endixon.sectors.paper.PaperSector;
import pl.endixon.sectors.paper.event.sector.SectorChangeEvent;
import pl.endixon.sectors.paper.sector.Sector;
import pl.endixon.sectors.paper.sector.SectorManager;
import pl.endixon.sectors.paper.user.UserManager;
import pl.endixon.sectors.paper.user.UserRedis;
import pl.endixon.sectors.paper.util.Configuration;

@RequiredArgsConstructor
public class MoveListener implements Listener {

    private final PaperSector paperSector;
    private static final long TRANSFER_DELAY = 5000L;
    private static final double KNOCK_BORDER_FORCE = 1.35;

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (event.isCancelled()) return;

        SectorManager sectorManager = paperSector.getSectorManager();
        Sector current = sectorManager.getCurrentSector();
        if (current == null || current.getType() == SectorType.QUEUE || current.getType() == SectorType.NETHER) return;

        UserRedis userRedis = UserManager.getUser(player).orElse(null);
        if (userRedis == null) return;

        Sector sector = sectorManager.getSector(event.getTo());
        if (sector == null) return;

        Bukkit.getScheduler().runTask(paperSector, () -> processSectorTransfer(player, userRedis, current, sector));
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        SectorManager sectorManager = paperSector.getSectorManager();
        Player player = event.getPlayer();

        Sector current = sectorManager.getCurrentSector();
        if (current == null || current.getType() == SectorType.QUEUE || current.getType() == SectorType.NETHER) return;

        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
                event.getFrom().getBlockY() == event.getTo().getBlockY() &&
                event.getFrom().getBlockZ() == event.getTo().getBlockZ()) return;

        UserRedis userRedis = UserManager.getUser(player).orElse(null);
        if (userRedis == null) return;

        Sector sector = sectorManager.getSector(event.getTo());
        if (sector == null) return;

        processSectorTransfer(player, userRedis, current, sector);
    }

    private void processSectorTransfer(Player player, UserRedis userRedis, Sector currentSector, Sector sector) {
        if (sector.getType() == SectorType.SPAWN) {
            processSpawnSectorTransfer(player, userRedis, currentSector);
            return;
        }

        if (!currentSector.equals(sector) &&
                !(currentSector.getType() == SectorType.SPAWN && sector.getType() == SectorType.SPAWN)) {

            if (!sector.isOnline()) {
                player.showTitle(Title.title(
                        Component.text(ChatUtil.fixColors(Configuration.SECTOR_DISABLED_TITLE)),
                        Component.text(ChatUtil.fixColors(Configuration.SECTOR_DISABLED_SUBTITLE)),
                        Title.Times.times(java.time.Duration.ofMillis(500),
                                java.time.Duration.ofMillis(2000),
                                java.time.Duration.ofMillis(500))
                ));
                currentSector.knockBorder(player, KNOCK_BORDER_FORCE);
                return;
            }

            if (System.currentTimeMillis() - userRedis.getLastTransferTimestamp() < TRANSFER_DELAY) {
                long remaining = TRANSFER_DELAY - (System.currentTimeMillis() - userRedis.getLastTransferTimestamp());
                player.showTitle(Title.title(
                        Component.text(
                                ChatUtil.fixHexColors("&#ef4444Sektor chwilowo niedostępny!")
                        ),
                        Component.text(
                                ChatUtil.fixHexColors("&#9ca3afOdczekaj &#7dd3fc" + (remaining / 1000 + 1) + "s")
                        ),
                        Title.Times.times(java.time.Duration.ofMillis(500),
                                java.time.Duration.ofMillis(2000),
                                java.time.Duration.ofMillis(500))
                ));
                currentSector.knockBorder(player, KNOCK_BORDER_FORCE);
                return;
            }

            if (System.currentTimeMillis() - userRedis.getLastSectorTransfer() < TRANSFER_DELAY) return;

            userRedis.setLastSectorTransfer(true);
            SectorChangeEvent ev = new SectorChangeEvent(player, sector);
            Bukkit.getPluginManager().callEvent(ev);
            if (ev.isCancelled()) return;

            paperSector.getSectorTeleportService().teleportToSector(player, userRedis, sector, false);
        }
    }

    private void processSpawnSectorTransfer(Player player, UserRedis userRedis, Sector currentSector) {
        Sector spawnToTeleport;
        try {
            spawnToTeleport = paperSector.getSectorManager().getBalancedRandomSpawnSector();
        } catch (IllegalStateException e) {
            player.showTitle(Title.title(
                    Component.text(ChatUtil.fixColors(Configuration.SECTOR_DISABLED_TITLE)),
                    Component.text(ChatUtil.fixColors(Configuration.SECTOR_DISABLED_SUBTITLE)),
                    Title.Times.times(java.time.Duration.ofMillis(500),
                            java.time.Duration.ofMillis(2000),
                            java.time.Duration.ofMillis(500))
            ));
            currentSector.knockBorder(player, KNOCK_BORDER_FORCE);
            return;
        }

        if (!spawnToTeleport.isOnline()) {
            player.showTitle(Title.title(
                    Component.text(ChatUtil.fixColors(Configuration.SECTOR_DISABLED_TITLE)),
                    Component.text(ChatUtil.fixColors(Configuration.SECTOR_DISABLED_SUBTITLE)),
                    Title.Times.times(java.time.Duration.ofMillis(500),
                            java.time.Duration.ofMillis(2000),
                            java.time.Duration.ofMillis(500))
            ));
            currentSector.knockBorder(player, KNOCK_BORDER_FORCE);
            return;
        }

        if (System.currentTimeMillis() - userRedis.getLastTransferTimestamp() < TRANSFER_DELAY) {
            long remaining = TRANSFER_DELAY - (System.currentTimeMillis() - userRedis.getLastTransferTimestamp());
            player.showTitle(Title.title(
                    Component.text(
                            ChatUtil.fixHexColors("&#ef4444Sektor chwilowo niedostępny!")
                    ),
                    Component.text(
                            ChatUtil.fixHexColors("&#9ca3afOdczekaj &#7dd3fc" + (remaining / 1000 + 1) + "s")
                    ),
                    Title.Times.times(java.time.Duration.ofMillis(500),
                            java.time.Duration.ofMillis(2000),
                            java.time.Duration.ofMillis(500))
            ));
            currentSector.knockBorder(player, KNOCK_BORDER_FORCE);
            return;
        }

        if (System.currentTimeMillis() - userRedis.getLastSectorTransfer() < TRANSFER_DELAY) return;

        userRedis.setLastSectorTransfer(true);
        SectorChangeEvent ev = new SectorChangeEvent(player, spawnToTeleport);
        Bukkit.getPluginManager().callEvent(ev);
        if (ev.isCancelled()) return;

        paperSector.getSectorTeleportService().teleportToSector(player, userRedis, spawnToTeleport, false);
    }

}
