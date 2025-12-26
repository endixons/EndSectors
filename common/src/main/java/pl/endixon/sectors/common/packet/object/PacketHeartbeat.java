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