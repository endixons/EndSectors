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

package pl.endixon.sectors.proxy;


import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import pl.endixon.sectors.common.Common;
import pl.endixon.sectors.common.packet.PacketChannel;
import pl.endixon.sectors.common.packet.object.*;
import pl.endixon.sectors.proxy.command.SectorsCommand;
import pl.endixon.sectors.proxy.config.ConfigLoader;
import pl.endixon.sectors.proxy.config.MessageLoader;
import pl.endixon.sectors.proxy.hook.CommonHeartbeatHook;
import pl.endixon.sectors.proxy.nats.listener.*;
import pl.endixon.sectors.proxy.user.listener.InfrastructureIntegrityListener;
import pl.endixon.sectors.proxy.user.listener.LastSectorConnectListener;
import pl.endixon.sectors.proxy.manager.SectorManager;
import pl.endixon.sectors.proxy.manager.QueueManager;
import pl.endixon.sectors.proxy.runnable.QueueRunnable;
import pl.endixon.sectors.proxy.user.listener.PlayerConnectionListener;
import pl.endixon.sectors.proxy.user.listener.ProxyPingListener;
import pl.endixon.sectors.proxy.user.profile.UserProfileCache;
import pl.endixon.sectors.proxy.util.LoggerUtil;

@Plugin(id = "endsectors-proxy", name = "EndSectorsProxy", version = "1.0")
@Getter
public class VelocitySectorPlugin {

    @Getter
    private static VelocitySectorPlugin instance;
    private final ProxyServer server;
    private final Path dataDirectory;
    private SectorManager sectorManager;
    private UserProfileCache userProfileCache;
    private QueueManager QueueManager;
    public ConfigLoader configLoader;
    public MessageLoader messageLoader;

    private CommonHeartbeatHook heartbeatHook;

    @Inject
    public VelocitySectorPlugin(ProxyServer server, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.dataDirectory = dataDirectory;
    }



    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        instance = this;
        this.sectorManager = new SectorManager();
        this.loadFiles();
        this.userProfileCache = new UserProfileCache(this);
        this.QueueManager = new QueueManager();;
        Common.initInstance();

        Common.getInstance().initializeRedis(
                configLoader.redisHost,
                configLoader.redisPort,
                configLoader.redisPassword
        );

        Common.getInstance().initializeNats(
                configLoader.natsUrl,
                configLoader.natsConnectionName
        );

        this.heartbeatHook = new CommonHeartbeatHook(this.server);
        this.heartbeatHook.checkConnection();
        this.initNatsSubscriptions();
        this.initListeners();
        this.initCommands();
        this.getServer().getScheduler().buildTask(this, new QueueRunnable()).repeat(2, TimeUnit.SECONDS).schedule();
        LoggerUtil.info("EndSectors-Proxy enabled successfully.");
    }


    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        if (this.heartbeatHook != null) {
            this.heartbeatHook.shutdown();
        }
        Common.getInstance().shutdown();
        LoggerUtil.info("EndSectors-Proxy has been successfully shut down.");
    }

    public void loadFiles() {
        this.configLoader = ConfigLoader.load(this);
        this.messageLoader = MessageLoader.load(this);

    }

        private void initCommands() {
        CommandManager commandManager = server.getCommandManager();
        CommandMeta meta = commandManager.metaBuilder("sectors").aliases("proxysectors", "sreload").plugin(this).build();
        commandManager.register(meta, new SectorsCommand(this));
        LoggerUtil.info("Command /sectors has been registered.");
    }

    private void initNatsSubscriptions() {

        Common.getInstance().getNatsManager().subscribe(
                PacketChannel.PACKET_CONFIGURATION_REQUEST.getSubject(),
                new PacketConfigurationRequestPacketListener(),
                PacketConfigurationRequest.class
        );

        Common.getInstance().getNatsManager().subscribe(
                PacketChannel.PACKET_BROADCAST_MESSAGE.getSubject(),
                new PacketBroadcastMessagePacketListener(),
                PacketBroadcastMessage.class
        );

        Common.getInstance().getNatsManager().subscribe(
                PacketChannel.PACKET_SEND_MESSAGE_TO_PLAYER.getSubject(),
                new PacketSendMessageToPlayerPacketListener(),
                PacketSendMessageToPlayer.class
        );

        Common.getInstance().getNatsManager().subscribe(
                PacketChannel.USER_CHECK_RESPONSE.getSubject(),
                new PacketUserCheckProxyListener(),
                PacketUserCheck.class
        );

        Common.getInstance().getNatsManager().subscribe(
                PacketChannel.PACKET_TELEPORT_TO_SECTOR.getSubject(),
                new TeleportToSectorListener(),
                PacketRequestTeleportSector.class
        );

        Common.getInstance().getNatsManager().subscribe(
                PacketChannel.PACKET_SECTOR_CONNECTED.getSubject(),
                new PacketSectorConnectedPacketListener(),
                PacketSectorConnected.class
        );

        Common.getInstance().getNatsManager().subscribe(
                PacketChannel.PACKET_SECTOR_DISCONNECTED.getSubject(),
                new PacketSectorDisconnectedPacketListener(),
                PacketSectorDisconnected.class
        );

        Common.getInstance().getNatsManager().subscribe(
                PacketChannel.PACKET_SECTOR_INFO.getSubject(),
                new PacketSectorInfoPacketListener(),
                PacketSectorInfo.class
        );
    }


    private void initListeners() {
        server.getEventManager().register(this, new LastSectorConnectListener(this));
        server.getEventManager().register(this, new ProxyPingListener());
        server.getEventManager().register(this, new InfrastructureIntegrityListener());
        server.getEventManager().register(this, new PlayerConnectionListener());
    }

    public ProxyServer getServerInstance() {
        return server;
    }

}
