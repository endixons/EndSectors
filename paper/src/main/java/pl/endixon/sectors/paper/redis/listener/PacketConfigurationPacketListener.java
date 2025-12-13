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
import pl.endixon.sectors.common.packet.object.PacketConfiguration;
import pl.endixon.sectors.paper.PaperSector;
import pl.endixon.sectors.paper.util.Logger;

public class PacketConfigurationPacketListener implements PacketListener<PacketConfiguration> {


    @Override
    public void handle(PacketConfiguration packet) {
        Logger.info("Otrzymano pakiet konfiguracji od serwera proxy!");
        PaperSector.getInstance().getSectorManager().loadSectorsData(packet.getSectorsData());
        PaperSector.getInstance().init();

    }

}

