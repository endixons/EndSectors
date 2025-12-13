package pl.endixon.sectors.paper.redis.packet;

import pl.endixon.sectors.common.packet.Packet;

public class PacketPermissionBroadcastMessage implements Packet {

    private final String permission;
    private final String message;


    public PacketPermissionBroadcastMessage(String permission, String message) {
        this.permission = permission;
        this.message = message;
    }

    public String getPermission() {
        return permission;
    }

    public String getMessage() {
        return message;
    }
}
