package pl.endixon.sectors.proxy.listener;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import org.bson.Document;
import pl.endixon.sectors.common.redis.MongoExecutor;
import pl.endixon.sectors.common.redis.MongoManager;
import pl.endixon.sectors.proxy.VelocitySectorPlugin;
import pl.endixon.sectors.proxy.manager.TeleportationManager;
import pl.endixon.sectors.proxy.queue.Queue;
import pl.endixon.sectors.proxy.queue.QueueManager;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class LastSectorConnectListener {

    private final VelocitySectorPlugin plugin;
    private final MongoManager mongo;
    private final TeleportationManager teleportManager;
    private final QueueManager queueService;

    @Inject
    public LastSectorConnectListener(VelocitySectorPlugin plugin, TeleportationManager teleportManager) {
        this.plugin = plugin;
        this.mongo = plugin.getMongoManager();
        this.teleportManager = teleportManager;
        this.queueService = plugin.getQueueManager();
    }

    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        Player player = event.getPlayer();
        String connectedServer = event.getServer().getServerInfo().getName();

        if (!connectedServer.equalsIgnoreCase("queue")) return;

        queueService.findQueueByPlayer(player).ifPresent(queue -> queue.removePlayer(player));
        fetchPlayerDocumentWithRetry(player, 0);
    }

    private void fetchPlayerDocumentWithRetry(Player player, int attempt) {
        CompletableFuture.supplyAsync(() ->
                mongo.getUsersCollection().find(new Document("Name", player.getUsername())).first(),
                MongoExecutor.EXECUTOR
        ).thenAccept(doc -> {
            if (doc == null) {
                long delay = Math.min(100 * (attempt + 1), 1000); 
                plugin.getProxy().getScheduler().buildTask(plugin,
                        () -> fetchPlayerDocumentWithRetry(player, attempt + 1))
                        .delay(delay, TimeUnit.MILLISECONDS)
                        .schedule();
                return;
            }

            String lastSector = doc.getString("sectorName");
            if (lastSector == null) return;

            Queue queue = queueService.getMap().computeIfAbsent(lastSector, Queue::new);
            queue.addPlayer(player);
        });
    }
}
