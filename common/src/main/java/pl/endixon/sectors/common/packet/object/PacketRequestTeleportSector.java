package pl.endixon.sectors.common.packet.object;

import pl.endixon.sectors.common.packet.Packet;

public class PacketRequestTeleportSector implements Packet {

    private final String playerName;
    private final String sector;

    public PacketRequestTeleportSector(String playerName, String sector) {
        this.playerName = playerName;
        this.sector = sector;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getSector() {
        return sector;
    }
}
