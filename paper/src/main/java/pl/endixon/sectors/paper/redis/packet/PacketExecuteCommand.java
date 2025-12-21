/*
 *
 * EndSectors – Non-Commercial License
 * (c) 2025 Endixon
 *
 * Permission is granted to use, copy, and
 * modify this software **only** for personal
 * or educational purposes.
 *
 * Commercial use, redistribution, claiming
 * this work as your own, or copying code
 * without explicit permission is strictly
 * prohibited.
 *
 * Visit https://github.com/Endixon/EndSectors
 * for more info.
 *
 */

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
