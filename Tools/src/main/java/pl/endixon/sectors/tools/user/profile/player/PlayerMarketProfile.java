    package pl.endixon.sectors.tools.user.profile.player;

    import lombok.*;
    import org.bson.codecs.pojo.annotations.BsonId;
    import pl.endixon.sectors.tools.market.type.MarketOfferStatus; // <--- Pamiętaj o imporcie!

    import java.util.UUID;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public class PlayerMarketProfile {
        @BsonId
        private UUID id;
        private UUID sellerUuid;
        private String sellerName;
        private String itemData;
        private String itemName;
        private String category;
        private double price;
        private long createdAt;
        private MarketOfferStatus status = MarketOfferStatus.ACTIVE;
    }