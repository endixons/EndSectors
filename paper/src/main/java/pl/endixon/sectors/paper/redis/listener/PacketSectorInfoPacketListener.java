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

import pl.endixon.sectors.common.packet.PacketListener;
import pl.endixon.sectors.paper.PaperSector;
import pl.endixon.sectors.paper.redis.packet.PacketSectorInfo;
import pl.endixon.sectors.paper.sector.Sector;
import pl.endixon.sectors.paper.sector.SectorManager;

public class PacketSectorInfoPacketListener implements PacketListener<PacketSectorInfo> {



    @Override
    public void handle(PacketSectorInfo packet) {

        Sector sector = PaperSector.getInstance().getSectorManager().getSector(packet.getSector());

        if (sector != null) {
            sector.setLastInfoPacket();
            sector.setTPS(packet.getTPS());
            sector.setPlayerCount(packet.getPlayerCount());
            sector.setMaxPlayers(packet.getMaxPlayers());
        }
    }
}

