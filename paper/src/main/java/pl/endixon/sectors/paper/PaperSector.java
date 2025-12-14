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

package pl.endixon.sectors.paper;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import pl.endixon.sectors.common.packet.PacketChannel;
import pl.endixon.sectors.common.packet.object.*;
import pl.endixon.sectors.common.redis.RedisManager;
import pl.endixon.sectors.paper.command.ChannelCommand;
import pl.endixon.sectors.paper.config.ConfigLoader;
import pl.endixon.sectors.paper.listener.player.*;
import pl.endixon.sectors.paper.command.SectorCommand;
import pl.endixon.sectors.paper.listener.other.MoveListener;
import pl.endixon.sectors.paper.redis.listener.*;
import pl.endixon.sectors.paper.redis.packet.PacketExecuteCommand;
import pl.endixon.sectors.paper.redis.packet.PacketPlayerInfoRequest;
import pl.endixon.sectors.paper.redis.packet.PacketSectorInfo;
import pl.endixon.sectors.paper.sector.ProtocolLibWorldBorderTask;
import pl.endixon.sectors.paper.sector.Sector;
import pl.endixon.sectors.paper.sector.transfer.SectorTeleportService;
import pl.endixon.sectors.paper.sector.SectorManager;
import pl.endixon.sectors.paper.task.*;
import pl.endixon.sectors.paper.user.UserManager;
import pl.endixon.sectors.paper.util.Logger;
import java.util.List;

@Getter
public class PaperSector extends JavaPlugin {

    @Getter
    private static PaperSector instance;
    private ProtocolManager protocolManager;
    private SectorManager sectorManager;
    private UserManager userManager;
    public final RedisManager redisManager = new RedisManager();
    private boolean inited = false;
    private final SectorTeleportService sectorTeleportService = new SectorTeleportService(this);
    private final SendSectorInfoTask sectorInfoTask = new SendSectorInfoTask(this);

    public RedisManager getRedisService() {
        return redisManager;
    }


    @Override
    public void onEnable() {
        instance = this;
        protocolManager = ProtocolLibrary.getProtocolManager();
        this.initManager();
        this.redisManager.publish(
                PacketChannel.PACKET_CONFIGURATION_REQUEST,
                new PacketConfigurationRequest(this.getSectorManager().getCurrentSectorName())
        );
        this.initListeners();
        this.initCommands();
        this.scheduleTasks();
        Logger.info("Włączono EndSectors!");
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
            Logger.info("Aktualny sektor jest NULL! Upewnij się, że sektor '" + currentSectorName + "' jest dodany w configu proxy!");
            Bukkit.shutdown();
            return;
        }
        Logger.info("Załadowano " + sectorManager.getSectors().size() + " sektorów!");
        Logger.info("Aktualny sektor: " + currentSectorName);
        if (!inited) {
            inited = true;
            Bukkit.getScheduler().runTaskTimerAsynchronously(
                    this,
                    sectorInfoTask,
                    0L,
                    20L * 10
            );
        }
        redisManager.publish(PacketChannel.PACKET_SECTOR_CONNECTED, new PacketSectorConnected(currentSectorName));
    }



    private void initManager() {
        Logger.info("Inicjalizacja managerów...");
        ConfigLoader config = ConfigLoader.load(getDataFolder());
        this.sectorManager = new SectorManager(this, config.currentSector);
        this.userManager = new UserManager();
        this.redisManager.initialize("127.0.0.1", 6379, "");
        this.redisManager.subscribe(config.currentSector, new PacketConfigurationPacketListener(), PacketConfiguration.class);
        this.redisManager.subscribe(PacketChannel.PACKET_SECTOR_INFO_QUEUE, new PacketSectorInfoPacketListener(), PacketSectorInfo.class);
        this.redisManager.subscribe(PacketChannel.PACKET_EXECUTE_COMMAND, new PacketExecuteCommandPacketListener(), PacketExecuteCommand.class);
        this.redisManager.subscribe(PacketChannel.PACKET_PLAYER_INFO_REQUEST, new PacketPlayerInfoRequestPacketListener(), PacketPlayerInfoRequest.class);
        this.redisManager.subscribe(PacketChannel.PACKET_SECTOR_CHAT_BROADCAST, new PacketSectorChatBroadcastPacketListener(), PacketSectorChatBroadcast.class);
        this.redisManager.subscribe(PacketChannel.PACKET_SECTOR_INFO, new PacketSectorInfoPacketListener(), PacketSectorInfo.class);
        this.redisManager.subscribe(PacketChannel.USER_CHECK_REQUEST, new PacketUserCheckListener(), PacketUserCheck.class);
        this.redisManager.subscribe(PacketChannel.PACKET_SECTOR_CONNECTED, new PacketSectorConnectedPacketListener(), PacketSectorConnected.class);
        this.redisManager.subscribe(PacketChannel.PACKET_SECTOR_DISCONNECTED, new PacketSectorDisconnectedPacketListener(), PacketSectorDisconnected.class);
        Logger.info("Zainicjalizowano managery");
    }



    private void initListeners() {
        List<Listener> listeners = List.of(
                new PlayerRespawnListener(this),
                new PlayerKickListener(),
                new PlayerQuitListener(),
                new PlayerLoginListener(this),
                new PlayerSectorInteractListener(sectorManager, this),
                new PlayerLocallyJoinListener(this),
                new PortalListener(this),
                new InventoryInternactListener(),
                new PlayerChatListener(this),
                new MoveListener(this)
        );
        listeners.forEach(listener -> getServer().getPluginManager().registerEvents(listener, this));
    }

    private void initCommands() {
        getCommand("sector").setExecutor(new SectorCommand(this));
        getCommand("channel").setExecutor(new ChannelCommand(sectorManager, sectorTeleportService));
    }

    private void scheduleTasks() {
        new ProtocolLibWorldBorderTask(sectorManager).runTaskTimer(this, 20L, 20L);
        new SpawnScoreboardTask(sectorManager).runTaskTimer(this, 20L, 20L);
        new SendInfoPlayerTask(this).runTaskTimer(this, 12000L, 12000L);
        new BorderActionBarTask(this).runTaskTimer(this, 5L, 5L);

    }
    public static PaperSector getInstance() {
        return instance;
    }

}

