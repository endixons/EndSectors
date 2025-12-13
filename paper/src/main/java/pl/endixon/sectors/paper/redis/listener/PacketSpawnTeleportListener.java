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


package pl.endixon.sectors.paper.redis.listener;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import pl.endixon.sectors.common.packet.PacketListener;
import pl.endixon.sectors.common.packet.object.PacketSpawnTeleport;
import pl.endixon.sectors.paper.sector.SectorManager;
import pl.endixon.sectors.paper.user.UserManager;
import pl.endixon.sectors.paper.user.UserMongo;
import pl.endixon.sectors.paper.util.Logger;
import pl.endixon.sectors.paper.PaperSector;

public class PacketSpawnTeleportListener implements PacketListener<PacketSpawnTeleport> {



    @Override
    public void handle(PacketSpawnTeleport packet) {
        Player player = Bukkit.getPlayerExact(packet.getPlayerName());
        if (player == null) return;

        UserMongo user = UserManager.getUser(player);
        if (user == null) return;

        Bukkit.getScheduler().runTask(PaperSector.getInstance(), () -> {
            if (Bukkit.getWorld("world") == null) {
                Logger.info("Nie znaleziono świata 'world' dla gracza " + player.getName());
                return;
            }

            Location spawnLoc = new Location(
                    Bukkit.getWorld("world"),
                    0.5, 70, 0.5
            );

            player.teleport(spawnLoc);
            Logger.info("Gracz " + player.getName() + " został teleportowany na SPAWN: " + spawnLoc);
        });
    }
}

