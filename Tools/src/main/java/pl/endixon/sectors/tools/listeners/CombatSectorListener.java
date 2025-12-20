package pl.endixon.sectors.tools.listeners;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import pl.endixon.sectors.paper.SectorsAPI;
import pl.endixon.sectors.paper.event.sector.SectorChangeEvent;
import pl.endixon.sectors.paper.sector.Sector;
import pl.endixon.sectors.paper.sector.SectorManager;
import pl.endixon.sectors.tools.manager.CombatManager;
import pl.endixon.sectors.tools.utils.MessagesUtil;

import java.time.Duration;

@RequiredArgsConstructor
public class CombatSectorListener implements Listener {

    private final CombatManager combatManager;
    private static final double KNOCK_BORDER_FORCE = 1.35;

    @EventHandler
    public void onSectorChange(SectorChangeEvent event) {
        Player player = event.getPlayer();
        boolean inCombat = combatManager.isInCombat(player);
        event.setCancelled(inCombat);
    }


    @EventHandler
    public void onMoveDuringCombat(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!combatManager.isInCombat(player)) return;

        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
                event.getFrom().getBlockY() == event.getTo().getBlockY() &&
                event.getFrom().getBlockZ() == event.getTo().getBlockZ()) return;

        SectorManager sectorManager = SectorsAPI.getInstance().getSectorManager();
        Sector current = sectorManager.getCurrentSector();
        Sector next = sectorManager.getSector(event.getTo());

        if (current == null || next == null || current.equals(next)) return;

        current.knockBorder(player, KNOCK_BORDER_FORCE);
        player.showTitle(Title.title(
                MessagesUtil.SECTOR_COMBAT_TITLE.get(),
                MessagesUtil.SECTOR_COMBAT_SUBTITLE.get(),
                Title.Times.times(
                        Duration.ofMillis(200),
                        Duration.ofSeconds(2),
                        Duration.ofMillis(200)
                )
        ));
    }


    @EventHandler
    public void onPortalEnter(PlayerPortalEvent event) {
        Player player = event.getPlayer();

        if (!combatManager.isInCombat(player)) return;


        event.setCancelled(true);
        event.setCanCreatePortal(false);
        player.teleport(event.getFrom());

        SectorManager sectorManager = SectorsAPI.getInstance().getSectorManager();
        Sector current = sectorManager.getCurrentSector();

        if (current != null) {
            current.knockBorder(player, KNOCK_BORDER_FORCE);
        }

        player.showTitle(Title.title(
                MessagesUtil.PORTAL_COMBAT_TITLE.get(),
                MessagesUtil.PORTAL_COMBAT_SUBTITLE.get(),
                Title.Times.times(
                        Duration.ofMillis(200),
                        Duration.ofSeconds(2),
                        Duration.ofMillis(200)
                )
        ));
    }


    //listener w tools
    @EventHandler
    public void onEnderPearlTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        Location to = event.getTo();

        if (event.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL) return;


        boolean inCombat = combatManager.isInCombat(player);
        boolean onSolidBlock = to.getBlock().getType().isSolid();

        if (inCombat || onSolidBlock) {
            event.setCancelled(true);
            player.teleport(event.getFrom());

            player.showTitle(Title.title(
                    MessagesUtil.PORTAL_COMBAT_TITLE.get(),
                    MessagesUtil.SECTOR_COMBAT_SUBTITLE.get(),
                    Title.Times.times(
                            Duration.ofMillis(200),
                            Duration.ofSeconds(2),
                            Duration.ofMillis(200)
                    )
            ));

        }
    }
}
