package pl.endixon.sectors.paper.listener.player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import pl.endixon.sectors.common.util.ChatUtil;
import pl.endixon.sectors.paper.PaperSector;
import pl.endixon.sectors.paper.sector.Sector;
import pl.endixon.sectors.paper.sector.SectorManager;
import pl.endixon.sectors.paper.user.UserManager;
import pl.endixon.sectors.common.sector.SectorType;
import pl.endixon.sectors.paper.user.UserMongo;
import pl.endixon.sectors.paper.util.Configuration;

import java.time.Duration;

public class PlayerTeleportListener implements Listener {

    private final PaperSector paperSector;

    public PlayerTeleportListener(PaperSector paperSector) {
        this.paperSector = paperSector;
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.isCancelled()) return;

        Sector queue = paperSector.getSectorManager().getCurrentSector();
        if (queue.getType() == SectorType.QUEUE) return;
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL) return;

        Player player = event.getPlayer();
        Location to = event.getTo();


        UserMongo user = UserManager.getUser(player);
            if (user == null) return;

            SectorManager sectorManager = paperSector.getSectorManager();
            Sector currentSector = sectorManager.getSector(player.getLocation());
            Sector targetSector = sectorManager.getSector(to);
            if (currentSector == null || targetSector == null) return;

            if (targetSector.getType() == SectorType.SPAWN) {
                targetSector = sectorManager.find(SectorType.SPAWN);
            }

            if (!targetSector.isOnline()) {
                player.showTitle(Title.title(
                        Component.text(ChatUtil.fixColors(Configuration.SECTOR_DISABLED_TITLE)),
                        Component.text(ChatUtil.fixColors(Configuration.SECTOR_DISABLED_SUBTITLE)),
                        Title.Times.times(
                                Duration.ofMillis(500),
                                Duration.ofMillis(2000),
                                Duration.ofMillis(500)
                        )
                ));
                currentSector.knockBorder(player, 1.5);
                return;
            }

            if (System.currentTimeMillis() - user.getLastSectorTransfer() < 3000L) return;
            user.setLastSectorTransfer(true);
            paperSector.getSectorTeleportService().teleportToSector(player, user, targetSector, false);
    }
}
