package pl.endixon.sectors.proxy.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import pl.endixon.sectors.common.sector.SectorData;
import pl.endixon.sectors.common.sector.SectorType;
import pl.endixon.sectors.common.util.Corner;
import pl.endixon.sectors.proxy.VelocitySectorPlugin;
import pl.endixon.sectors.proxy.util.LoggerUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ConfigLoader {

    public String redisHost = "127.0.0.1";
    public int redisPort = 6379;
    public String redisPassword = "password";
    public String natsUrl = "nats://user:password@127.0.0.1:4222";
    public String natsConnectionName = "proxy";
    public Map<String, Map<String, SectorData>> sectors = new LinkedHashMap<>();
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private ConfigLoader() { }

    @SuppressWarnings("unchecked")
    public static ConfigLoader load(VelocitySectorPlugin plugin) {
        Path dataFolder = plugin.getDataDirectory();
        Path configPath = dataFolder.resolve("config.json");

        try {
            if (!Files.exists(dataFolder)) Files.createDirectories(dataFolder);

            Map<String, Object> root;
            if (Files.exists(configPath)) {
                try (Reader reader = new InputStreamReader(new FileInputStream(configPath.toFile()), StandardCharsets.UTF_8)) {
                    root = gson.fromJson(reader, Map.class);
                } catch (Exception e) {
                    LoggerUtil.error("Error while parsing config.json, rolling back to defaults: " + e.getMessage());
                    root = createDefaultData();
                    saveJson(configPath, root);
                }
            } else {
                root = createDefaultData();
                saveJson(configPath, root);
            }

            ConfigLoader config = new ConfigLoader();
            config.redisHost = (String) root.getOrDefault("redisHost", "127.0.0.1");
            config.redisPort = ((Number) root.getOrDefault("redisPort", 6379)).intValue();
            config.redisPassword = (String) root.getOrDefault("redisPassword", "password");
            config.natsUrl = (String) root.getOrDefault("natsUrl", "nats://user:password@127.0.0.1:4222");
            config.natsConnectionName = (String) root.getOrDefault("natsConnectionName", "proxy");

            Map<String, Map<String, Object>> sectorsMap = (Map<String, Map<String, Object>>) root.get("sectors");
            if (sectorsMap != null) {
                for (Map.Entry<String, Map<String, Object>> typeEntry : sectorsMap.entrySet()) {
                    String typeName = typeEntry.getKey();
                    Map<String, Object> typeDataMap = typeEntry.getValue();
                    List<String> loadedSectors = new ArrayList<>();

                    for (Map.Entry<String, Object> sectorEntry : typeDataMap.entrySet()) {
                        String sectorName = sectorEntry.getKey();
                        Object value = sectorEntry.getValue();
                        if (!(value instanceof Map<?, ?> sectorMap)) continue;

                        try {
                            int pos1X = ((Number) sectorMap.get("pos1X")).intValue();
                            int pos1Z = ((Number) sectorMap.get("pos1Z")).intValue();
                            int pos2X = ((Number) sectorMap.get("pos2X")).intValue();
                            int pos2Z = ((Number) sectorMap.get("pos2Z")).intValue();
                            String world = (String) sectorMap.get("world");
                            String typeStr = (String) sectorMap.get("type");

                            SectorType sectorType = SectorType.valueOf(typeStr.toUpperCase());
                            Corner corner1 = new Corner(pos1X, pos1Z);
                            Corner corner2 = new Corner(pos2X, pos2Z);

                            SectorData data = new SectorData(sectorName, corner1, corner2, world, sectorType);
                            if (validateSectorData(data)) {
                            plugin.getSectorManager().addSectorData(data);
                            loadedSectors.add(sectorName);

                            config.sectors.computeIfAbsent(typeName, k -> new LinkedHashMap<>()).put(sectorName, data);
                            } else {
                                LoggerUtil.error("Invalid sector data for '" + sectorName + "', skipping...");
                            }
                        } catch (Exception e) {
                            LoggerUtil.error("Failed to load sector '" + sectorName + "': " + e.getMessage());
                        }
                    }

                    if (!loadedSectors.isEmpty()) {
                        LoggerUtil.info("Loaded sectors of type " + typeName + ": " + String.join(", ", loadedSectors));
                    } else {
                        LoggerUtil.error("No sectors to load for type " + typeName + ".");
                    }
                }
            }

            return config;

        } catch (IOException e) {
            LoggerUtil.error("Critical error while loading config: " + e.getMessage());
            return defaultConfig();
        }
    }

    private static void saveJson(Path path, Object data) {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(path.toFile()), StandardCharsets.UTF_8)) {
            gson.toJson(data, writer);
            LoggerUtil.info("Default config.json has been generated successfully!");
        } catch (IOException e) {
            LoggerUtil.error("Failed to save default config.json: " + e.getMessage());
        }
    }

    private static Map<String, Object> createDefaultData() {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("redisHost", "127.0.0.1");
        root.put("redisPort", 6379);
        root.put("redisPassword", "password");
        root.put("natsUrl", "nats://user:password@127.0.0.1:4222");
        root.put("natsConnectionName", "proxy");

        Map<String, Object> sectors = new LinkedHashMap<>();

        Map<String, Object> spawn = new LinkedHashMap<>();
        spawn.put("spawn_1", createSectorMap(-200, -200, 200, 200, "SPAWN", "world"));
        spawn.put("spawn_2", createSectorMap(-200, -200, 200, 200, "SPAWN", "world"));
        sectors.put("SPAWN", spawn);


        Map<String, Object> queue = new LinkedHashMap<>();
        queue.put("queue", createSectorMap(-200, -200, 200, 200, "QUEUE", "world"));
        sectors.put("QUEUE", queue);


        Map<String, Object> sector = new LinkedHashMap<>();
        sector.put("s1", createSectorMap(-200, 200, 5000, 5000, "SECTOR", "world"));
        sector.put("w1", createSectorMap(-5000, -200, -200, 5000, "SECTOR", "world"));
        sector.put("e1", createSectorMap(200, -5000, 5000, 200, "SECTOR", "world"));
        sector.put("n1", createSectorMap(-5000, -5000, 200, -200, "SECTOR", "world"));
        sectors.put("SECTOR", sector);


        Map<String, Object> nether = new LinkedHashMap<>();
        nether.put("nether01", createSectorMap(-200, -200, 200, 200, "NETHER", "world_nether"));
        nether.put("nether02", createSectorMap(-200, -200, 200, 200, "NETHER", "world_nether"));
        sectors.put("NETHER", nether);

        Map<String, Object> end = new LinkedHashMap<>();
        end.put("end01", createSectorMap(-200, -200, 200, 200, "END", "world_end"));
        end.put("end02", createSectorMap(-200, -200, 200, 200, "END", "world_end"));
        sectors.put("END", end);

        root.put("sectors", sectors);
        return root;
    }

    private static Map<String, Object> createSectorMap(int pos1X, int pos1Z, int pos2X, int pos2Z, String type, String world) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("pos1X", pos1X);
        map.put("pos1Z", pos1Z);
        map.put("pos2X", pos2X);
        map.put("pos2Z", pos2Z);
        map.put("type", type);
        map.put("world", world);
        return map;
    }

    private static ConfigLoader defaultConfig() {
        ConfigLoader config = new ConfigLoader();
        config.redisHost = "127.0.0.1";
        config.redisPort = 6379;
        config.redisPassword = "password";
        config.natsUrl = "nats://user:password@127.0.0.1:4222";
        config.natsConnectionName = "proxy";
        return config;
    }

    private static boolean validateSectorData(SectorData data) {
        return data != null
                && data.getName() != null && !data.getName().isEmpty()
                && data.getFirstCorner() != null && data.getSecondCorner() != null
                && data.getWorld() != null && !data.getWorld().isEmpty()
                && data.getType() != null;
    }


}
