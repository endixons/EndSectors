package pl.endixon.sectors.proxy.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import pl.endixon.sectors.common.sector.SectorData;
import pl.endixon.sectors.common.sector.SectorType;
import pl.endixon.sectors.common.util.Corner;
import pl.endixon.sectors.proxy.VelocitySectorPlugin;
import pl.endixon.sectors.proxy.manager.SectorManager;
import pl.endixon.sectors.proxy.util.LoggerUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ConfigCreator {

    private static final ObjectMapper mapper = new ObjectMapper();

    private ConfigCreator() { }

    public static ConfigCreator load(VelocitySectorPlugin plugin) {
        Path dataFolder = plugin.getDataDirectory();
        Path configPath = dataFolder.resolve("config.json");

        try {
            if (!Files.exists(dataFolder)) Files.createDirectories(dataFolder);

            Map<String, Object> root;
            if (Files.exists(configPath)) {
                root = mapper.readValue(Files.newBufferedReader(configPath), Map.class);
            } else {
                root = createDefaultData();
                mapper.writerWithDefaultPrettyPrinter().writeValue(configPath.toFile(), root);
                LoggerUtil.info("Default config.json został utworzony!");
            }

            Map<String, Map<String, Object>> sectors = (Map<String, Map<String, Object>>) root.get("sectors");

            for (Map.Entry<String, Map<String, Object>> typeEntry : sectors.entrySet()) {
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

                        plugin.getSectorManager().addSectorData(new SectorData(sectorName, corner1, corner2, world, sectorType));
                        loadedSectors.add(sectorName);

                    } catch (Exception e) {
                        LoggerUtil.error("Nie udało się załadować sektora '" + sectorName + "': " + e.getMessage());
                    }
                }

                if (!loadedSectors.isEmpty()) {
                    LoggerUtil.info("Loaded sectors of type " + typeName + ": " + String.join(", ", loadedSectors));
                } else {
                    LoggerUtil.error("No sectors to load for type " + typeName + ".");
                }
            }

            return new ConfigCreator();

        } catch (IOException e) {
            e.printStackTrace();
            return new ConfigCreator();
        }
    }

    private static Map<String, Object> createDefaultData() {
        Map<String, Object> root = new LinkedHashMap<>();
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
}
