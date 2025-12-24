/*
 *
 * EndSectors  Non-Commercial License
 * (c) 2025 Endixon
 *
 * Permission is granted to use, copy, and
 * modify this software **only** for personal
 * or educational purposes.
 *
 * Commercial use, redistribution, claiming
 * this work as your own, or copying code
 * without explicit permission is strictly
 * prohibited.
 *
 * Visit https://github.com/Endixon/EndSectors
 * for more info.
 *
 */

package pl.endixon.sectors.proxy.queue.runnable;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import pl.endixon.sectors.common.sector.SectorData;
import pl.endixon.sectors.common.util.Logger;
import pl.endixon.sectors.proxy.VelocitySectorPlugin;
import pl.endixon.sectors.proxy.queue.Queue;
import pl.endixon.sectors.proxy.queue.QueueManager;
import pl.endixon.sectors.proxy.manager.SectorManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Enterprise-grade Queue Runnable.
 * Optimized for high-traffic and low-latency sector transitions.
 */
public class QueueRunnable implements Runnable {

    private static final String QUEUE_SERVER_NAME = "queue";
    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final Component TITLE_CACHE = MM.deserialize("<gradient:#00d2ff:#3a7bd5><bold>KOLEJKA</bold></gradient>");
    private static final Map<String, Component> SUBTITLE_CACHE = new ConcurrentHashMap<>();

    private final ProxyServer proxyServer = VelocitySectorPlugin.getInstance().getServer();
    private final QueueManager queueManager = VelocitySectorPlugin.getInstance().getQueueManager();
    private final SectorManager sectorManager = VelocitySectorPlugin.getInstance().getSectorManager();

    private static final int MAX_RELEASE_PER_TICK = 5;

    @Override
    public void run() {
        for (final Queue queue : this.queueManager.getMap().values()) {
            this.processQueue(queue);
        }
    }

    private void processQueue(final Queue queue) {
        final List<Player> allPlayers = queue.getPlayers();
        if (allPlayers.isEmpty()) {
            return;
        }

        final String sectorName = queue.getSector();
        final SectorData sectorData = this.sectorManager.getSectorData(sectorName);

        this.cleanupQueue(allPlayers);
        if (allPlayers.isEmpty()) {
            return;
        }

        final boolean online = sectorData != null && sectorData.isOnline();
        final boolean full = this.isSectorFull(sectorData);
        this.logQueueStatus(sectorName, sectorData, online, full, allPlayers.size());
        final List<Player> sortedQueue = this.sortQueueByPriority(allPlayers);
        this.processPlayersInQueue(sortedQueue, allPlayers, sectorName, online, full);
    }

    private void cleanupQueue(final List<Player> players) {
        players.removeIf(player -> {
            if (player == null || !player.isActive()) {
                return true;
            }
            return player.getCurrentServer()
                    .map(server -> !server.getServerInfo().getName().equalsIgnoreCase(QUEUE_SERVER_NAME))
                    .orElse(true);
        });
    }

    private void processPlayersInQueue(final List<Player> sortedQueue, final List<Player> originalList, final String sectorName, final boolean online, final boolean full) {
        int released = 0;
        final int total = sortedQueue.size();
        final List<Player> toRemove = new ArrayList<>();

        for (int i = 0; i < total; i++) {
            final Player player = sortedQueue.get(i);
            final int position = i + 1;

            if (online && !full && released < MAX_RELEASE_PER_TICK) {
                this.sendPlayerToSector(player, sectorName, toRemove);
                released++;
                continue;
            }

            this.dispatchTitle(player, sectorName, online, position, total, full);
        }

        if (!toRemove.isEmpty()) {
            originalList.removeAll(toRemove);
        }
    }

    private void sendPlayerToSector(final Player player, final String sectorName, final List<Player> toRemove) {
        this.proxyServer.getServer(sectorName).ifPresent(server -> {
            Logger.info(String.format("[Queue-Release] Transferring player %s to sector %s", player.getUsername(), sectorName));
            player.createConnectionRequest(server).fireAndForget();
            toRemove.add(player);
        });
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

        final List<Player> sorted = new ArrayList<>(admins);
        sorted.addAll(vips);
        sorted.addAll(regulars);
        return sorted;
    }

    private void dispatchTitle(final Player player, final String sector, final boolean online, final int pos, final int total, final boolean full) {
        final String cacheKey = String.format("q_%s_%b_%b_%d_%d", sector, online, full, pos, total);
        final Component subtitle = SUBTITLE_CACHE.computeIfAbsent(cacheKey, k -> this.buildSubtitle(sector, online, pos, total, full));
        player.showTitle(Title.title(TITLE_CACHE, subtitle));
    }

    private Component buildSubtitle(final String sector, final boolean online, final int pos, final int total, final boolean full) {
        if (!online) {
            return MM.deserialize("<gradient:#ff4b2b:#ff416c>Sektor <white>" + sector + "</white> jest obecnie <bold>OFFLINE</bold></gradient>");
        }

        if (full) {
            return MM.deserialize("<gradient:#f8ff00:#f8ff00>Sektor <white>" + sector + "</white> jest <bold>PELNY</bold></gradient> <gray>(" + pos + "/" + total + ")</gray>");
        }

        return MM.deserialize("<gray>Twoja pozycja: <gradient:#00d2ff:#3a7bd5><bold>" + pos + "</bold></gradient><dark_gray>/</dark_gray><gradient:#3a7bd5:#00d2ff>" + total + "</gradient>");
    }

    private void logQueueStatus(final String sector, final SectorData data, final boolean online, final boolean full, final int queueSize) {
        Logger.info(String.format("[Queue-Debug] Sector: %s | Online: %b | Full: %b | Players: %d/%d | In queue: %d",
                sector, online, full,
                data != null ? data.getPlayerCount() : 0,
                data != null ? data.getMaxPlayers() : 0,
                queueSize));
    }

    private boolean isSectorFull(final SectorData sectorData) {
        if (sectorData == null) {
            return true;
        }
        return sectorData.getPlayerCount() >= sectorData.getMaxPlayers();
    }
}