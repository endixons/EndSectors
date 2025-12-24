package pl.endixon.sectors.common.packet.object;

import lombok.Getter;
import pl.endixon.sectors.common.packet.Packet;

@Getter
public class PacketSectorInfo implements Packet {

    private final String sector;
    private final boolean status;
    private final float tps;
    private final int playerCount;
    private final int maxPlayers;

    public PacketSectorInfo(String sector, boolean status, float tps, int playerCount, int maxPlayers) {
        this.sector = sector;
        this.status = status;
        this.tps = tps;
        this.playerCount = playerCount;
        this.maxPlayers = maxPlayers;
    }
}
