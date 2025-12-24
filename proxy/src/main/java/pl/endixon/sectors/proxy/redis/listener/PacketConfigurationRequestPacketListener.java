package pl.endixon.sectors.proxy.redis.listener;

import pl.endixon.sectors.common.packet.PacketListener;
import pl.endixon.sectors.common.packet.object.PacketConfiguration;
import pl.endixon.sectors.common.packet.object.PacketConfigurationRequest;
import pl.endixon.sectors.common.sector.SectorData;
import pl.endixon.sectors.proxy.VelocitySectorPlugin;
import pl.endixon.sectors.proxy.manager.SectorManager;
import pl.endixon.sectors.proxy.util.LoggerUtil;

import java.util.Collection;

public class PacketConfigurationRequestPacketListener implements PacketListener<PacketConfigurationRequest> {

    @Override
    public void handle(PacketConfigurationRequest packet) {
        final String targetSector = packet.getSector();

        if (targetSector == null || targetSector.isEmpty()) {
            return;
        }

        final SectorManager sectorManager = VelocitySectorPlugin.getInstance().getSectorManager();
        if (sectorManager == null) {
            return;
        }

        final Collection<SectorData> sectorsData = sectorManager.getSectorsData();
        if (sectorsData == null || sectorsData.isEmpty()) {
            LoggerUtil.error("[CRITICAL] Configuration request failed for " + targetSector + ". Sector data is missing.");
            return;
        }

        LoggerUtil.info("Received configuration packet request from sector: " + targetSector);

        final PacketConfiguration responsePacket = new PacketConfiguration(sectorsData.toArray(new SectorData[0]));

        VelocitySectorPlugin.getInstance().getRedisService().publish(targetSector, responsePacket);
    }
}