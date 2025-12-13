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

import com.velocitypowered.api.proxy.Player;

import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import pl.endixon.sectors.common.packet.PacketListener;
import pl.endixon.sectors.common.packet.object.PacketBroadcastMessage;
import pl.endixon.sectors.proxy.VelocitySectorPlugin;

public class PacketBroadcastMessagePacketListener implements PacketListener<PacketBroadcastMessage> {




    @Override
    public void handle(PacketBroadcastMessage packet) {
        ProxyServer server = VelocitySectorPlugin.getInstance().getServerInstance();
        if (server == null) return;

        Component message = Component.text(packet.getMessage());

        for (Player player : server.getAllPlayers()) {
            player.sendMessage(message);
        }
    }
}

