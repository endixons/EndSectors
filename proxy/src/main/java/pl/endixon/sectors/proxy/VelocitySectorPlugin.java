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
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import lombok.Getter;
import pl.endixon.sectors.common.packet.PacketChannel;
import pl.endixon.sectors.common.redis.MongoManager;
import pl.endixon.sectors.common.redis.RedisPacketListener;
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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Plugin(id = "endsectors-proxy", name = "EndSectorsProxy", version = "1.0")

@Getter
public class VelocitySectorPlugin {

    @Getter
    private static VelocitySectorPlugin instance;
    private final ObjectMapper mapper = new ObjectMapper();
    private final ProxyServer server;
    private final Path dataDirectory;
    private MongoManager mongoManager;
    private SectorManager sectorManager;

    private RedisManager redisManager;
    private QueueManager QueueManager;
    private TeleportationManager teleportationManager;

    @Inject
    public VelocitySectorPlugin(ProxyServer server, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialize(com.velocitypowered.api.event.proxy.ProxyInitializeEvent event) {
        instance = this;
        Logger.info("Uruchamiam EndSectors-Proxy...");
        this.sectorManager = new SectorManager();
        this.teleportationManager = new TeleportationManager();
        this.QueueManager = new QueueManager();
        this.loadSectors();
        this.initRedisManager();
        this.initMongoManager();
        this.initListeners();
        this.getServer().getScheduler().buildTask(this, new QueueRunnable()).repeat(2, TimeUnit.SECONDS).schedule();
        Logger.info("Uruchomiono!");
    }

    private void initMongoManager() {
        mongoManager = new MongoManager();
        Logger.info("Zainicjalizowano MongoManagera");

    }


    private void loadSectors() {
        Path configPath = getDataDirectory().resolve("config.json");
        if (!Files.exists(configPath)) {
            Logger.info("Brak config.json w katalogu pluginu! Trwa tworzenie domyślnego configu...");
            Path dataFolder = Paths.get("plugins", "EndSectors-Proxy");
            ConfigCreator.createDefaultConfig(dataFolder);
            return;
        }
        try {
            TypeReference<Map<String, Object>> typeRef = new TypeReference<>() {};
            Map<String, Object> root = mapper.readValue(Files.newBufferedReader(configPath), typeRef);
            Object sectorsObj = root.get("sectors");
            if (!(sectorsObj instanceof Map)) {
                Logger.info("Nie znaleziono sekcji 'sectors' w config.json!");
                return;
            }
            Map<String, Object> sectors = (Map<String, Object>) sectorsObj;
            for (Map.Entry<String, Object> typeEntry : sectors.entrySet()) {
                String typeName = typeEntry.getKey();
                Object typeDataObj = typeEntry.getValue();
                if (!(typeDataObj instanceof Map)) continue;

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

                // TU: log bez world
                loadedSectors.add(sectorName + " (" + sectorType + ")");

            } catch (Exception e) {
                Logger.info("Nie udało się zapisać sektora " + sectorName + ": " + e.getMessage());
            }
        }

        if (!loadedSectors.isEmpty()) {
            Logger.info("Załadowano sektory typu " + typeName + ": " + String.join(", ", loadedSectors));
        } else {
            Logger.info("Brak sektorów do załadowania dla typu " + typeName);
        }
    }




    private void initRedisManager() {
        this.redisManager = new RedisManager();
        this.redisManager.setPacketSender(PacketChannel.PROXY);

        Arrays.stream(new RedisPacketListener<?>[] {
                new PacketConfigurationRequestPacketListener(this.sectorManager),
                new PacketBroadcastMessagePacketListener(),
                new PacketSendMessageToPlayerPacketListener(),

                new PacketBroadcastTitlePacketListener(),
        }).forEach(this.redisManager::subscribe);


        Arrays.stream(new RedisPacketListener<?>[] {
                new TeleportToSectorListener(this.sectorManager,teleportationManager)
        }).forEach(listener -> this.redisManager.subscribe(PacketChannel.PROXY, listener));


        Arrays.stream(new RedisPacketListener<?>[] {
                new PacketSectorConnectedPacketListener(this.sectorManager),
                new PacketSectorDisconnectedPacketListener(this.sectorManager)
        }).forEach(listener -> this.redisManager.subscribe(PacketChannel.GLOBAL, listener));

        Logger.info("Zainicjalizowano RedisManagera");
    }

    private void initListeners() {
        server.getEventManager().register(this, new LastSectorConnectListener(this, teleportationManager));

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

