package pl.endixon.sectors.proxy.queue.runnable;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import pl.endixon.sectors.common.sector.SectorData;
import pl.endixon.sectors.common.util.ChatUtil;
import pl.endixon.sectors.proxy.VelocitySectorPlugin;
import pl.endixon.sectors.proxy.queue.QueueManager;
import pl.endixon.sectors.proxy.queue.Queue;
import pl.endixon.sectors.proxy.manager.SectorManager;
import pl.endixon.sectors.proxy.user.RedisUserService;
import pl.endixon.sectors.proxy.util.Logger;

import java.util.Optional;

public class QueueRunnable implements Runnable {

    private final ProxyServer proxyServer = VelocitySectorPlugin.getInstance().getServer();
    private final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacy(LegacyComponentSerializer.SECTION_CHAR);
    private final QueueManager queueService = VelocitySectorPlugin.getInstance().getQueueManager();
    private final SectorManager sectorManager = VelocitySectorPlugin.getInstance().getSectorManager();
    private final VelocitySectorPlugin plugin = VelocitySectorPlugin.getInstance();
    private final RedisUserService redisUserService = new RedisUserService(plugin);

    @Override
    public void run() {
        for (Queue queue : queueService.getMap().values()) {
            if (queue.getPlayers().isEmpty()) continue;


            queue.getPlayers().removeIf(player ->
                    player.getCurrentServer().isEmpty() ||
                            !player.getCurrentServer().get().getServerInfo().getName().equalsIgnoreCase("queue")
            );
            if (queue.getPlayers().isEmpty()) continue;

            int idx = 1;
            for (Player player : queue.getPlayers()) {
                if (player.getCurrentServer().isEmpty() ||
                        !player.getCurrentServer().get().getServerInfo().getName().equalsIgnoreCase("queue"))
                    continue;


                Optional<String> optionalSectorName = redisUserService.getSectorName(player.getUsername());
                String lastSectorName = optionalSectorName.orElse(queue.getSector());
                SectorData sector = null;

                if (lastSectorName != null && !lastSectorName.equalsIgnoreCase("null")) {
                    sector = sectorManager.getSectorData(lastSectorName);
                }

                if (sector == null) {
                    Optional<SectorData> randomOnline = sectorManager.getRandomNonQueueSector();

                    if (randomOnline.isPresent()) {
                        sector = randomOnline.get();
                        lastSectorName = sector.getName();
                        queue.setSector(lastSectorName);
                        redisUserService.setSectorName(player.getUsername(), lastSectorName);
                        Logger.info("[QueueRunnable] Gracz " + player.getUsername() + " nie miał lastSector, przypisano losowy sektor: " + lastSectorName);
                    }
                }


                boolean sectorOnline = sector != null && sector.isOnline();

                if (sectorOnline) {
                    RegisteredServer server = proxyServer.getServer(lastSectorName).orElse(null);
                    if (server != null) {
                        player.createConnectionRequest(server).fireAndForget();
                        Logger.info("[Queue] Gracz " + player.getUsername() + " łączy się do sektora " + lastSectorName);
                    }
                }

                String subtitle;
                if (!sectorOnline) {
                    subtitle = "&cSektor " + lastSectorName + " offline, czekasz w kolejce";
                } else {
                    subtitle = "&fJesteś aktualnie w kolejce&8: &6" + idx + "&8/&e" + queue.getPlayers().size();
                }

                player.showTitle(Title.title(
                        LEGACY_SERIALIZER.deserialize(ChatUtil.fixColors("&f&l➤ &e&lKolejka")),
                        LEGACY_SERIALIZER.deserialize(ChatUtil.fixColors(subtitle))
                ));
                idx++;
            }
        }
    }
}
