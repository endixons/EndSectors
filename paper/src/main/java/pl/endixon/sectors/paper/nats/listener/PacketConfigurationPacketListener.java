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

package pl.endixon.sectors.paper.nats.listener;

import org.bukkit.Bukkit;
import pl.endixon.sectors.common.Common;
import pl.endixon.sectors.common.packet.PacketChannel;
import pl.endixon.sectors.common.packet.PacketListener;
import pl.endixon.sectors.common.packet.object.PacketConfiguration;
import pl.endixon.sectors.common.packet.object.PacketSectorConnected;
import pl.endixon.sectors.paper.PaperSector;
import pl.endixon.sectors.paper.sector.Sector;
import pl.endixon.sectors.paper.task.SendSectorInfoTask;
import pl.endixon.sectors.paper.util.LoggerUtil;

public class PacketConfigurationPacketListener implements PacketListener<PacketConfiguration> {

    private boolean inited = false;

    @Override
    public void handle(PacketConfiguration packet) {

        PaperSector.getInstance().getSectorManager().loadSectorsData(packet.getSectorsData());
        Sector currentSector = PaperSector.getInstance().getSectorManager().getCurrentSector();
        String currentSectorName = PaperSector.getInstance().getSectorManager().getCurrentSectorName();

        if (currentSector == null) {
            LoggerUtil.info("Current sector is NULL! Make sure that the sector name '" + currentSectorName + "' matches the one defined in the proxy plugin configuration and in velocity.toml.");
            return;
        }

        LoggerUtil.info("Loaded " + PaperSector.getInstance().getSectorManager().getSectors().size() + " sectors!");
        LoggerUtil.info("Current sector: " + currentSectorName);

        if (!inited) {
            inited = true;
            Bukkit.getScheduler().runTaskTimerAsynchronously(PaperSector.getInstance(), new SendSectorInfoTask(PaperSector.getInstance()), 0L, 20L * 5);
        }
        Common.getInstance().getNatsManager().publish(PacketChannel.PACKET_SECTOR_CONNECTED.getSubject(), new PacketSectorConnected(currentSectorName));
        LoggerUtil.info("Sectors data synchronized and system initialized.");
    }
}