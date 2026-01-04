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
import net.kyori.adventure.text.format.NamedTextColor;
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
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import pl.endixon.sectors.common.sector.SectorType;
import pl.endixon.sectors.paper.PaperSector;
import pl.endixon.sectors.paper.manager.SectorManager;
import pl.endixon.sectors.paper.sector.Sector;
import pl.endixon.sectors.paper.user.profile.UserProfile;
import pl.endixon.sectors.paper.user.profile.UserProfileRepository;
import pl.endixon.sectors.paper.util.MessagesUtil;
import pl.endixon.sectors.paper.util.LoggerUtil;

@AllArgsConstructor
public class PlayerSectorInteractListener implements Listener {

    private final SectorManager sectorManager;
    private final PaperSector PaperSector;

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        Sector current = PaperSector.getSectorManager().getCurrentSector();

        if (current != null) {
            if (current.getType() == SectorType.AFK) {
                if (!player.hasPermission("endsectors.admin") && !event.getMessage().toLowerCase().startsWith("/spawn")) {
                    event.setCancelled(true);
                }
                return;
            }

            if (current.getType() == SectorType.QUEUE && !player.hasPermission("endsectors.admin")) {
                event.setCancelled(true);
            }
        }
    }


    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Sector current = PaperSector.getSectorManager().getCurrentSector();

        if (current != null && current.getType() == SectorType.AFK) {
            if (!player.hasPermission("endsectors.admin")) {
                event.setCancelled(true);
            }
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

        if (!allowed && dist <= this.PaperSector.getConfiguration().breakBorderDistance) {
            event.setCancelled(true);
            p.sendMessage(MessagesUtil.BREAK_BORDER_DISTANCE_MESSAGE.get());
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

        if (!canPlace && distanceToBorder <= this.PaperSector.getConfiguration().placeBorderDistance) {
            event.setCancelled(true);
            player.sendMessage(MessagesUtil.PLACE_BORDER_DISTANCE_MESSAGE.get());
        }
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        Sector queue = PaperSector.getSectorManager().getCurrentSector();
        if (queue.getType() == SectorType.QUEUE) {
            event.setCancelled(true);
            return;
        }

        if (sectorManager.getCurrentSector().getBorderDistance(event.getBlock().getLocation()) <= this.PaperSector.getConfiguration().explosionBorderDistance) {
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

        if (sectorManager.getCurrentSector().getBorderDistance(event.getLocation()) <= this.PaperSector.getConfiguration().explosionBorderDistance) {
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
        if (PaperSector.getSectorManager().getCurrentSector().getBorderDistance(loc) <= this.PaperSector.getConfiguration().bucketBorderDistance) {
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
        if (PaperSector.getSectorManager().getCurrentSector().getBorderDistance(loc) <= this.PaperSector.getConfiguration().bucketBorderDistance) {
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

        if (sectorManager.getCurrentSector().getBorderDistance(loc) <= this.PaperSector.getConfiguration().dropItemBorderDistance) {
            event.setCancelled(true);
            return;
        }
        cancelIfRedirecting(player, event);
    }

    @EventHandler
    public void onPickupItem(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        Sector current = PaperSector.getSectorManager().getCurrentSector();
        Location loc = player.getLocation();

        if (current.getType() == SectorType.QUEUE) {
            event.setCancelled(true);
            return;
        }

        if (sectorManager.getCurrentSector().getBorderDistance(loc) <= PaperSector.getConfiguration().dropItemBorderDistance) {
            event.setCancelled(true);
            return;
        }

        cancelIfRedirecting(player, event);
    }


    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {

        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        Sector current = PaperSector.getSectorManager().getCurrentSector();
        if (current == null) {
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
        if (!(event.getEntity().getShooter() instanceof Player player)) {
            return;
        }

        Sector current = PaperSector.getSectorManager().getCurrentSector();
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

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        Sector current = PaperSector.getSectorManager().getCurrentSector();

        if (current.getType() == SectorType.QUEUE) {
            event.setCancelled(true);
            return;
        }

        cancelIfRedirecting(player, event);
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        Sector current = PaperSector.getSectorManager().getCurrentSector();

        if (current.getType() == SectorType.QUEUE) {
            event.setCancelled(true);
            return;
        }
        cancelIfRedirecting(player, event);
    }

    @EventHandler
    public void onInteract(InventoryInteractEvent event) {

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        Sector current = PaperSector.getSectorManager().getCurrentSector();

        if (current.getType() == SectorType.QUEUE) {
            event.setCancelled(true);
            return;
        }
        cancelIfRedirecting(player, event);
    }



    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        Sector current = PaperSector.getSectorManager().getCurrentSector();
        if (current == null || current.getType() == SectorType.QUEUE) {
            event.setCancelled(true);
            return;
        }

        if (event.getInventory().getType() == InventoryType.SHULKER_BOX) {
            Location loc = player.getLocation();
            int borderDistance = sectorManager.getCurrentSector().getBorderDistance(loc);
            if (borderDistance <= PaperSector.getConfiguration().dropItemBorderDistance) {
                event.setCancelled(true);
                return;
            }
        }

        cancelIfRedirecting(player, event);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        Sector current = PaperSector.getSectorManager().getCurrentSector();
        if (current == null || current.getType() == SectorType.QUEUE) {
            event.setCancelled(true);
            return;
        }

        if (event.getClickedInventory() != null &&
                event.getClickedInventory().getType() == InventoryType.SHULKER_BOX) {
            Location loc = player.getLocation();
            int borderDistance = sectorManager.getCurrentSector().getBorderDistance(loc);
            if (borderDistance <= PaperSector.getConfiguration().dropItemBorderDistance) {
                event.setCancelled(true);
                return;
            }
        }

        cancelIfRedirecting(player, event);
    }



    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        Sector current = PaperSector.getSectorManager().getCurrentSector();
        if (current == null || current.getType() == SectorType.QUEUE) {
            event.setCancelled(true);
            return;
        }

        if (event.getInventory().getType() == InventoryType.SHULKER_BOX) {
            Location loc = player.getLocation();
            int borderDistance = sectorManager.getCurrentSector().getBorderDistance(loc);
            if (borderDistance <= PaperSector.getConfiguration().dropItemBorderDistance) {
                event.setCancelled(true);
                return;
            }
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

        if (timeSinceLastTransfer < this.PaperSector.getConfiguration().protectionAfterTransferMillis) {
            event.setCancelled(true);
        }

}
}
