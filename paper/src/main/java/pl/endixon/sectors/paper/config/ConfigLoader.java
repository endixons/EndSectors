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

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import pl.endixon.sectors.paper.PaperSector;

public class ConfigLoader {

    public String currentSector = "spawn_1";

    public boolean ScoreboardEnabled = true;
    public Map<String, List<String>> scoreboard = new HashMap<>();
    public Map<String, String> sectorTitles = new HashMap<>();
    private static final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);


    public static ConfigLoader load(File dataFolder) {
        try {
            if (!dataFolder.exists() && !dataFolder.mkdirs()) {
                PaperSector.getInstance().getLogger().warning(
                        "Nie udało się utworzyć folderu configu: " + dataFolder.getAbsolutePath()
                );
            }

            File file = new File(dataFolder, "config.json");

            if (file.exists()) {
                try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
                    return mapper.readValue(reader, ConfigLoader.class);
                } catch (IOException e) {
                    PaperSector.getInstance().getLogger().warning(
                            "Błąd podczas wczytywania config.json, używam default: " + e.getMessage()
                    );
                    return defaultConfig();
                }
            } else {
                ConfigLoader defaultConfig = defaultConfig();
                try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {

                    DefaultPrettyPrinter printer = new DefaultPrettyPrinter();
                    printer.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);

                    mapper.writer(printer).writeValue(writer, defaultConfig);
                    PaperSector.getInstance().getLogger().info("Utworzono domyślny config.json ");
                } catch (IOException e) {
                    PaperSector.getInstance().getLogger().warning(
                            "Nie udało się zapisać domyślnego config.json: " + e.getMessage()
                    );
                }
                return defaultConfig;
            }

        } catch (Exception e) {
            PaperSector.getInstance().getLogger().warning(
                    "Nieoczekiwany błąd wczytywania configu: " + e.getMessage()
            );
            return defaultConfig();
        }
    }


    private static ConfigLoader defaultConfig() {
        ConfigLoader config = new ConfigLoader();
        config.currentSector = "spawn_1";
        config.ScoreboardEnabled = true;

        config.scoreboard.put("SPAWN", Arrays.asList(
                "&#55FF55📍 Sektor: {sectorName}",
                "&#FFD700👤 Nick: {playerName}",
                "                    ",
                "&#00FFFF⚡ TPS: {tps}",
                "&#FF5555🟢 Online: {onlineCount}",
                "                    ",
                "&#AAAAAAZnajdujesz się na kanale: {sectorName}",
                "&#AAAAAAAby zmienić kanał użyj /ch"
        ));

        config.scoreboard.put("NETHER", Arrays.asList(
                "&#FF5555📍 Sektor: {sectorName}",
                "&#FFD700👤 Nick: {playerName}",
                "                    ",
                "                    ",
                "&#00FFFF⚡ TPS: {tps}",
                "&#FF5555🟢 Online: {onlineCount}"
        ));

        config.scoreboard.put("END", Arrays.asList(
                "&#AA88FF📍 Sektor: {sectorName}",
                "&#FFD700👤 Nick: {playerName}",
                "                    ",
                "                    ",
                "&#00FFFF⚡ TPS: {tps}",
                "&#FF5555🟢 Online: {onlineCount}"
        ));

        config.scoreboard.put("ADMIN", Arrays.asList(
                "&#AA88FF📍 Sektor: {sectorName}",
                "&#FFD700👤 Nick: {playerName}",
                "                    ",
                "                    ",
                "&#00FFFF⚡ TPS: {tps}",
                "&#FF5555🟢 Online: {onlineCount}",
                "                    ",
                "&#00AAFF📶 Ping: {ping}ms",
                "&#FF00FF🖥 CPU: {cpu}%",
                "&#AA00FF💾 RAM: {freeRam}/{maxRam}MB"
        ));

        config.sectorTitles.put("SPAWN", "&#55FF55🏰 Spawn");
        config.sectorTitles.put("NETHER", "&#FF5555🔥 Nether");
        config.sectorTitles.put("END", "&#AA88FF🌌 End");
        config.sectorTitles.put("ADMIN", "&#AA88FF❓ Admin");

        config.sectorTitles.put("DEFAULT", "&#FFFFFF❓ {sectorType}");
        return config;
    }
}
