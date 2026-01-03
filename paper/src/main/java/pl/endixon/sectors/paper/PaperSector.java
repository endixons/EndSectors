/*
 *
 * EndSectors – Non-Commercial License
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

package pl.endixon.sectors.paper;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import java.util.List;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import pl.endixon.sectors.common.Common;
import pl.endixon.sectors.common.packet.PacketChannel;
import pl.endixon.sectors.common.packet.object.*;
import pl.endixon.sectors.common.sector.SectorType;
import pl.endixon.sectors.paper.command.ChannelCommand;
import pl.endixon.sectors.paper.command.SectorCommand;
import pl.endixon.sectors.paper.config.ConfigLoader;
import pl.endixon.sectors.paper.config.MessageLoader;
import pl.endixon.sectors.paper.hook.CommonHeartbeatHook;
import pl.endixon.sectors.paper.manager.SectorManager;
import pl.endixon.sectors.paper.nats.listener.*;
import pl.endixon.sectors.paper.nats.packet.PacketExecuteCommand;
import pl.endixon.sectors.paper.nats.packet.PacketPlayerInfoRequest;
import pl.endixon.sectors.paper.sector.SectorTeleport;
import pl.endixon.sectors.paper.task.*;
import pl.endixon.sectors.paper.user.listeners.*;
import pl.endixon.sectors.paper.user.profile.UserProfileCache;
import pl.endixon.sectors.paper.util.LoggerUtil;

@Getter
public class PaperSector extends JavaPlugin {

    @Getter
    private static PaperSector instance;
    private ProtocolManager protocolManager;
    private SectorManager sectorManager;
    private SectorTeleport sectorTeleport;
    private ConfigLoader configuration;
    private MessageLoader messageLoader;
    private CommonHeartbeatHook heartbeatHook;

    @Override
    public void onEnable() {
        instance = this;
        Common.initInstance();
        this.loadFiles();
        this.protocolManager = ProtocolLibrary.getProtocolManager();
        this.sectorManager = new SectorManager(this, configuration.currentSector);
        this.sectorTeleport = new SectorTeleport(this);

        Common.getInstance().initializeRedis(
                configuration.redisHost,
                configuration.redisPort,
                configuration.redisPassword
        );

        Common.getInstance().initializeNats(
                configuration.natsUrl,
                configuration.natsConnectionName
        );

        this.heartbeatHook = new CommonHeartbeatHook(this);
        this.heartbeatHook.checkConnection();
        this.initNatsSubscriptions(configuration);
        Common.getInstance().getNatsManager().publish(PacketChannel.PACKET_CONFIGURATION_REQUEST.getSubject(), new PacketConfigurationRequest(sectorManager.getCurrentSectorName()));
        this.initCommands();
       this.initListeners();
        this.scheduleTasks(configuration);
        this.loadAllPlayers();
        new SectorsAPI(this);
        LoggerUtil.info("EndSectors-Paper enabled successfully.");
    }

    @Override
    public void onDisable() {
        PacketSectorDisconnected packet = new PacketSectorDisconnected(this.sectorManager.getCurrentSectorName());
        Common.getInstance().getNatsManager().publish(PacketChannel.PACKET_SECTOR_DISCONNECTED.getSubject(), packet);

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.kick(Component.text("§cSektor " + sectorManager.getCurrentSectorName() + " §czostał zamknięty i jest niedostępny!"));
        }
        if (this.heartbeatHook != null) {
            this.heartbeatHook.shutdown();
        }
        Common.getInstance().shutdown();
    }

    public void loadFiles() {
        this.configuration = ConfigLoader.load(getDataFolder());
        this.messageLoader = MessageLoader.load(getDataFolder());
    }

    private void loadAllPlayers() {
        UserProfileCache.warmup();
    }



    private void initNatsSubscriptions(ConfigLoader config) {
        var nats = Common.getInstance().getNatsManager();
        nats.subscribe(
                config.currentSector,
                new PacketConfigurationPacketListener(),
                PacketConfiguration.class
        );

        nats.subscribe(
                PacketChannel.PACKET_EXECUTE_COMMAND.getSubject(),
                new PacketExecuteCommandPacketListener(),
                PacketExecuteCommand.class
        );

        nats.subscribe(
                PacketChannel.PACKET_PLAYER_INFO_REQUEST.getSubject(),
                new PacketPlayerInfoRequestPacketListener(),
                PacketPlayerInfoRequest.class
        );

        nats.subscribe(
                PacketChannel.PACKET_SECTOR_CHAT_BROADCAST.getSubject(),
                new PacketSectorChatBroadcastPacketListener(),
                PacketSectorChatBroadcast.class
        );

        nats.subscribe(
                PacketChannel.PACKET_SECTOR_INFO.getSubject(),
                new PacketSectorInfoPacketListener(),
                pl.endixon.sectors.paper.nats.packet.PacketSectorInfo.class
        );

        nats.subscribe(
                PacketChannel.PACKET_SECTOR_CONNECTED.getSubject(),
                new PacketSectorConnectedPacketListener(),
                PacketSectorConnected.class
        );

        nats.subscribe(
                PacketChannel.PACKET_SECTOR_DISCONNECTED.getSubject(),
                new PacketSectorDisconnectedPacketListener(),
                PacketSectorDisconnected.class
        );

        if (SectorType.isQueueSector(config.currentSector)) {
            nats.subscribe(
                    PacketChannel.USER_CHECK_REQUEST.getSubject(),
                    new PacketUserCheckListener(),
                    PacketUserCheck.class
            );
        }
    }

    private void initListeners() {
        List<Listener> listeners = List.of(
                new PlayerRespawnListener(this),
                new PlayerDisconnectListener(),
                new PlayerLoginListener(this),
                new PlayerSectorInteractListener(sectorManager, this),
                new PlayerLocallyJoinListener(this),
                new PlayerPortalListener(this),
                new PlayerTeleportListener(this),
                new PlayerInventoryInteractListener(),
                new PlayerChatListener(this),
                new PlayerMoveListener(this)
        );
        listeners.forEach(listener -> getServer().getPluginManager().registerEvents(listener, this));
    }

    private void initCommands() {
        getCommand("sector").setExecutor(new SectorCommand(this));
        getCommand("channel").setExecutor(new ChannelCommand(sectorManager,sectorTeleport));
    }

    public void scheduleTasks(ConfigLoader config) {
        if (config.scoreboardEnabled) {
            new SpawnScoreboardTask(sectorManager, config).runTaskTimer(this, 0L, 20L);
        }
        new ProtocolLibWorldBorderTask(sectorManager).runTaskTimer(this, 20L, 20L);
        new BorderActionBarTask(this).runTaskTimer(this, 20L, 20L);
        new SendInfoPlayerTask(this).runTaskTimer(this, 12000L, 12000L);
    }
}
