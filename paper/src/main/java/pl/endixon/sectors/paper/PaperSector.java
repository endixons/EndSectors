package pl.endixon.sectors.paper;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import java.util.List;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import pl.endixon.sectors.common.packet.PacketChannel;
import pl.endixon.sectors.common.packet.object.*;
import pl.endixon.sectors.common.redis.RedisManager;
import pl.endixon.sectors.paper.command.ChannelCommand;
import pl.endixon.sectors.paper.command.SectorCommand;
import pl.endixon.sectors.paper.config.ConfigLoader;
import pl.endixon.sectors.paper.config.MessageLoader;

import pl.endixon.sectors.paper.manager.SectorManager;
import pl.endixon.sectors.paper.redis.listener.*;
import pl.endixon.sectors.paper.redis.packet.PacketExecuteCommand;
import pl.endixon.sectors.paper.redis.packet.PacketPlayerInfoRequest;
import pl.endixon.sectors.paper.redis.packet.PacketSectorInfo;
import pl.endixon.sectors.paper.sector.Sector;
import pl.endixon.sectors.paper.sector.SectorTeleport;
import pl.endixon.sectors.paper.task.*;
import pl.endixon.sectors.paper.user.listeners.*;
import pl.endixon.sectors.paper.user.profile.UserProfileCache;
import pl.endixon.sectors.paper.user.profile.UserProfileRepository;
import pl.endixon.sectors.paper.util.LoggerUtil;

@Getter
public class PaperSector extends JavaPlugin {

    @Getter
    private static PaperSector instance;
    private ProtocolManager protocolManager;
    private SectorManager sectorManager;
    public final RedisManager redisManager = new RedisManager();
    private boolean inited = false;
    private final SectorTeleport sectorTeleport = new SectorTeleport(this);
    private final SendSectorInfoTask sectorInfoTask = new SendSectorInfoTask(this);
    private ConfigLoader configuration;
    private MessageLoader messageLoader;


    @Override
    public void onEnable() {
        instance = this;
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "lunar:apollo");

        protocolManager = ProtocolLibrary.getProtocolManager();
        this.loadFiles();
        this.initManager(configuration);
        this.redisManager.publish(PacketChannel.PACKET_CONFIGURATION_REQUEST, new PacketConfigurationRequest(this.getSectorManager().getCurrentSectorName()));

        this.initListeners();
        this.initCommands();
        this.scheduleTasks(configuration);

        new SectorsAPI(this);
        this.loadAllPlayers();

        LoggerUtil.info("EndSectors enabled successfully.");
    }


    public void loadFiles() {
        LoggerUtil.info("Loading system configurations...");
        this.configuration = ConfigLoader.load(getDataFolder());
        this.messageLoader = MessageLoader.load(getDataFolder());
        LoggerUtil.info("Configuration and Messages synchronized.");
    }

    @Override
    public void onDisable() {
        PacketSectorDisconnected packet = new PacketSectorDisconnected(this.getSectorManager().getCurrentSectorName());
        this.redisManager.publish(PacketChannel.PACKET_SECTOR_DISCONNECTED, packet);
        this.redisManager.shutdown();

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.kickPlayer("§cSektor " + sectorManager.getCurrentSectorName() + " §czostał zamknięty i jest niedostępny!");
        }
    }

    public void init() {
        Sector currentSector = sectorManager.getCurrentSector();
        String currentSectorName = sectorManager.getCurrentSectorName();

        if (currentSector == null) {
            LoggerUtil.info("Current sector is NULL! Make sure that the sector name '" + currentSectorName + "' matches the one defined in the proxy plugin configuration and in velocity.toml.");
            Bukkit.shutdown();
            return;
        }

        LoggerUtil.info("Loaded " + sectorManager.getSectors().size() + " sectors!");
        LoggerUtil.info("Current sector: " + currentSectorName);

        if (!inited) {
            inited = true;
            Bukkit.getScheduler().runTaskTimerAsynchronously(this, sectorInfoTask, 0L, 20L * 10);
        }
        redisManager.publish(PacketChannel.PACKET_SECTOR_CONNECTED, new PacketSectorConnected(currentSectorName));
    }

    private void loadAllPlayers() {
        UserProfileCache.warmup();
    }

    private void initManager(ConfigLoader config) {
        LoggerUtil.info("Initializing managers...");
        this.sectorManager = new SectorManager(this, config.currentSector);
        this.redisManager.initialize("127.0.0.1", 6379, "");

        this.redisManager.subscribe(config.currentSector, new PacketConfigurationPacketListener(), PacketConfiguration.class);
        this.redisManager.subscribe(PacketChannel.PACKET_EXECUTE_COMMAND, new PacketExecuteCommandPacketListener(), PacketExecuteCommand.class);
        this.redisManager.subscribe(PacketChannel.PACKET_PLAYER_INFO_REQUEST, new PacketPlayerInfoRequestPacketListener(), PacketPlayerInfoRequest.class);
        this.redisManager.subscribe(PacketChannel.PACKET_SECTOR_CHAT_BROADCAST, new PacketSectorChatBroadcastPacketListener(), PacketSectorChatBroadcast.class);
        this.redisManager.subscribe(PacketChannel.PACKET_SECTOR_INFO, new PacketSectorInfoPacketListener(), PacketSectorInfo.class);
        this.redisManager.subscribe(PacketChannel.USER_CHECK_REQUEST, new PacketUserCheckListener(), PacketUserCheck.class);
        this.redisManager.subscribe(PacketChannel.PACKET_SECTOR_CONNECTED, new PacketSectorConnectedPacketListener(), PacketSectorConnected.class);
        this.redisManager.subscribe(PacketChannel.PACKET_SECTOR_DISCONNECTED, new PacketSectorDisconnectedPacketListener(), PacketSectorDisconnected.class);

        LoggerUtil.info("Managers initialized successfully.");
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
        getCommand("channel").setExecutor(new ChannelCommand(sectorManager, sectorTeleport));
    }

    public void scheduleTasks(ConfigLoader config) {
        if (config.scoreboardEnabled) {
            new SpawnScoreboardTask(sectorManager, config).runTaskTimer(this, 0L, 20L);
        }
        new ProtocolLibWorldBorderTask(sectorManager).runTaskTimer(this, 20L, 20L);
        new BorderActionBarTask(this).runTaskTimer(this, 20L, 20L);
        new SendInfoPlayerTask(this).runTaskTimer(this, 12000L, 12000L);
    }

    public RedisManager getRedisService() {
        return redisManager;
    }
}