package pl.endixon.sectors.common.packet.object;

import pl.endixon.sectors.common.packet.Packet;

public class PacketSendMessageToPlayer implements Packet {

    private final String playerName;
    private final String message;

    public PacketSendMessageToPlayer(String playerName, String message) {
        this.playerName = playerName;
        this.message = message;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getMessage() {
        return message;
    }
}
