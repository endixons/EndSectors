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

    package pl.endixon.sectors.proxy.listener;

    import com.google.inject.Inject;
    import com.velocitypowered.api.event.Subscribe;
    import com.velocitypowered.api.event.player.ServerConnectedEvent;
    import com.velocitypowered.api.proxy.Player;
    import pl.endixon.sectors.common.packet.PacketChannel;
    import pl.endixon.sectors.common.packet.object.PacketUserCheck;
    import pl.endixon.sectors.proxy.VelocitySectorPlugin;
    import pl.endixon.sectors.proxy.queue.QueueManager;
    import java.util.concurrent.Executors;
    import java.util.concurrent.ScheduledExecutorService;
    import java.util.concurrent.TimeUnit;

    public class LastSectorConnectListener {

        private final VelocitySectorPlugin plugin;
        private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();


        @Inject
        public LastSectorConnectListener(VelocitySectorPlugin plugin) {
            this.plugin = plugin;
        }

        @Subscribe
        public void onServerConnected(ServerConnectedEvent event) {
            Player player = event.getPlayer();
            String connectedServer = event.getServer().getServerInfo().getName();
            if (!connectedServer.equalsIgnoreCase("queue")) return;
            QueueManager queueService = plugin.getQueueManager();
            queueService.findQueueByPlayer(player).ifPresent(queue -> queue.removePlayer(player));
            pollForUser(player);
        }

        private void pollForUser(Player player) {
            String username = player.getUsername();
            scheduler.schedule(() -> {
                PacketUserCheck packet = new PacketUserCheck(username);
                plugin.getRedisManager().publish(PacketChannel.PROXY_TO_PAPER, packet);
            }, 200, TimeUnit.MILLISECONDS);
        }
    }