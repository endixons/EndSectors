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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import pl.endixon.sectors.paper.PaperSector;

public class ConfigLoader {

    public String currentSector = "spawn01";
    public boolean ScoreboardEnabled = true;
    public Map<String, List<String>> scoreboard = new HashMap<>();
    public String adminTitlePrefix = "🛡 ";
    public String adminTitleSuffix = " 🛡";
    public String playerTitlePrefix = "✨ ";
    public String playerTitleSuffix = " ✨";

    public Map<String, String> sectorTitles = new HashMap<>();

    private static final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    public static ConfigLoader load(File dataFolder) {
        try {
            if (!dataFolder.exists() && !dataFolder.mkdirs()) {
                PaperSector.getInstance().getLogger().warning("Nie udało się utworzyć folderu configu: " + dataFolder.getAbsolutePath());
            }

            File file = new File(dataFolder, "config.json");

            if (file.exists()) {
                try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
                    return mapper.readValue(reader, ConfigLoader.class);
                } catch (IOException e) {
                    PaperSector.getInstance().getLogger().warning("Błąd podczas wczytywania config.json, używam default: " + e.getMessage());
                    return defaultConfig();
                }
            } else {
                ConfigLoader defaultConfig = defaultConfig();
                try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
                    mapper.writeValue(writer, defaultConfig);
                    PaperSector.getInstance().getLogger().info("Utworzono domyślny config.json");
                } catch (IOException e) {
                    PaperSector.getInstance().getLogger().warning("Nie udało się zapisać domyślnego config.json: " + e.getMessage());
                }
                return defaultConfig;
            }

        } catch (Exception e) {
            PaperSector.getInstance().getLogger().warning("Nieoczekiwany błąd wczytywania configu: " + e.getMessage());
            return defaultConfig();
        }
    }

    private static ConfigLoader defaultConfig() {
        ConfigLoader config = new ConfigLoader();
        config.currentSector = "spawn01";
        config.ScoreboardEnabled = true;
        config.scoreboard.put("SPAWN", Arrays.asList("§a📍 Sektor: {sectorName}", "§e👤 Nick: {playerName}", "                    ", "§b⚡ TPS: {tps}", "§c🟢 Online: {onlineCount}", "                    ", "§7Znajdujesz się na kanale: {sectorName}", "§7Aby zmienić kanał użyj /ch"));

        config.scoreboard.put("NETHER", Arrays.asList("§a📍 Sektor: {sectorName}", "§e👤 Nick: {playerName}", "                    ", "                    ", "§b⚡ TPS: {tps}", "§c🟢 Online: {onlineCount}"));

        config.scoreboard.put("END", Arrays.asList("§a📍 Sektor: {sectorName}", "§e👤 Nick: {playerName}", "                    ", "                    ", "§b⚡ TPS: {tps}", "§c🟢 Online: {onlineCount}"));

        config.scoreboard.put("ADMIN", Arrays.asList("                    ", "§b📶 Ping: {ping}ms", "§d🖥 CPU: {cpu}%", "§5💾 RAM: {freeRam}/{maxRam}MB"));

        config.sectorTitles.put("SPAWN", "🏰 Spawn");
        config.sectorTitles.put("NETHER", "🔥 Nether");
        config.sectorTitles.put("END", "🌌 End");
        config.sectorTitles.put("DEFAULT", "❓ {sectorType}");

        return config;
    }
}
