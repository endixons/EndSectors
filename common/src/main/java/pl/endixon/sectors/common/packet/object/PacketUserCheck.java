package pl.endixon.sectors.common.packet.object;

import pl.endixon.sectors.common.packet.Packet;

public class PacketUserCheck implements Packet {

    private final String username;
    private final Boolean exists;
    private final String lastSector;

    // Konstruktor tylko z username
    public PacketUserCheck(String username) {
        this.username = username;
        this.exists = null;
        this.lastSector = null;
    }

    // Konstruktor pełny
    public PacketUserCheck(String username, Boolean exists, String lastSector) {
        this.username = username;
        this.exists = exists;
        this.lastSector = lastSector;
    }

    public String getUsername() {
        return username;
    }

    public Boolean getExists() {
        return exists;
    }

    public String getLastSector() {
        return lastSector;
    }
}
