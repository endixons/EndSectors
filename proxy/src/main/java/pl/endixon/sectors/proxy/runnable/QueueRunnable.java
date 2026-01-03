package pl.endixon.sectors.proxy.runnable;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import pl.endixon.sectors.common.sector.SectorData;
import pl.endixon.sectors.common.util.LoggerUtil;
import pl.endixon.sectors.proxy.VelocitySectorPlugin;
import pl.endixon.sectors.proxy.sector.SectorQueue;
import pl.endixon.sectors.proxy.manager.QueueManager;
import pl.endixon.sectors.proxy.manager.SectorManager;
import pl.endixon.sectors.proxy.util.ProxyMessages;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class QueueRunnable implements Runnable {

    private static final String QUEUE_SERVER_NAME = "queue";
    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final Component TITLE_CACHE = MM.deserialize("<gradient:#00d2ff:#3a7bd5><bold>KOLEJKA</bold></gradient>");
    private static final Map<String, Component> SUBTITLE_CACHE = new ConcurrentHashMap<>();

    private final ProxyServer proxyServer = VelocitySectorPlugin.getInstance().getServer();
    private final QueueManager queueManager = VelocitySectorPlugin.getInstance().getQueueManager();
    private final SectorManager sectorManager = VelocitySectorPlugin.getInstance().getSectorManager();

    private static final int MAX_RELEASE_PER_TICK = 20;

    @Override
    public void run() {
        for (final SectorQueue sectorQueue : this.queueManager.getMap().values()) {
            this.processQueue(sectorQueue);
        }
    }

    private void processQueue(final SectorQueue sectorQueue) {
        final List<Player> allPlayers = sectorQueue.getPlayers();
        if (allPlayers.isEmpty()) {
            return;
        }

        this.cleanupQueue(allPlayers);

        if (allPlayers.isEmpty()) {
            return;
        }

        final String sectorName = sectorQueue.getSector();
        final SectorData sectorData = this.sectorManager.getSectorData(sectorName);
        final boolean online = (sectorData != null && sectorData.isOnline());
        final List<Player> sortedQueue = this.sortQueueByPriority(allPlayers);
        this.processPlayersInQueue(sortedQueue, sectorName, sectorData, online);
    }

    private void cleanupQueue(final List<Player> players) {
        players.removeIf(player -> {
            if (player == null || !player.isActive()) {
                return true;
            }

            return player.getCurrentServer().map(server -> {
                final String currentServerName = server.getServerInfo().getName();
                return !currentServerName.equalsIgnoreCase(QUEUE_SERVER_NAME);
            }).orElse(false);
        });
    }

    private void processPlayersInQueue(final List<Player> sortedQueue, final String sectorName, final SectorData sectorData, final boolean online) {
        if (!online || sectorData == null) {
            this.handleOfflineSector(sortedQueue, sectorName);
            return;
        }

        int releasedThisTick = 0;
        final int totalInQueue = sortedQueue.size();

        int currentCount = sectorData.getPlayerCount();
        final int maxSlots = sectorData.getMaxPlayers();

        for (int i = 0; i < totalInQueue; i++) {
            final Player player = sortedQueue.get(i);
            final int positionInQueue = i + 1;
            final boolean isFull = currentCount >= maxSlots;

            if (!isFull && releasedThisTick < MAX_RELEASE_PER_TICK) {
                this.sendPlayerToSector(player, sectorName);
                releasedThisTick++;
                currentCount++;
                sectorData.setPlayerCount(currentCount);

                continue;
            }

            this.dispatchTitle(player, sectorName, true, positionInQueue, totalInQueue, isFull);
        }
    }

    private void sendPlayerToSector(final Player player, final String sectorName) {
        this.proxyServer.getServer(sectorName).ifPresent(server -> {
            player.createConnectionRequest(server).connect().thenAccept(result -> {
                if (result.isSuccessful()) {
                    player.resetTitle();
                }
            });
        });
    }

    private void handleOfflineSector(final List<Player> players, final String sectorName) {
        for (int i = 0; i < players.size(); i++) {
            this.dispatchTitle(players.get(i), sectorName, false, i + 1, players.size(), false);
        }
    }

    private List<Player> sortQueueByPriority(final List<Player> players) {
        final List<Player> admins = new ArrayList<>();
        final List<Player> vips = new ArrayList<>();
        final List<Player> regulars = new ArrayList<>();
        for (final Player p : players) {
            if (p.hasPermission("queue.admin")) {
                admins.add(p);
            } else if (p.hasPermission("queue.vip")) {
                vips.add(p);
            } else {
                regulars.add(p);
            }
        }
        final List<Player> sorted = new ArrayList<>(admins.size() + vips.size() + regulars.size());
        sorted.addAll(admins);
        sorted.addAll(vips);
        sorted.addAll(regulars);
        return sorted;
    }

    private void dispatchTitle(final Player player, final String sector, final boolean online, final int pos, final int total, final boolean full) {
        final String cacheKey = String.format("q_sys_%s_%b_%b_%d_%utils", sector, online, full, pos, total);
        final Component subtitle = SUBTITLE_CACHE.computeIfAbsent(cacheKey, k -> this.buildSubtitle(sector, online, pos, total, full));
        player.showTitle(Title.title(ProxyMessages.QUEUE_TITLE.get(), subtitle));
    }

    private Component buildSubtitle(final String sector, final boolean online, final int pos, final int total, final boolean full) {

        if (!online) {
            return ProxyMessages.QUEUE_OFFLINE.get(
                    "{SECTOR}", sector,
                    "{POS}", String.valueOf(pos),
                    "{TOTAL}", String.valueOf(total)
            );
        }

        if (full) {
            return ProxyMessages.QUEUE_FULL.get(
                    "{SECTOR}", sector,
                    "{POS}", String.valueOf(pos),
                    "{TOTAL}", String.valueOf(total)
            );
        }

        return ProxyMessages.QUEUE_POSITION.get(
                "{POS}", String.valueOf(pos),
                "{TOTAL}", String.valueOf(total)
        );
    }


}