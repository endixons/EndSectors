    package pl.endixon.sectors.tools.nats.listener;

    import pl.endixon.sectors.common.packet.PacketListener;
    import pl.endixon.sectors.tools.market.type.MarketOfferStatus; // <--- Import Enuma
    import pl.endixon.sectors.tools.nats.packet.PacketMarketUpdate;
    import pl.endixon.sectors.tools.user.profile.player.PlayerMarketProfile;
    import pl.endixon.sectors.tools.user.profile.cache.ProfileMarketCache;

    public class PacketMarketUpdateListener implements PacketListener<PacketMarketUpdate> {

        @Override
        public void handle(PacketMarketUpdate packet) {
            if (packet.getAction().equals("REMOVE")) {
                ProfileMarketCache.remove(packet.getId());
                return;
            }
            PlayerMarketProfile offer = new PlayerMarketProfile(
                    packet.getId(),
                    packet.getSellerUuid(),
                    packet.getSellerName(),
                    packet.getItemData(),
                    packet.getItemName(),
                    packet.getCategory(),
                    packet.getPrice(),
                    packet.getCreatedAt(),
                    MarketOfferStatus.ACTIVE
            );

            ProfileMarketCache.put(offer);
        }
    }