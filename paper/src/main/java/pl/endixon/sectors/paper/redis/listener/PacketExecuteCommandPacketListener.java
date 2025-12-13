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
import org.bukkit.command.ConsoleCommandSender;
import pl.endixon.sectors.common.packet.PacketListener;
import pl.endixon.sectors.paper.redis.packet.PacketExecuteCommand;
import pl.endixon.sectors.paper.PaperSector;

public class PacketExecuteCommandPacketListener implements PacketListener<PacketExecuteCommand> {



    @Override
    public void handle(PacketExecuteCommand packet) {
        ConsoleCommandSender console = PaperSector.getInstance().getServer().getConsoleSender();
        Bukkit.getScheduler().runTask(PaperSector.getInstance(), () -> {
            PaperSector.getInstance().getServer().dispatchCommand(console, packet.getCommand());
        });
    }
}

