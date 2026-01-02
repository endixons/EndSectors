package pl.endixon.sectors.tools.nats.packet;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.endixon.sectors.common.packet.Packet;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PacketMarketUpdate implements Packet {

    private UUID id;
    private String action;
    private UUID sellerUuid;
    private String sellerName;
    private String itemData;
    private String itemName;
    private String category;
    private double price;
    private long createdAt;

    public static PacketMarketUpdate remove(UUID id) {
        return new PacketMarketUpdate(id, "REMOVE", null, null, null, null, null, 0.0, 0L);
    }
}