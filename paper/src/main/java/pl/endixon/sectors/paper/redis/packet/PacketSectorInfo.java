package pl.endixon.sectors.paper.redis.packet;

import pl.endixon.sectors.common.packet.Packet;

public class PacketSectorInfo implements Packet {

    private final String sector;
    private final float tps;
    private final int playerCount;
    private final int maxPlayers;

    public PacketSectorInfo(String sector, float tps, int playerCount, int maxPlayers) {
        this.sector = sector;
        this.tps = tps;
        this.playerCount = playerCount;
        this.maxPlayers = maxPlayers;
    }

    public String getSector() {
        return sector;
    }

    public float getTPS() {
        return tps;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }
}
