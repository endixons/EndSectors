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

package pl.endixon.sectors.proxy.nats.listener;

import pl.endixon.sectors.common.packet.PacketListener;
import pl.endixon.sectors.common.packet.object.PacketSectorDisconnected;
import pl.endixon.sectors.proxy.VelocitySectorPlugin;

public class PacketSectorDisconnectedPacketListener implements PacketListener<PacketSectorDisconnected> {

    @Override
    public void handle(PacketSectorDisconnected packet) {
        VelocitySectorPlugin.getInstance().getSectorManager().getSectorData(packet.getSector()).setOnline(false);
        VelocitySectorPlugin.getInstance().getSectorManager().getSectorData(packet.getSector()).setPlayerCount(0);
        VelocitySectorPlugin.getInstance().getSectorManager().getSectorData(packet.getSector()).setMaxPlayers(0);
        VelocitySectorPlugin.getInstance().getSectorManager().getSectorData(packet.getSector()).setTps(0.00);


    }
}
