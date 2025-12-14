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


package pl.endixon.sectors.paper.listener.player;

import lombok.AllArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
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
import pl.endixon.sectors.common.util.ChatUtil;
import pl.endixon.sectors.paper.PaperSector;
import pl.endixon.sectors.paper.sector.Sector;
import pl.endixon.sectors.paper.sector.SectorManager;
import pl.endixon.sectors.paper.user.UserManager;
import pl.endixon.sectors.paper.user.UserRedis;
import pl.endixon.sectors.paper.util.Configuration;

@AllArgsConstructor
public class PlayerSectorInteractListener implements Listener {


    private final SectorManager sectorManager;
    private final PaperSector PaperSector;


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

        if (sector == null || sector.getType() == SectorType.QUEUE) return;

        int dist = sector.getBorderDistance(loc);
        boolean allowed = p.hasPermission("endsectors.border.break");

        if (!allowed && dist <= Configuration.BREAK_BORDER_DISTANCE) {
            event.setCancelled(true);
            p.sendMessage(ChatUtil.fixColors(Configuration.BREAK_BORDER_DISTANCE_MESSAGE));
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Location loc = event.getBlock().getLocation();
        Sector sector = sectorManager.getSector(loc);

        if (sector == null || sector.getType() == SectorType.QUEUE) return;

        Player player = event.getPlayer();
        int distanceToBorder = sector.getBorderDistance(loc);
        boolean canPlace = player.hasPermission("endsectors.border.place");

        if (!canPlace && distanceToBorder <= Configuration.PLACE_BORDER_DISTANCE) {
            event.setCancelled(true);
            player.sendMessage(ChatUtil.fixColors(Configuration.PLACE_BORDER_DISTANCE_MESSAGE));
        }
    }


    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        Sector queue = PaperSector.getSectorManager().getCurrentSector();
        if (queue.getType() == SectorType.QUEUE)
            return;

        if (sectorManager.getCurrentSector().getBorderDistance(event.getBlock().getLocation()) <= Configuration.EXPLOSION_BORDER_DISTANCE) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        Sector queue = PaperSector.getSectorManager().getCurrentSector();
        if (queue.getType() == SectorType.QUEUE)
            return;

        if (sectorManager.getCurrentSector().getBorderDistance(event.getLocation()) <= Configuration.EXPLOSION_BORDER_DISTANCE) {
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
        if (PaperSector.getSectorManager().getCurrentSector().getBorderDistance(loc) <= Configuration.BUCKET_BORDER_DISTANCE) {
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
        if (PaperSector.getSectorManager().getCurrentSector().getBorderDistance(loc) <= Configuration.BUCKET_BORDER_DISTANCE) {
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
        if (sectorManager.getCurrentSector().getBorderDistance(loc) <= Configuration.BUCKET_BORDER_DISTANCE) {
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
        if (sectorManager.getCurrentSector().getBorderDistance(loc) <= Configuration.BUCKET_BORDER_DISTANCE) {
            event.setCancelled(true);
            return;
        }
        cancelIfRedirecting(player, event);
    }



    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        Sector current = PaperSector.getSectorManager().getCurrentSector();
        if (current.getType() == SectorType.QUEUE) {
            event.setCancelled(true);
            return;
        }
        cancelIfRedirecting(player, event);
    }


    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player)) return;
        Player player = (Player) event.getEntity().getShooter();
        Sector current = PaperSector.getSectorManager().getCurrentSector();
        if (current.getType() == SectorType.QUEUE) {
            event.setCancelled(true);
            return;
        }
        cancelIfRedirecting(player, event);
    }


    @EventHandler
    public void onOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        Sector current = PaperSector.getSectorManager().getCurrentSector();
        if (current.getType() == SectorType.QUEUE) {
            event.setCancelled(true);
            return;
        }
        cancelIfRedirecting(player, event);
    }


    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

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
        if (!(event.getWhoClicked() instanceof Player)) return;

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
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        Sector current = PaperSector.getSectorManager().getCurrentSector();
        if (current.getType() == SectorType.QUEUE) {
            event.setCancelled(true);
            return;
        }
        cancelIfRedirecting(player, event);
    }

    private void cancelIfRedirecting(Player player, org.bukkit.event.Cancellable event) {
        UserRedis user = UserManager.getUser(player).orElse(null);
        if (user == null) return;
        if (System.currentTimeMillis() - user.getLastSectorTransfer() < 3000L) {
            event.setCancelled(true);
        }
    }

}

