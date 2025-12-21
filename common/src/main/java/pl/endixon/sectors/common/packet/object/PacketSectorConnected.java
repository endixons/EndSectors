package pl.endixon.sectors.common.packet.object;

import pl.endixon.sectors.common.packet.Packet;

public class PacketSectorConnected implements Packet {

    private final String sector;

    public PacketSectorConnected(String sector) {
        this.sector = sector;
    }

    public String getSector() {
        return sector;
    }
}
