package pl.endixon.sectors.tools.market.repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections; // <--- Ważny import do optymalizacji
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.Updates;
import lombok.RequiredArgsConstructor;
import org.bson.conversions.Bson;
import pl.endixon.sectors.tools.market.type.MarketOfferStatus;
import pl.endixon.sectors.tools.user.profile.PlayerMarketProfile;
import pl.endixon.sectors.tools.user.profile.ProfileMarketCache;
import java.util.*;

import static com.mongodb.client.model.Filters.eq;

@RequiredArgsConstructor
public class MarketRepository {

    private final MongoCollection<PlayerMarketProfile> collection;

    public void warmup() {
        List<PlayerMarketProfile> activeOffers = collection.find(eq("status", "ACTIVE")).into(new ArrayList<>());
        activeOffers.forEach(ProfileMarketCache::put);
    }

    public Optional<PlayerMarketProfile> find(UUID id) {
        PlayerMarketProfile cached = ProfileMarketCache.get(id);
        if (cached != null) return Optional.of(cached);
        return Optional.ofNullable(collection.find(eq("_id", id)).first());
    }

    public void save(PlayerMarketProfile offer) {
        collection.replaceOne(eq("_id", offer.getId()), offer, new ReplaceOptions().upsert(true));

        if (offer.getStatus() == MarketOfferStatus.ACTIVE) {
            ProfileMarketCache.put(offer);
        } else {
            ProfileMarketCache.remove(offer.getId());
        }
    }

    public boolean delete(UUID id) {
        if (collection.deleteOne(eq("_id", id)).getDeletedCount() > 0) {
            ProfileMarketCache.remove(id);
            return true;
        }
        return false;
    }

    public List<PlayerMarketProfile> findByCategory(String category) {
        return ProfileMarketCache.getValues().stream()
                .filter(offer -> offer.getStatus() == MarketOfferStatus.ACTIVE)
                .filter(offer -> offer.getCategory().equalsIgnoreCase(category))
                .toList();
    }

    public List<PlayerMarketProfile> findBySeller(UUID sellerUuid) {
        return ProfileMarketCache.getValues().stream()
                .filter(offer -> offer.getStatus() == MarketOfferStatus.ACTIVE)
                .filter(offer -> offer.getSellerUuid().equals(sellerUuid))
                .toList();
    }



    public List<PlayerMarketProfile> findExpiredBySeller(UUID sellerUuid) {
        Bson filter = Filters.and(
                eq("sellerUuid", sellerUuid),
                eq("status", "EXPIRED")
        );
        return collection.find(filter).into(new ArrayList<>());
    }


    public Map<UUID, Integer> expireAndGetOwners(long expirationThreshold) {
        Bson filter = Filters.and(
                eq("status", "ACTIVE"),
                Filters.lt("createdAt", expirationThreshold)
        );

        List<PlayerMarketProfile> expiring = collection.find(filter)
                .projection(Projections.include("sellerUuid"))
                .into(new ArrayList<>());

        if (expiring.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<UUID, Integer> affectedPlayers = new HashMap<>();
        for (PlayerMarketProfile p : expiring) {
            if (p.getSellerUuid() != null) {
                affectedPlayers.merge(p.getSellerUuid(), 1, Integer::sum);
            }
        }

        Bson update = Updates.set("status", "EXPIRED");
        long modifiedCount = collection.updateMany(filter, update).getModifiedCount();

        if (modifiedCount > 0) {
            this.cleanupLocalCache(expirationThreshold);
        }

        return affectedPlayers;
    }

    public void cleanupLocalCache(long expirationThreshold) {
        List<UUID> toRemove = ProfileMarketCache.getValues().stream()
                .filter(o -> o.getCreatedAt() < expirationThreshold)
                .map(PlayerMarketProfile::getId)
                .toList();

        toRemove.forEach(ProfileMarketCache::remove);
    }
}