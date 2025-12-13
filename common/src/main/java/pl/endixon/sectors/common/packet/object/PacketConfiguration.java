package pl.endixon.sectors.common.packet.object;

import pl.endixon.sectors.common.packet.Packet;
import pl.endixon.sectors.common.sector.SectorData;

public class PacketConfiguration implements Packet {

    private final SectorData[] sectorsData;

    public PacketConfiguration(SectorData[] sectorsData) {
        this.sectorsData = sectorsData;
    }

    public SectorData[] getSectorsData() {
        return sectorsData;
    }
}
