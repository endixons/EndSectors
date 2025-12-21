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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import pl.endixon.sectors.common.packet.PacketChannel;
import pl.endixon.sectors.common.packet.object.*;
import pl.endixon.sectors.common.redis.RedisManager;
import pl.endixon.sectors.common.sector.SectorData;
import pl.endixon.sectors.common.sector.SectorType;
import pl.endixon.sectors.common.util.Corner;
import pl.endixon.sectors.proxy.config.ConfigCreator;
import pl.endixon.sectors.proxy.listener.LastSectorConnectListener;
import pl.endixon.sectors.proxy.manager.SectorManager;
import pl.endixon.sectors.proxy.manager.TeleportationManager;
import pl.endixon.sectors.proxy.queue.QueueManager;
import pl.endixon.sectors.proxy.queue.runnable.QueueRunnable;
import pl.endixon.sectors.proxy.redis.listener.*;
import pl.endixon.sectors.proxy.util.Logger;

@Plugin(id = "endsectors-proxy", name = "EndSectorsProxy", version = "1.0")
@Getter
public class VelocitySectorPlugin {

    @Getter
    private static VelocitySectorPlugin instance;
    private final ObjectMapper mapper = new ObjectMapper();
    private final ProxyServer server;
    private final Path dataDirectory;
    private SectorManager sectorManager;

    public final RedisManager redisManager = new RedisManager();
    private QueueManager QueueManager;
    private TeleportationManager teleportationManager;

    @Inject
    public VelocitySectorPlugin(ProxyServer server, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.dataDirectory = dataDirectory;
    }

    public RedisManager getRedisService() {
        return redisManager;
    }

    @Subscribe
    public void onProxyInitialize(com.velocitypowered.api.event.proxy.ProxyInitializeEvent event) {
        instance = this;
        Logger.info("Starting EndSectors-Proxy...");
        System.setProperty("io.netty.transport.noNative", "true");
        this.sectorManager = new SectorManager();
        this.teleportationManager = new TeleportationManager();
        this.QueueManager = new QueueManager();
        this.loadSectors();
        this.initRedisManager();
        this.initListeners();
        this.getServer().getScheduler().buildTask(this, new QueueRunnable()).repeat(2, TimeUnit.SECONDS).schedule();
        Logger.info("EndSectors-Proxy started successfully.");
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        this.redisManager.shutdown();
    }

    private void loadSectors() {
        Path configPath = getDataDirectory().resolve("config.json");

        if (!Files.exists(configPath)) {
            Logger.info("config.json not found in the plugin directory! Creating default configuration...");
            ConfigCreator.createDefaultConfig(getDataDirectory());
            return;
        }
        try {
            TypeReference<Map<String, Object>> typeRef = new TypeReference<>() {
            };
            Map<String, Object> root = mapper.readValue(Files.newBufferedReader(configPath), typeRef);
            Object sectorsObj = root.get("sectors");
            if (!(sectorsObj instanceof Map)) {
                Logger.info("Section 'sectors' was not found in config.json!");
                return;
            }
            Map<String, Object> sectors = (Map<String, Object>) sectorsObj;
            for (Map.Entry<String, Object> typeEntry : sectors.entrySet()) {
                String typeName = typeEntry.getKey();
                Object typeDataObj = typeEntry.getValue();
                if (!(typeDataObj instanceof Map))
                    continue;

                Map<String, Object> typeDataMap = (Map<String, Object>) typeDataObj;
                Map<String, Map<String, Object>> sectorDataMap = new LinkedHashMap<>();

                for (Map.Entry<String, Object> sectorEntry : typeDataMap.entrySet()) {
                    if (sectorEntry.getValue() instanceof Map) {
                        sectorDataMap.put(sectorEntry.getKey(), (Map<String, Object>) sectorEntry.getValue());
                    }
                }

                addSectorsToManager(sectorDataMap, typeName);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addSectorsToManager(Map<String, Map<String, Object>> sectors, String typeName) {
        List<String> loadedSectors = new ArrayList<>();

        for (Map.Entry<String, Map<String, Object>> entry : sectors.entrySet()) {
            String sectorName = entry.getKey();
            Map<String, Object> data = entry.getValue();
            try {
                int pos1X = ((Number) data.get("pos1X")).intValue();
                int pos1Z = ((Number) data.get("pos1Z")).intValue();
                int pos2X = ((Number) data.get("pos2X")).intValue();
                int pos2Z = ((Number) data.get("pos2Z")).intValue();
                String world = (String) data.get("world");
                String typeStr = (String) data.get("type");

                SectorType sectorType = SectorType.valueOf(typeStr.toUpperCase());

                Corner corner1 = new Corner(pos1X, pos1Z);
                Corner corner2 = new Corner(pos2X, pos2Z);

                sectorManager.addSectorData(new SectorData(sectorName, corner1, corner2, world, sectorType));
                loadedSectors.add(sectorName + " (" + sectorType + ")");

            } catch (Exception e) {
                Logger.info("Failed to save sector '" + sectorName + "': " + e.getMessage());
            }
        }

        if (!loadedSectors.isEmpty()) {
            Logger.info("Loaded sectors of type " + typeName + ": " + String.join(", ", loadedSectors));
        } else {
            Logger.info("No sectors to load for type " + typeName + ".");
        }
    }

    private void initRedisManager() {
        this.redisManager.initialize("127.0.0.1", 6379, "");
        this.redisManager.subscribe(PacketChannel.PACKET_CONFIGURATION_REQUEST, new PacketConfigurationRequestPacketListener(), PacketConfigurationRequest.class);
        this.redisManager.subscribe(PacketChannel.PACKET_BROADCAST_MESSAGE, new PacketBroadcastMessagePacketListener(), PacketBroadcastMessage.class);
        this.redisManager.subscribe(PacketChannel.PACKET_SEND_MESSAGE_TO_PLAYER, new PacketSendMessageToPlayerPacketListener(), PacketSendMessageToPlayer.class);
        this.redisManager.subscribe(PacketChannel.PACKET_BROADCAST_TITLE, new PacketBroadcastTitlePacketListener(), PacketBroadcastTitle.class);
        this.redisManager.subscribe(PacketChannel.USER_CHECK_RESPONSE, new PacketUserCheckProxyListener(), PacketUserCheck.class);
        this.redisManager.subscribe(PacketChannel.PACKET_TELEPORT_TO_SECTOR, new TeleportToSectorListener(), PacketRequestTeleportSector.class);
        this.redisManager.subscribe(PacketChannel.PACKET_SECTOR_CONNECTED, new PacketSectorConnectedPacketListener(), PacketSectorConnected.class);
        this.redisManager.subscribe(PacketChannel.PACKET_SECTOR_DISCONNECTED, new PacketSectorDisconnectedPacketListener(), PacketSectorDisconnected.class);
        Logger.info("RedisManager initialized.");
    }

    private void initListeners() {
        server.getEventManager().register(this, new LastSectorConnectListener(this));

        server.getEventManager().register(this, new Object() {
            @Subscribe
            public void onPlayerLogin(LoginEvent event) {
                redisManager.addOnlinePlayer(event.getPlayer().getUsername());
            }

            @Subscribe
            public void onPlayerDisconnect(DisconnectEvent event) {
                redisManager.removeOnlinePlayer(event.getPlayer().getUsername());
            }
        });
    }

    public ProxyServer getServerInstance() {
        return server;
    }
}
