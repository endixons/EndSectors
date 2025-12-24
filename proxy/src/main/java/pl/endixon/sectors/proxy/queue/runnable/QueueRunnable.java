package pl.endixon.sectors.proxy.queue.runnable;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import pl.endixon.sectors.common.sector.SectorData;
import pl.endixon.sectors.proxy.VelocitySectorPlugin;
import pl.endixon.sectors.proxy.queue.Queue;
import pl.endixon.sectors.proxy.queue.QueueManager;
import pl.endixon.sectors.proxy.manager.SectorManager;

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

    private static final int MAX_RELEASE_PER_TICK = 100;

    @Override
    public void run() {
        for (final Queue queue : this.queueManager.getMap().values()) {
            final List<Player> allPlayers = queue.getPlayers();
            if (allPlayers.isEmpty()) {
                continue;
            }

            final String sectorName = queue.getSector();
            final SectorData sectorData = this.sectorManager.getSectorData(sectorName);
            final boolean isOnline = sectorData != null && sectorData.isOnline();

            final boolean isMaxPlayer = sectorData != null
                    && sectorData.getPlayerCount() >= sectorData.getMaxPlayers();


            final int initialSize = allPlayers.size();
            final List<Player> admins = new ArrayList<>(initialSize / 10);
            final List<Player> vips = new ArrayList<>(initialSize / 4);
            final List<Player> regulars = new ArrayList<>(initialSize);

            for (final Player p : allPlayers) {
                if (p.getCurrentServer().map(s -> !s.getServerInfo().getName().equalsIgnoreCase(QUEUE_SERVER_NAME)).orElse(true)) {
                    allPlayers.remove(p);
                    continue;
                }

                if (p.hasPermission("queue.admin")) {
                    admins.add(p);
                } else if (p.hasPermission("queue.vip")) {
                    vips.add(p);
                } else {
                    regulars.add(p);
                }
            }

            final List<Player> sortedQueue = new ArrayList<>(initialSize);
            sortedQueue.addAll(admins);
            sortedQueue.addAll(vips);
            sortedQueue.addAll(regulars);

            int released = 0;
            final int total = sortedQueue.size();

            for (int i = 0; i < total; i++) {
                final Player player = sortedQueue.get(i);
                final int position = i + 1;

                if (isOnline && !isMaxPlayer && released < MAX_RELEASE_PER_TICK) {
                    this.proxyServer.getServer(sectorName).ifPresent(server ->
                            player.createConnectionRequest(server).fireAndForget()
                    );
                    released++;
                }

                this.dispatchTitle(player, sectorName, isOnline, position, total,isMaxPlayer);
            }
        }
    }

    private void dispatchTitle(Player player, String sector, boolean online, int pos, int total,boolean full) {

        if (pos < 300 && online) {
            final String cacheKey = "on_" + pos + "_" + total;
            final Component subtitle = SUBTITLE_CACHE.computeIfAbsent(cacheKey, k -> this.buildSubtitle(sector, true, pos, total,full));
            player.showTitle(Title.title(TITLE_CACHE, subtitle));
            return;
        }

        player.showTitle(Title.title(TITLE_CACHE, this.buildSubtitle(sector, online, pos, total, full)));
    }

    private Component buildSubtitle(String sector, boolean online, int pos, int total,boolean full) {
        if (!online) {
            return MM.deserialize("<red>Sektor <white>" + sector + "</white> jest <bold>OFFLINE</bold></red>");
        }
        if (!full) {
            return   MM.deserialize("<red>Sektor <white>" + sector + "</white> jest <bold>PELNY</bold></red>");

        }
        return MM.deserialize("<gray>Pozycja: <#00d2ff>" + pos + "</#00d2ff><dark_gray>/</dark_gray><#3a7bd5>" + total + "</#3a7bd5>");
    }
}