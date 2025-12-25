/*
 *
 *  EndSectors  Non-Commercial License
 *  (c) 2025 Endixon
 *
 *  Permission is granted to use, copy, and
 *  modify this software **only** for personal
 *  or educational purposes.
 *
 *   Commercial use, redistribution, claiming
 *  this work as your own, or copying code
 *  without explicit permission is strictly
 *  prohibited.
 *
 *  Visit https://github.com/Endixon/EndSectors
 *  for more info.
 *
 */

package pl.endixon.sectors.proxy.nats.listener;

import pl.endixon.sectors.common.cache.UserFlagCache;
import pl.endixon.sectors.common.packet.PacketListener;
import pl.endixon.sectors.common.packet.object.PacketUserCheck;
import pl.endixon.sectors.proxy.VelocitySectorPlugin;
import pl.endixon.sectors.proxy.sector.SectorQueue;
import pl.endixon.sectors.proxy.manager.QueueManager;
import pl.endixon.sectors.proxy.manager.SectorManager;
import pl.endixon.sectors.proxy.user.profile.UserProfileCache;
import pl.endixon.sectors.proxy.util.LoggerUtil;
import pl.endixon.sectors.common.sector.SectorData;

public class PacketUserCheckProxyListener implements PacketListener<PacketUserCheck> {

    private static final String UNKNOWN_VAL = "unknown";

    private final QueueManager queueManager = VelocitySectorPlugin.getInstance().getQueueManager();
    private final SectorManager sectorManager = VelocitySectorPlugin.getInstance().getSectorManager();

    @Override
    public void handle(PacketUserCheck packet) {
        if (packet.getUsername() == null) {
            return;
        }

        final String username = packet.getUsername().toLowerCase();
        final UserProfileCache userProfileCache = VelocitySectorPlugin.getInstance().getUserProfileCache();
        final UserFlagCache cache = UserFlagCache.getInstance();

        if (packet.getExists() == null || !packet.getExists()) {
            LoggerUtil.error("[PacketUserCheck] Player " + username + " does not exist in database.");
            return;
        }

        cache.setExists(username, true);

        String resolvedSector = this.resolveSector(username, cache, userProfileCache);

        if (resolvedSector == null) {
            resolvedSector = this.sectorManager.getRandomNonQueueSector()
                    .map(SectorData::getName)
                    .orElse(null);

            if (resolvedSector != null) {
                this.updateStorages(username, resolvedSector, cache, userProfileCache);
            }
        }

        if (resolvedSector == null) {
            LoggerUtil.error("[CRITICAL] No available sectors for player: " + username);
            return;
        }

        final String finalTarget = resolvedSector;
        final SectorQueue sectorQueue = this.queueManager.getMap().computeIfAbsent(finalTarget, SectorQueue::new);

        VelocitySectorPlugin.getInstance().getServer()
                .getPlayer(username)
                .ifPresent(sectorQueue::addPlayer);
    }

    private String resolveSector(String username, UserFlagCache cache, UserProfileCache redis) {
        final String cached = cache.getLastSector(username);
        if (this.isValid(cached)) {
            return cached;
        }

        return redis.getSectorName(username)
                .map(sectorFromRedis -> {
                    cache.setLastSector(username, sectorFromRedis);
                    return sectorFromRedis;
                })
                .orElse(null);
    }

    private boolean isValid(String sector) {
        return sector != null && !sector.isBlank() && !sector.trim().equalsIgnoreCase(UNKNOWN_VAL);
    }

    private void updateStorages(String username, String sector, UserFlagCache cache, UserProfileCache redis) {
        cache.setLastSector(username, sector);
        redis.setSectorName(username, sector);
    }
}