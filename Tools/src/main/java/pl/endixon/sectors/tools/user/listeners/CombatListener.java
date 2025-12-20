package pl.endixon.sectors.tools.user.listeners;


import net.kyori.adventure.title.Title;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
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

public class CombatListener implements Listener {

    private final CombatManager combatManager;
    private final SectorsAPI api;
    private static final double KNOCK_BORDER_FORCE = 1.35;

    public CombatListener(CombatManager combatManager, SectorsAPI sectorsAPI) {
        this.combatManager = combatManager;

        if (sectorsAPI == null) {
            throw new IllegalArgumentException("SectorsAPI cannot be null!");
        }
        this.api = sectorsAPI;
    }

    @EventHandler
    public void onSectorChange(SectorChangeEvent event) {
        Player player = event.getPlayer();
        boolean inCombat = combatManager.isInCombat(player);
        event.setCancelled(inCombat);
    }


    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {

        if (!(event instanceof EntityDamageByEntityEvent entityEvent)) {
            return;
        }

        Player victim = (Player) entityEvent.getEntity();
        Player attacker = (Player) entityEvent.getDamager();

        if (event.getEntity().getType() != EntityType.PLAYER
                || entityEvent.getDamager().getType() != EntityType.PLAYER) {
            return;
        }

        processCombat(attacker, victim);
    }

    private void processCombat(Player attacker, Player victim) {

        if ((victim.getGameMode() != GameMode.SURVIVAL && victim.getGameMode() != GameMode.ADVENTURE)
                || (attacker.getGameMode() != GameMode.SURVIVAL && attacker.getGameMode() != GameMode.ADVENTURE)) {
            return;
        }

        if (combatManager.isInCombat(attacker)) {
            combatManager.endCombat(attacker);
        }

        if (combatManager.isInCombat(victim)) {
            combatManager.endCombat(victim);
        }

        combatManager.startCombat(attacker, victim);
    }

    @EventHandler
    public void onMoveDuringCombat(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (!combatManager.isInCombat(player)) return;

        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
                event.getFrom().getBlockY() == event.getTo().getBlockY() &&
                event.getFrom().getBlockZ() == event.getTo().getBlockZ()) return;

        SectorManager sectorManager = api.getSectorManager();
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

        SectorManager sectorManager = api.getSectorManager();
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


    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        if (combatManager.isInCombat(player)) {
            player.sendMessage((MessagesUtil.COMBAT_NO_COMMAND.get()));
            event.setCancelled(true);
        }
    }
}
