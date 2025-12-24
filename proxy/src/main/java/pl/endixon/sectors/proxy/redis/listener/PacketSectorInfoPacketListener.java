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
import pl.endixon.sectors.common.packet.object.PacketSectorInfo;
import pl.endixon.sectors.common.sector.SectorData;
import pl.endixon.sectors.proxy.VelocitySectorPlugin;
import pl.endixon.sectors.proxy.util.LoggerUtil;


public class PacketSectorInfoPacketListener implements PacketListener<PacketSectorInfo> {

    @Override
    public void handle(PacketSectorInfo packet) {

        final String targetSector = packet.getSector();

        SectorData sector = VelocitySectorPlugin.getInstance()
                .getSectorManager()
                .getSectorData(targetSector);

        if (sector == null) {
            return;
        }

        sector.setTps(packet.getTps());
        sector.setPlayerCount(packet.getPlayerCount());
        sector.setMaxPlayers(packet.getMaxPlayers());
        sector.setOnline(packet.isStatus());



    }
}
