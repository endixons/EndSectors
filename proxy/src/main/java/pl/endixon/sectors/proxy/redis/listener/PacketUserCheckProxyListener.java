package pl.endixon.sectors.proxy.redis.listener;

import pl.endixon.sectors.common.cache.UserFlagCache;
import pl.endixon.sectors.common.packet.PacketListener;
import pl.endixon.sectors.common.packet.object.PacketUserCheck;
import pl.endixon.sectors.proxy.VelocitySectorPlugin;
import pl.endixon.sectors.proxy.queue.Queue;
import pl.endixon.sectors.proxy.queue.QueueManager;
import pl.endixon.sectors.proxy.manager.SectorManager;
import pl.endixon.sectors.proxy.user.RedisUserService;
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
        final RedisUserService redisUserService = VelocitySectorPlugin.getInstance().getRedisUserService();
        final UserFlagCache cache = UserFlagCache.getInstance();

        if (packet.getExists() == null || !packet.getExists()) {
            LoggerUtil.error("[PacketUserCheck] Player " + username + " does not exist in database.");
            return;
        }

        cache.setExists(username, true);

        String resolvedSector = this.resolveSector(username, cache, redisUserService);

        if (resolvedSector == null) {
            resolvedSector = this.sectorManager.getRandomNonQueueSector()
                    .map(SectorData::getName)
                    .orElse(null);

            if (resolvedSector != null) {
                this.updateStorages(username, resolvedSector, cache, redisUserService);
            }
        }

        if (resolvedSector == null) {
            LoggerUtil.error("[CRITICAL] No available sectors for player: " + username);
            return;
        }

        final String finalTarget = resolvedSector;
        final Queue queue = this.queueManager.getMap().computeIfAbsent(finalTarget, Queue::new);

        VelocitySectorPlugin.getInstance().getServer()
                .getPlayer(username)
                .ifPresent(queue::addPlayer);
    }

    private String resolveSector(String username, UserFlagCache cache, RedisUserService redis) {
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

    private void updateStorages(String username, String sector, UserFlagCache cache, RedisUserService redis) {
        cache.setLastSector(username, sector);
        redis.setSectorName(username, sector);
    }
}