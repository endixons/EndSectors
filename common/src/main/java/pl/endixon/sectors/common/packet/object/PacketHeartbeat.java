package pl.endixon.sectors.common.packet.object;

import lombok.Getter;
import lombok.Setter;
import pl.endixon.sectors.common.packet.Packet;

@Getter
@Setter
public class PacketHeartbeat implements Packet {
    private final String message;
    private final boolean status;

    public PacketHeartbeat(String message, boolean status) {
        this.message = message;
        this.status = status;
    }
}