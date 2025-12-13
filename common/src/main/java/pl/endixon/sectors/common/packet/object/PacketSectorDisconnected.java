package pl.endixon.sectors.common.packet.object;

import pl.endixon.sectors.common.packet.Packet;

public class PacketSectorDisconnected implements Packet {

    private final String sector;

    public PacketSectorDisconnected(String sector) {
        this.sector = sector;
    }


    public String getSector() {
        return sector;
    }
}
