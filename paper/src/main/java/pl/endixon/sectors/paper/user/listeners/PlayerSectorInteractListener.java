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

import lombok.AllArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import pl.endixon.sectors.common.sector.SectorType;
import pl.endixon.sectors.paper.PaperSector;
import pl.endixon.sectors.paper.manager.SectorManager;
import pl.endixon.sectors.paper.sector.Sector;
import pl.endixon.sectors.paper.user.profile.UserProfile;
import pl.endixon.sectors.paper.user.profile.UserProfileRepository;
import pl.endixon.sectors.paper.util.ChatAdventureUtil;
import pl.endixon.sectors.paper.util.ConfigurationUtil;
import pl.endixon.sectors.paper.util.LoggerUtil;

@AllArgsConstructor
public class PlayerSectorInteractListener implements Listener {

    private final SectorManager sectorManager;
    private final PaperSector PaperSector;
    private final ChatAdventureUtil CHAT = new ChatAdventureUtil();

    @EventHandler
    public void onCommand(org.bukkit.event.player.PlayerCommandPreprocessEvent event) {
        Sector queue = PaperSector.getSectorManager().getCurrentSector();

        if (queue != null && queue.getType() == SectorType.QUEUE) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player p = event.getPlayer();
        Location loc = event.getBlock().getLocation();
        Sector sector = sectorManager.getSector(loc);

        if (sector == null || sector.getType() == SectorType.QUEUE) {
            return;
        }

        int dist = sector.getBorderDistance(loc);
        boolean allowed = p.hasPermission("endsectors.border.break");

        if (!allowed && dist <= ConfigurationUtil.BREAK_BORDER_DISTANCE) {
            event.setCancelled(true);
            p.sendMessage(CHAT.toComponent(ConfigurationUtil.BREAK_BORDER_DISTANCE_MESSAGE));
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Location loc = event.getBlock().getLocation();
        Sector sector = sectorManager.getSector(loc);
        Player player = event.getPlayer();

        if (sector == null || sector.getType() == SectorType.QUEUE) {
            return;
        }

        int distanceToBorder = sector.getBorderDistance(loc);
        boolean canPlace = player.hasPermission("endsectors.border.place");

        if (!canPlace && distanceToBorder <= ConfigurationUtil.PLACE_BORDER_DISTANCE) {
            event.setCancelled(true);
            player.sendMessage(CHAT.toComponent(ConfigurationUtil.PLACE_BORDER_DISTANCE_MESSAGE));
        }
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        Sector queue = PaperSector.getSectorManager().getCurrentSector();
        if (queue.getType() == SectorType.QUEUE) {
            event.setCancelled(true);
            return;
        }

        if (sectorManager.getCurrentSector().getBorderDistance(event.getBlock().getLocation()) <= ConfigurationUtil.EXPLOSION_BORDER_DISTANCE) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        Sector queue = PaperSector.getSectorManager().getCurrentSector();
        if (queue.getType() == SectorType.QUEUE) {
            event.setCancelled(true);
            return;
        }

        if (sectorManager.getCurrentSector().getBorderDistance(event.getLocation()) <= ConfigurationUtil.EXPLOSION_BORDER_DISTANCE) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent event) {
        Sector current = PaperSector.getSectorManager().getCurrentSector();
        Player player = event.getPlayer();

        if (current.getType() == SectorType.QUEUE) {
            event.setCancelled(true);
            return;
        }

        Location loc = event.getBlockClicked().getLocation();
        if (PaperSector.getSectorManager().getCurrentSector().getBorderDistance(loc) <= ConfigurationUtil.BUCKET_BORDER_DISTANCE) {
            event.setCancelled(true);
            return;
        }

        cancelIfRedirecting(player, event);
    }

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        Sector current = PaperSector.getSectorManager().getCurrentSector();
        Player player = event.getPlayer();

        if (current.getType() == SectorType.QUEUE) {
            event.setCancelled(true);
            return;
        }

        Location loc = event.getBlockClicked().getLocation();
        if (PaperSector.getSectorManager().getCurrentSector().getBorderDistance(loc) <= ConfigurationUtil.BUCKET_BORDER_DISTANCE) {
            event.setCancelled(true);
            return;
        }

        cancelIfRedirecting(player, event);
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        Sector current = PaperSector.getSectorManager().getCurrentSector();
        Player player = event.getPlayer();
        Location loc = player.getLocation();

        if (current.getType() == SectorType.QUEUE) {
            event.setCancelled(true);
            return;
        }

        if (sectorManager.getCurrentSector().getBorderDistance(loc) <= ConfigurationUtil.BUCKET_BORDER_DISTANCE) {
            event.setCancelled(true);
            return;
        }
        cancelIfRedirecting(player, event);
    }

    @EventHandler
    public void onPickupItem(PlayerPickupItemEvent event) {
        Sector current = PaperSector.getSectorManager().getCurrentSector();
        Player player = event.getPlayer();
        Location loc = player.getLocation();

        if (current.getType() == SectorType.QUEUE) {
            event.setCancelled(true);
            return;
        }

        if (sectorManager.getCurrentSector().getBorderDistance(loc) <= ConfigurationUtil.BUCKET_BORDER_DISTANCE) {
            event.setCancelled(true);
            return;
        }

        cancelIfRedirecting(player, event);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        Player player = (Player) event.getEntity();
        Sector current = PaperSector.getSectorManager().getCurrentSector();

        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        if (current.getType() == SectorType.QUEUE) {
            event.setCancelled(true);
            return;
        }
        cancelIfRedirecting(player, event);
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        Player player = (Player) event.getEntity().getShooter();
        Sector current = PaperSector.getSectorManager().getCurrentSector();

        if (!(event.getEntity().getShooter() instanceof Player)) {
            return;
        }

        if (current.getType() == SectorType.QUEUE) {
            event.setCancelled(true);
            return;
        }

        cancelIfRedirecting(player, event);
    }

    @EventHandler
    public void onOpen(InventoryOpenEvent event) {
        Player player = (Player) event.getPlayer();
        Sector current = PaperSector.getSectorManager().getCurrentSector();

        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        if (current.getType() == SectorType.QUEUE) {
            event.setCancelled(true);
            return;
        }

        cancelIfRedirecting(player, event);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {

        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        Sector current = PaperSector.getSectorManager().getCurrentSector();

        if (current.getType() == SectorType.QUEUE) {
            event.setCancelled(true);
            return;
        }

        cancelIfRedirecting(player, event);
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        Sector current = PaperSector.getSectorManager().getCurrentSector();

        if (current.getType() == SectorType.QUEUE) {
            event.setCancelled(true);
            return;
        }
        cancelIfRedirecting(player, event);
    }

    @EventHandler
    public void onInteract(InventoryInteractEvent event) {

        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        Sector current = PaperSector.getSectorManager().getCurrentSector();

        if (current.getType() == SectorType.QUEUE) {
            event.setCancelled(true);
            return;
        }
        cancelIfRedirecting(player, event);
    }

    private void cancelIfRedirecting(Player player, Cancellable event) {
        UserProfile user = UserProfileRepository.getUser(player).orElse(null);
        if (user == null) {
            LoggerUtil.info("[SectorRedirect] Player " + player.getName() + " has no profile, skipping cancel.");
            return;
        }

        long timeSinceLastTransfer = System.currentTimeMillis() - user.getLastSectorTransfer();

        if (timeSinceLastTransfer < 5000L) {
            event.setCancelled(true);
        }

}
}
