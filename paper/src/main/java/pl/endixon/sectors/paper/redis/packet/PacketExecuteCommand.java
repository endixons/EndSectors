package pl.endixon.sectors.paper.redis.packet;

import pl.endixon.sectors.common.packet.Packet;

public class PacketExecuteCommand implements Packet {

    private final String command;


    public PacketExecuteCommand(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }
}
