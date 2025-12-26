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

package pl.endixon.sectors.paper.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import pl.endixon.sectors.paper.PaperSector;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigLoader {

    public String currentSector = "spawn_1";
    public boolean scoreboardEnabled = true;

    public String redisHost = "127.0.0.1";
    public int redisPort = 6379;
    public String redisPassword = "";

    public String natsUrl = "nats://127.0.0.1:4222";
    public String natsConnectionName = "spawn_1";

    public int borderMessageDistance = 15;
    public int breakBorderDistance = 15;
    public int placeBorderDistance = 15;
    public int explosionBorderDistance = 15;
    public int bucketBorderDistance = 15;
    public int dropItemBorderDistance = 15;

    public long protectionAfterTransferMillis = 5000L;
    public long transferDelayMillis = 5000L;
    public double knockBorderForce = 1.35;
    public int protectionSeconds = 5;

    public Map<String, List<String>> scoreboard = new HashMap<>();
    public Map<String, String> sectorTitles = new HashMap<>();

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static ConfigLoader load(File dataFolder) {
        try {
            if (!dataFolder.exists() && !dataFolder.mkdirs()) {
                PaperSector.getInstance().getLogger().warning("Failed to create configuration directory: " + dataFolder.getAbsolutePath());
            }

            File file = new File(dataFolder, "config.json");

            if (file.exists()) {
                try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
                    return gson.fromJson(reader, ConfigLoader.class);
                } catch (Exception e) {
                    PaperSector.getInstance().getLogger().warning("Error while parsing config.json, rolling back to defaults: " + e.getMessage());
                    return defaultConfig();
                }
            } else {
                ConfigLoader defaultConfig = defaultConfig();
                try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
                    gson.toJson(defaultConfig, writer);
                    PaperSector.getInstance().getLogger().info("Default config.json has been generated successfully.");
                } catch (IOException e) {
                    PaperSector.getInstance().getLogger().warning("Failed to save default config.json: " + e.getMessage());
                }
                return defaultConfig;
            }
        } catch (Exception e) {
            PaperSector.getInstance().getLogger().severe("Unexpected critical error during configuration load: " + e.getMessage());
            return defaultConfig();
        }
    }

    private static ConfigLoader defaultConfig() {
        ConfigLoader config = new ConfigLoader();

        config.currentSector = "spawn_1";
        config.scoreboardEnabled = true;

        config.redisHost = "127.0.0.1";
        config.redisPort = 6379;
        config.redisPassword = "";

        config.natsUrl = "nats://127.0.0.1:4222";
        config.natsConnectionName = "spawn_1";

        config.borderMessageDistance = 15;
        config.breakBorderDistance = 15;
        config.placeBorderDistance = 15;
        config.explosionBorderDistance = 15;
        config.bucketBorderDistance = 15;
        config.dropItemBorderDistance = 15;

        config.protectionAfterTransferMillis = 5000L;
        config.transferDelayMillis = 5000L;
        config.knockBorderForce = 1.35;
        config.protectionSeconds = 5;

        config.scoreboard.put("SPAWN", List.of(
                "                    ",
                "<#55FF55>📍 Sektor: <white>{sectorName}",
                "<#FFD700>👤 Nick: <white>{playerName}",
                "                    ",
                "<#00FFFF>⚡ TPS: {tps}",
                "<#FF5555>🟢 Online: <white>{onlineCount}",
                "                    ",
                "<#AAAAAA>Znajdujesz się na kanale: <white>{sectorName}",
                "<#AAAAAA>Aby zmienić kanał użyj <#55FF55>/ch",
                "                    "
        ));

        config.scoreboard.put("NETHER", List.of(
                "                    ",
                "<#FF5555>📍 Sektor: <white>{sectorName}",
                "<#FFD700>👤 Nick: <white>{playerName}",
                "                    ",
                "                    ",
                "<#00FFFF>⚡ TPS: {tps}",
                "<#FF5555>🟢 Online: <white>{onlineCount}",
                "                    "
        ));

        config.scoreboard.put("END", List.of(
                "<#AA88FF>📍 Sektor: <white>{sectorName}",
                "<#FFD700>👤 Nick: <white>{playerName}",
                "                    ",
                "                    ",
                "<#00FFFF>⚡ TPS: {tps}",
                "<#FF5555>🟢 Online: <white>{onlineCount}",
                "                    "
        ));

        config.scoreboard.put("ADMIN", List.of(
                "                    ",
                "<#AA88FF>📍 Sektor: <white>{sectorName}",
                "<#FFD700>👤 Nick: <white>{playerName}",
                "                    ",
                "<#00FFFF>⚡ TPS: {tps}",
                "<#FF5555>🟢 Online: <white>{onlineCount}",
                "                    ",
                "<#00AAFF>📶 Ping: <white>{ping}ms",
                "<#FF00FF>🖥 CPU: <white>{cpu}%",
                "<#AA00FF>💾 RAM: <white>{freeRam}/{maxRam}MB",
                "                    "
        ));

        config.sectorTitles.put("SPAWN", "<#55FF55>🏰 Spawn");
        config.sectorTitles.put("NETHER", "<#FF5555>🔥 Nether");
        config.sectorTitles.put("END", "<#AA88FF>🌌 End");
        config.sectorTitles.put("ADMIN", "<#AA88FF>❓ Admin");
        config.sectorTitles.put("DEFAULT", "<#FFFFFF>❓ {sectorType}");

        return config;
    }
}
