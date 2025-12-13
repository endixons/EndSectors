package pl.endixon.sectors.common.packet.object;

import pl.endixon.sectors.common.packet.Packet;

public class PacketConfigurationRequest implements Packet {

    private final String sector;

    public PacketConfigurationRequest(String sector) {
        this.sector = sector;
    }

    // Getter
    public String getSector() {
        return sector;
    }
}
