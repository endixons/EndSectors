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

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import pl.endixon.sectors.common.packet.PacketChannel;
import pl.endixon.sectors.common.packet.object.PacketConfigurationRequest;
import pl.endixon.sectors.common.packet.object.PacketSectorConnected;
import pl.endixon.sectors.common.packet.object.PacketSectorDisconnected;

import pl.endixon.sectors.common.redis.MongoManager;
import pl.endixon.sectors.common.redis.RedisPacketListener;
import pl.endixon.sectors.common.redis.RedisManager;
import pl.endixon.sectors.paper.command.ChannelCommand;
import pl.endixon.sectors.paper.config.ConfigLoader;

import pl.endixon.sectors.paper.listener.player.*;
import pl.endixon.sectors.paper.command.SectorCommand;
import pl.endixon.sectors.paper.listener.other.MoveListener;
import pl.endixon.sectors.paper.redis.listener.*;
import pl.endixon.sectors.paper.sector.transfer.SectorTeleportService;
import pl.endixon.sectors.paper.sector.SectorManager;
import pl.endixon.sectors.paper.task.SectorWorldBorderTask;
import pl.endixon.sectors.paper.task.SendSectorInfoTask;
import pl.endixon.sectors.paper.task.SpawnScoreboardTask;
import pl.endixon.sectors.paper.user.UserManager;
import pl.endixon.sectors.paper.util.Logger;
import java.util.List;
import java.util.stream.Stream;

@Getter
public class PaperSector extends JavaPlugin {

    @Getter
    private static PaperSector instance;
    private SectorManager sectorManager;
    private UserManager userManager;
    private RedisManager redisManager;
    private MongoManager mongoManager;
    private boolean inited = false;
    private final SectorTeleportService sectorTeleportService = new SectorTeleportService(this);

    @Override
    public void onEnable() {
        instance = this;

        this.initManager();
        this.initListeners();
        this.initCommands();
        this.redisManager.publish(PacketChannel.PROXY, new PacketConfigurationRequest());
        this.scheduleTasks();

        Logger.info("Włączono EndSectors!");
    }


    @Override
    public void onDisable() {
        if (this.redisManager != null) {
            try {
                PacketSectorDisconnected packet = new PacketSectorDisconnected();
                this.redisManager.publish(PacketChannel.GLOBAL, packet);
                this.redisManager.shutdown();
            } catch (Exception ignored) {}
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.kickPlayer("§cSektor " + sectorManager.getCurrentSectorName() + " §czostał zamknięty i jest niedostępny!");
        }
    }


    public void init() {
        if (sectorManager.getCurrentSector() == null) {
            Logger.info("Aktualny sektor jest NULL, prawdopodobnie sektor o nazwie " + sectorManager.getCurrentSectorName() + " nie został dodany do Configu w pluginie Proxy!");
            Bukkit.shutdown();
            return;
        }
        Logger.info("Załadowano " + sectorManager.getSectors().size() + " sektorów!");
        Logger.info("Aktualny sektor " + sectorManager.getCurrentSector().getName());
        if (!inited) {
            inited = true;
            Bukkit.getScheduler().runTaskTimerAsynchronously(
                    this,
                    () -> new SendSectorInfoTask(this).run(),
                    0L,
                    20L * 10
            );
        }
        redisManager.publish(PacketChannel.GLOBAL, new PacketSectorConnected());
    }


    private void initManager() {
        Logger.info("Inicjalizacja managerów...");
        ConfigLoader config = ConfigLoader.load(getDataFolder());

        this.sectorManager = new SectorManager(this, config.currentSector);
        this.userManager = new UserManager();
        this.redisManager = new RedisManager();
        mongoManager = new MongoManager();
        this.redisManager.setPacketSender(sectorManager.getCurrentSectorName());

        Stream.of(new RedisPacketListener<?>[]{
                new PacketConfigurationPacketListener(this),
        }).forEach(redisManager::subscribe);

        Stream.of(new RedisPacketListener<?>[]{
                new PacketSpawnTeleportListener(sectorManager, this),
        }).forEach(listener -> this.redisManager.subscribe(PacketChannel.SPAWN, listener));

        Stream.of(new RedisPacketListener<?>[]{
                new PacketSectorInfoPacketListener(sectorManager),
        }).forEach(listener -> this.redisManager.subscribe(PacketChannel.QUEUE, listener));

        Stream.of(new RedisPacketListener<?>[]{
                new PacketExecuteCommandPacketListener(this),
                new PacketPlayerInfoRequestPacketListener(this),
                new PacketPermissionBroadcastMessagePacketListener(),
                new PacketSectorChatBroadcastPacketListener(this),
                new PacketSectorInfoPacketListener(this.sectorManager)
        }).forEach(listener -> this.redisManager.subscribe(PacketChannel.SECTORS, listener));

        Stream.of(new RedisPacketListener<?>[]{
                new PacketUserCheckListener(this)
        }).forEach(listener -> this.redisManager.subscribe(PacketChannel.PROXY_TO_PAPER, listener));

        Stream.of(new RedisPacketListener<?>[]{
                new PacketSectorConnectedPacketListener(sectorManager),
                new PacketSectorDisconnectedPacketListener(sectorManager)
        }).forEach(listener -> this.redisManager.subscribe(PacketChannel.GLOBAL, listener));
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
        new SectorWorldBorderTask(sectorManager).runTaskTimer(this, 65L, 65L);
        new SpawnScoreboardTask(sectorManager).runTaskTimer(this, 20L, 20L);
    }
}

