package pl.endixon.sectors.proxy.redis.listener;

import pl.endixon.sectors.common.cache.UserFlagCache;
import pl.endixon.sectors.common.packet.PacketListener;
import pl.endixon.sectors.common.packet.object.PacketUserCheck;
import pl.endixon.sectors.proxy.VelocitySectorPlugin;
import pl.endixon.sectors.proxy.queue.Queue;
import pl.endixon.sectors.proxy.queue.QueueManager;
import pl.endixon.sectors.proxy.util.Logger;

public class PacketUserCheckProxyListener implements PacketListener<PacketUserCheck> {



    @Override
    public void handle(PacketUserCheck packet) {
        String username = packet.getUsername();
        Boolean exists = packet.getExists();
        String packetSector = packet.getLastSector();

        if (exists == null || !exists) {
            Logger.info("Gracz " + username + " nie istnieje !");
            return;
        }
        UserFlagCache cache = UserFlagCache.getInstance();
        cache.setExists(username, true);

        String cachedSector = cache.getLastSector(username);
        if (packetSector != null && !packetSector.equals(cachedSector)) {
            cache.setLastSector(username, packetSector);
            cachedSector = packetSector;
        }
        addPlayerToQueue(username, cachedSector);
    }

    private void addPlayerToQueue(String username, String sector) {
        QueueManager queueManager = VelocitySectorPlugin.getInstance().getQueueManager();
        Queue queue = queueManager.getMap().computeIfAbsent(sector, Queue::new);
        VelocitySectorPlugin.getInstance().getServer().getPlayer(username).ifPresentOrElse(
                queue::addPlayer,
                () -> Logger.info("Gracz " + username + " nie znaleziony na serwerze!")
        );
    }
}
