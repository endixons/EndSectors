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

package pl.endixon.sectors.tools.user.listeners;

import java.time.Duration;

import net.kyori.adventure.title.Title;
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
import pl.endixon.sectors.paper.event.SectorChangeEvent;
import pl.endixon.sectors.paper.manager.SectorManager;
import pl.endixon.sectors.paper.sector.Sector;
import pl.endixon.sectors.tools.manager.CombatManager;
import pl.endixon.sectors.tools.utils.MessagesUtil;

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
    public void onPlayerDeath(org.bukkit.event.entity.PlayerDeathEvent event) {
        Player victim = event.getEntity();
        if (combatManager.isInCombat(victim)) {
            combatManager.endCombat(victim);
        }
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

        if (event.getEntity().getType() != EntityType.PLAYER || entityEvent.getDamager().getType() != EntityType.PLAYER) {
            return;
        }

        Player victim = (Player) entityEvent.getEntity();
        Player attacker = (Player) entityEvent.getDamager();
        processCombat(attacker, victim);
    }

    private void processCombat(Player attacker, Player victim) {
        if (!combatManager.canStartCombat(attacker, victim)) {
            return;
        }
        combatManager.startCombat(attacker, victim);
    }

    @EventHandler
    public void onMoveDuringCombat(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (!combatManager.isInCombat(player)) {
            return;
        }

        if (event.getFrom().getBlockX() == event.getTo().getBlockX() && event.getFrom().getBlockY() == event.getTo().getBlockY() && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        SectorManager sectorManager = api.getSectorManager();
        Sector current = sectorManager.getCurrentSector();
        Sector next = sectorManager.getSector(event.getTo());

        if (current == null || next == null || current.equals(next)) {
            return;
        }

        current.knockBorder(player, KNOCK_BORDER_FORCE);

        player.showTitle(Title.title(MessagesUtil.SECTOR_COMBAT_TITLE.get(), MessagesUtil.SECTOR_COMBAT_SUBTITLE.get(), Title.Times.times(Duration.ofMillis(200), Duration.ofSeconds(2), Duration.ofMillis(200))));
    }

    @EventHandler
    public void onPortalEnter(PlayerPortalEvent event) {
        Player player = event.getPlayer();

        if (!combatManager.isInCombat(player))
            return;

        event.setCancelled(true);
        event.setCanCreatePortal(false);
        player.teleport(event.getFrom());

        SectorManager sectorManager = api.getSectorManager();
        Sector current = sectorManager.getCurrentSector();

        if (current != null) {
            current.knockBorder(player, KNOCK_BORDER_FORCE);
        }

        player.showTitle(Title.title(MessagesUtil.PORTAL_COMBAT_TITLE.get(), MessagesUtil.PORTAL_COMBAT_SUBTITLE.get(), Title.Times.times(Duration.ofMillis(200), Duration.ofSeconds(2), Duration.ofMillis(200))));
    }

    @EventHandler
    public void onEnderPearlTeleport(PlayerTeleportEvent event) {
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            return;
        }

        Player player = event.getPlayer();

        if (!combatManager.isInCombat(player)) {
            return;
        }

        Location from = event.getFrom();
        Location to = event.getTo();

        SectorManager sectorManager = api.getSectorManager();
        Sector current = sectorManager.getSector(from);
        Sector next = sectorManager.getSector(to);

        if (current == null || next == null || current.equals(next)) {
            return;
        }
        event.setCancelled(true);

        player.showTitle(Title.title(MessagesUtil.SECTOR_COMBAT_TITLE.get(), MessagesUtil.SECTOR_COMBAT_SUBTITLE.get(), Title.Times.times(Duration.ofMillis(200), Duration.ofSeconds(2), Duration.ofMillis(200))));
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
