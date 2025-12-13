package pl.endixon.sectors.common.packet.object;

import pl.endixon.sectors.common.packet.Packet;

public class PacketBroadcastMessage implements Packet {

    private final String message;

    public PacketBroadcastMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
