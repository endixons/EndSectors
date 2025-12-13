package pl.endixon.sectors.common.packet.object;

import pl.endixon.sectors.common.packet.Packet;

public class PacketSectorChatBroadcast implements Packet {

    private final String senderName;
    private final String message;

    public PacketSectorChatBroadcast(String senderName, String message) {
        this.senderName = senderName;
        this.message = message;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getMessage() {
        return message;
    }
}
