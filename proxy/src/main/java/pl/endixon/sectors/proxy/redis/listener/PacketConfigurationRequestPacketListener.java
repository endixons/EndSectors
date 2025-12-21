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
import pl.endixon.sectors.common.packet.object.PacketConfiguration;
import pl.endixon.sectors.common.packet.object.PacketConfigurationRequest;
import pl.endixon.sectors.common.sector.SectorData;
import pl.endixon.sectors.proxy.VelocitySectorPlugin;
import pl.endixon.sectors.proxy.manager.SectorManager;
import pl.endixon.sectors.proxy.util.Logger;

public class PacketConfigurationRequestPacketListener implements PacketListener<PacketConfigurationRequest> {

    @Override
    public void handle(PacketConfigurationRequest packet) {
        String sector = packet.getSector();

        if (sector == null || sector.isEmpty()) {
            Logger.info("Otrzymano zapytanie o pakiet konfiguracji z pustym sektorem, ignoruję pakiet.");
            return;
        }

        SectorManager sectorManager = VelocitySectorPlugin.getInstance().getSectorManager();
        if (sectorManager == null || sectorManager.getSectorsData() == null) {
            Logger.info("SectorManager lub lista sektorów jest null, nie można wysłać pakietu konfiguracji.");
            return;
        }

        Logger.info("Otrzymano zapytanie o pakiet konfiguracji od sektora " + sector);

        PacketConfiguration packetConfiguration = new PacketConfiguration(sectorManager.getSectorsData().toArray(new SectorData[0]));

        VelocitySectorPlugin.getInstance().getRedisService().publish(sector, packetConfiguration);
    }
}
