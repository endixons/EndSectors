package pl.endixon.sectors.tools.market;

import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import pl.endixon.sectors.common.Common;
import pl.endixon.sectors.common.packet.PacketChannel;
import pl.endixon.sectors.tools.market.Repository.MarketRepository;
import pl.endixon.sectors.tools.market.type.MarketOfferStatus;
import pl.endixon.sectors.tools.market.type.PurchaseResult;
import pl.endixon.sectors.tools.nats.packet.PacketMarketExpirationNotify;
import pl.endixon.sectors.tools.nats.packet.PacketMarketJanitor;
import pl.endixon.sectors.tools.nats.packet.PacketMarketNotify;
import pl.endixon.sectors.tools.nats.packet.PacketMarketUpdate;
import pl.endixon.sectors.tools.user.repository.PlayerRepository;
import pl.endixon.sectors.tools.user.profile.player.PlayerMarketProfile;
import pl.endixon.sectors.tools.user.profile.player.PlayerProfile;
import pl.endixon.sectors.tools.utils.LoggerUtil;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class MarketService {

    private final MarketRepository marketRepository;
    private static final long EXPIRATION_TIME_MS = TimeUnit.HOURS.toMillis(24);

    public void listOffer(PlayerProfile seller, String itemData, String itemName, String category, double price) {
        UUID offerId = UUID.randomUUID();
        long now = System.currentTimeMillis();

        PlayerMarketProfile offer = new PlayerMarketProfile(
                offerId,
                seller.getUuid(),
                seller.getName(),
                itemData,
                itemName,
                category,
                price,
                now,
                MarketOfferStatus.ACTIVE
        );

        marketRepository.save(offer);

        PacketMarketUpdate packet = new PacketMarketUpdate(
                offerId, "ADD", seller.getUuid(), seller.getName(), itemData, itemName, category, price, now
        );
        Common.getInstance().getNatsManager().publish(PacketChannel.MARKET_UPDATE.getSubject(), packet);
    }

    public boolean cancelOffer(UUID offerId, UUID sellerUuid) {
        return marketRepository.find(offerId)
                .filter(offer -> offer.getSellerUuid().equals(sellerUuid))
                .filter(offer -> offer.getStatus() == MarketOfferStatus.ACTIVE)
                .map(offer -> {
                    if (marketRepository.delete(offerId)) {
                        Common.getInstance().getNatsManager().publish(
                                PacketChannel.MARKET_UPDATE.getSubject(),
                                PacketMarketUpdate.remove(offerId)
                        );
                        return true;
                    }
                    return false;
                })
                .orElse(false);
    }



    public boolean claimStorageItem(UUID offerId, UUID ownerUuid) {
        return marketRepository.find(offerId)
                .filter(offer -> offer.getSellerUuid().equals(ownerUuid))
                .filter(offer ->
                        offer.getStatus() == MarketOfferStatus.EXPIRED ||
                                offer.getStatus() == MarketOfferStatus.CLAIMABLE
                )
                .map(offer -> marketRepository.delete(offerId))
                .orElse(false);
    }


    public PurchaseResult processPurchase(UUID offerId, PlayerProfile buyer, PlayerRepository playerRepository) {
        return marketRepository.find(offerId).map(offer -> {

            if (offer.getStatus() != MarketOfferStatus.ACTIVE) {
                return PurchaseResult.NOT_FOUND;
            }

            if (offer.getSellerUuid().equals(buyer.getUuid())) {
                return PurchaseResult.SELF_PURCHASE;
            }
            if (buyer.getBalance() < offer.getPrice()) {
                return PurchaseResult.NOT_ENOUGH_MONEY;
            }

            if (!marketRepository.delete(offerId)) {
                return PurchaseResult.ALREADY_SOLD;
            }

            buyer.setBalance(buyer.getBalance() - offer.getPrice());
            playerRepository.save(buyer);

            playerRepository.find(offer.getSellerUuid()).ifPresent(seller -> {
                seller.setBalance(seller.getBalance() + offer.getPrice());
                playerRepository.save(seller);
            });

            PacketMarketNotify notify = new PacketMarketNotify(offer.getSellerUuid(), buyer.getName(), offer.getItemName(), offer.getPrice());
            Common.getInstance().getNatsManager().publish(PacketChannel.MARKET_NOTIFY.getSubject(), notify);

            return PurchaseResult.SUCCESS;
        }).orElse(PurchaseResult.NOT_FOUND);
    }


    public void runExpirationTask() {
        long threshold = System.currentTimeMillis() - EXPIRATION_TIME_MS;
        Map<UUID, Integer> affectedPlayers = marketRepository.expireAndGetOwners(threshold);

        if (!affectedPlayers.isEmpty()) {
            LoggerUtil.info("Market Janitor: Expired items for " + affectedPlayers.size() + " players. Syncing...");

            PacketMarketJanitor janitorPacket = new PacketMarketJanitor(threshold);
            Common.getInstance().getNatsManager().publish(PacketChannel.MARKET_JANITOR.getSubject(), janitorPacket);

            affectedPlayers.forEach((uuid, count) -> {
                PacketMarketExpirationNotify notifyPacket = new PacketMarketExpirationNotify(uuid, count);
                Common.getInstance().getNatsManager().publish(PacketChannel.MARKET_EXPIRATION_NOTIFY.getSubject(), notifyPacket);
            });
        }
    }

    public int getMarketLimit(@NotNull Player player) {
        if (player.hasPermission("endsectors.market.limit.unlimited")) return 1000;
        if (player.hasPermission("endsectors.market.limit.vip")) return 15;
        if (player.hasPermission("endsectors.market.limit.player")) return 5;
        return 3;
    }
}