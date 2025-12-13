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


package pl.endixon.sectors.proxy.redis.listener;

import pl.endixon.sectors.common.packet.PacketListener;
import pl.endixon.sectors.common.packet.object.PacketRequestTeleportSector;
import pl.endixon.sectors.proxy.VelocitySectorPlugin;
import pl.endixon.sectors.proxy.manager.SectorManager;
import pl.endixon.sectors.proxy.manager.TeleportationManager;
import pl.endixon.sectors.proxy.util.Logger;

public class TeleportToSectorListener implements PacketListener<PacketRequestTeleportSector> {



    @Override
    public void handle(PacketRequestTeleportSector packet) {
        String playerName = packet.getPlayerName();
        String sectorName = packet.getSector();

        Logger.info("Otrzymano request teleportu gracza " + playerName + " do sektora " + sectorName);

        var playerOpt = VelocitySectorPlugin.getInstance().getServerInstance().getPlayer(playerName);
        var serverOpt = VelocitySectorPlugin.getInstance().getServerInstance().getServer(sectorName);

        if (playerOpt.isPresent() && serverOpt.isPresent()) {
            VelocitySectorPlugin.getInstance().getTeleportationManager().addPending(playerName);
            playerOpt.get().createConnectionRequest(serverOpt.get()).fireAndForget();
            VelocitySectorPlugin.getInstance().getServerInstance()
                    .getScheduler()
                    .buildTask(VelocitySectorPlugin.getInstance(), () ->  VelocitySectorPlugin.getInstance().getTeleportationManager().removePending(playerName))
                    .delay(2, java.util.concurrent.TimeUnit.SECONDS)
                    .schedule();
        } else {
            Logger.info("Nie udało się teleportować gracza " + playerName + " do sektora " + sectorName + " (gracz lub serwer nie istnieje)");
        }
    }
}

