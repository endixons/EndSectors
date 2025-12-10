package pl.endixon.sectors.paper.listener.other;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
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
import pl.endixon.sectors.paper.user.UserMongo;
import pl.endixon.sectors.paper.util.Configuration;

@RequiredArgsConstructor
public class MoveListener implements Listener {

    private final PaperSector paperSector;

    @EventHandler
    public void onTeleport(final PlayerTeleportEvent event) {
        Sector current = paperSector.getSectorManager().getCurrentSector();
        if (current != null && (current.getType() == SectorType.QUEUE || current.getType() == SectorType.NETHER)) return;
        handlePlayerMove(event.getPlayer(), event.getFrom(), event.getTo(), true);
    }

    @EventHandler
    public void onMove(final PlayerMoveEvent event) {
        Sector current = paperSector.getSectorManager().getCurrentSector();
        if (current != null && (current.getType() == SectorType.QUEUE || current.getType() == SectorType.NETHER)) return;
        handlePlayerMove(event.getPlayer(), event.getFrom(), event.getTo(), false);
    }

    private void handlePlayerMove(Player player, Location from, Location to, boolean teleport) {

        if (from.getBlockX() == to.getBlockX() &&
                from.getBlockY() == to.getBlockY() &&
                from.getBlockZ() == to.getBlockZ()) return;

        UserMongo userMongo = UserManager.getUser(player);
            if (userMongo == null) return;

                SectorManager sectorManager = paperSector.getSectorManager();
                Sector currentSector = sectorManager.getCurrentSector();
                if (currentSector == null) return;

                Sector sector = sectorManager.getSector(to);
                if (sector == null) return;

                if (sector.getType() == SectorType.SPAWN) {
                    Sector spawnToTeleport;
                    try {
                        spawnToTeleport = sectorManager.getBalancedRandomSpawnSector();
                    } catch (IllegalStateException e) {
                        player.showTitle(Title.title(
                                Component.text(ChatUtil.fixColors(Configuration.SECTOR_DISABLED_TITLE)),
                                Component.text(ChatUtil.fixColors(Configuration.SECTOR_DISABLED_SUBTITLE)),
                                Title.Times.times(
                                        java.time.Duration.ofMillis(500),
                                        java.time.Duration.ofMillis(2000),
                                        java.time.Duration.ofMillis(500)
                                )
                        ));
                        currentSector.knockBorder(player, 1.0);
                        return;
                    }

                    if (!spawnToTeleport.isOnline()) {
                        player.showTitle(Title.title(
                                Component.text(ChatUtil.fixColors(Configuration.SECTOR_DISABLED_TITLE)),
                                Component.text(ChatUtil.fixColors(Configuration.SECTOR_DISABLED_SUBTITLE)),
                                Title.Times.times(
                                        java.time.Duration.ofMillis(500),
                                        java.time.Duration.ofMillis(2000),
                                        java.time.Duration.ofMillis(500)
                                )
                        ));
                        currentSector.knockBorder(player, 1.5);
                        return;
                    }

                    if (System.currentTimeMillis() - userMongo.getLastTransferTimestamp() < 5000L) {
                        long remaining = 5000L - (System.currentTimeMillis() - userMongo.getLastTransferTimestamp());
                        player.showTitle(Title.title(
                                Component.text(ChatUtil.fixColors("&cNie możesz połączyć się z tym sektorem teraz!")).color(NamedTextColor.RED),
                                Component.text(ChatUtil.fixColors("&7Odczekaj " + (remaining / 1000 + 1) + " sekund i spróbuj ponownie")).color(NamedTextColor.GRAY),
                                Title.Times.times(
                                        java.time.Duration.ofMillis(500),
                                        java.time.Duration.ofMillis(2000),
                                        java.time.Duration.ofMillis(500)
                                )
                        ));
                        return;
                    }

                    if (System.currentTimeMillis() - userMongo.getLastSectorTransfer() < 5000L) return;

                    userMongo.setLastSectorTransfer(true);
                    SectorChangeEvent ev = new SectorChangeEvent(player, spawnToTeleport);
                    Bukkit.getPluginManager().callEvent(ev);
                    if (ev.isCancelled()) return;

                    if (!teleport) {
                        paperSector.getSectorTeleportService().teleportToSector(player, userMongo, spawnToTeleport, false);
                    } else {
                        Bukkit.getScheduler().runTaskLater(paperSector,
                                () -> paperSector.getSectorTeleportService().teleportToSector(player, userMongo, spawnToTeleport, false),
                                0L);
                    }
                    return;
                }

                if (!currentSector.equals(sector) && !(currentSector.getType() == SectorType.SPAWN && sector.getType() == SectorType.SPAWN)) {

                    if (!sector.isOnline()) {
                        player.showTitle(Title.title(
                                Component.text(ChatUtil.fixColors(Configuration.SECTOR_DISABLED_TITLE)),
                                Component.text(ChatUtil.fixColors(Configuration.SECTOR_DISABLED_SUBTITLE)),
                                Title.Times.times(
                                        java.time.Duration.ofMillis(500),
                                        java.time.Duration.ofMillis(2000),
                                        java.time.Duration.ofMillis(500)
                                )
                        ));
                        currentSector.knockBorder(player, 1.0);
                        return;
                    }

                    if (System.currentTimeMillis() - userMongo.getLastTransferTimestamp() < 5000L) {
                        long remaining = 5000L - (System.currentTimeMillis() - userMongo.getLastTransferTimestamp());
                        player.showTitle(Title.title(
                                Component.text(ChatUtil.fixColors("&cNie możesz połączyć się z tym sektorem teraz!")).color(NamedTextColor.RED),
                                Component.text(ChatUtil.fixColors("&7Odczekaj " + (remaining / 1000 + 1) + " sekund i spróbuj ponownie")).color(NamedTextColor.GRAY),
                                Title.Times.times(
                                        java.time.Duration.ofMillis(500),
                                        java.time.Duration.ofMillis(2000),
                                        java.time.Duration.ofMillis(500)
                                )
                        ));
                        return;
                    }

                    if (System.currentTimeMillis() - userMongo.getLastSectorTransfer() < 5000L) return;

                    userMongo.setLastSectorTransfer(true);
                    SectorChangeEvent ev = new SectorChangeEvent(player, sector);
                    Bukkit.getPluginManager().callEvent(ev);
                    if (ev.isCancelled()) return;

                    if (!teleport) {
                        paperSector.getSectorTeleportService().teleportToSector(player, userMongo, sector, false);
                    } else {
                        Bukkit.getScheduler().runTaskLater(paperSector,
                                () -> paperSector.getSectorTeleportService().teleportToSector(player, userMongo, sector, false),
                                0L);
                    }
                }
    }
}
