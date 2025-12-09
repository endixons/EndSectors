package pl.endixon.sectors.proxy.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class ConfigCreator {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static void createDefaultConfig(Path dataFolder) {
        try {
            if (!Files.exists(dataFolder)) {
                Files.createDirectories(dataFolder);
            }

            Path configPath = dataFolder.resolve("config.json");
            if (Files.exists(configPath)) {
                System.out.println("config.json już istnieje, pomijam tworzenie.");
                return;
            }

            Map<String, Object> root = new LinkedHashMap<>();
            Map<String, Object> sectors = new LinkedHashMap<>();


            Map<String, Object> spawn = new LinkedHashMap<>();
            spawn.put("spawn_1", createSectorMap(-100, -100, 100, 100, "SPAWN", "world"));
            spawn.put("spawn_2", createSectorMap(-100, -100, 100, 100, "SPAWN", "world"));
            spawn.put("spawn_3", createSectorMap(-100, -100, 100, 100, "SPAWN", "world"));
            sectors.put("SPAWN", spawn);


            Map<String, Object> queue = new LinkedHashMap<>();
            queue.put("queue", createSectorMap(-100, -100, 100, 100, "QUEUE", "world"));
            sectors.put("QUEUE", queue);


            Map<String, Object> sector = new LinkedHashMap<>();
            sector.put("s1", createSectorMap(-100, 100, 1000, 1000, "SECTOR", "world"));
            sector.put("w1", createSectorMap(-1000, -100, -100, 1000, "SECTOR", "world"));
            sector.put("e1", createSectorMap(100, -1000, 1000, 100, "SECTOR", "world"));
            sector.put("n1", createSectorMap(-1000, -1000, 100, -100, "SECTOR", "world"));
            sectors.put("SECTOR", sector);


            Map<String, Object> nether = new LinkedHashMap<>();
            nether.put("nether01", createSectorMap(-100, -100, 100, 100, "NETHER", "world_nether"));
            nether.put("nether02", createSectorMap(-100, -100, 100, 100, "NETHER", "world_nether"));
            sectors.put("NETHER", nether);


            Map<String, Object> end = new LinkedHashMap<>();
            end.put("end01", createSectorMap(-100, -100, 100, 100, "END", "world_end"));
            end.put("end02", createSectorMap(-100, -100, 100, 100, "END", "world_end"));
            sectors.put("END", end);

            root.put("sectors", sectors);

            mapper.writerWithDefaultPrettyPrinter().writeValue(configPath.toFile(), root);
            System.out.println("config.json został utworzony!");

        } catch (IOException e) {
            e.printStackTrace();
        }
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
