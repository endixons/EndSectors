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

package pl.endixon.sectors.proxy.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.Setter;
import pl.endixon.sectors.common.util.LoggerUtil;
import pl.endixon.sectors.proxy.VelocitySectorPlugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Getter
@Setter
public class MessageLoader {

    private Map<String, String> messages = new HashMap<>();
    private Map<String, List<String>> motd = new HashMap<>();

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static MessageLoader load(VelocitySectorPlugin plugin) {
        File dataFolder = plugin.getDataDirectory().toFile();

        try {
            if (!dataFolder.exists() && !dataFolder.mkdirs()) {
                LoggerUtil.warn("Failed to create configuration directory: " + dataFolder.getAbsolutePath());
            }

            File file = new File(dataFolder, "message.json");

            if (file.exists()) {
                try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
                    MessageLoader loaded = gson.fromJson(reader, MessageLoader.class);
                    return (loaded != null) ? loaded : defaultMessages(file);
                } catch (Exception e) {
                    LoggerUtil.warn("Error while parsing message.json, rolling back to defaults: " + e.getMessage());
                    return defaultMessages(file);
                }
            } else {
                return defaultMessages(file);
            }

        } catch (Exception e) {
            LoggerUtil.error("Unexpected critical error during message load: " + e.getMessage());
            return new MessageLoader();
        }
    }

    private static MessageLoader defaultMessages(File file) {
        MessageLoader config = new MessageLoader();
        Map<String, String> m = config.messages;
        Map<String, List<String>> motd = config.motd;

        m.put("QUEUE_TITLE", "<gradient:#00d2ff:#3a7bd5><bold>KOLEJKA</bold></gradient>");
        m.put("QUEUE_OFFLINE", "<gradient:#ff4b2b:#ff416c>Sektor <white>{sector}</white> jest obecnie <bold>OFFLINE</bold></gradient> <gray>({pos}/{total})</gray>");
        m.put("QUEUE_FULL", "<gradient:#f8ff00:#f8ff00>Sektor <white>{sector}</white> jest <bold>PELNY</bold></gradient> <gray>({pos}/{total})</gray>");
        m.put("QUEUE_POSITION", "<gradient:#e0e0e0:#ffffff>Twoja pozycja: </gradient><gradient:#00d2ff:#3a7bd5><bold>{pos}</bold></gradient><white><bold> / </bold></white><gradient:#3a7bd5:#00d2ff>{total}</gradient>");

        m.put("DISCONNECT_MESSAGE",
                "<red>Połączenie z infrastrukturą zostało przerwane.\n" +
                        "<gray>Trwa próba przywrócenia usług...");

        m.put("EMERGENCY_KICK",
                "<bold><gradient:#ff4b2b:#ff416c>ENDSECTORS</gradient></bold><br><br>" +
                        "<gray>Obecnie trwają <gradient:#ffe259:#ffa751>PRACE KONSERWACYJNE</gradient>.<br>" +
                        "<gray>Zapraszamy ponownie za kilka minut!<br><br>" +
                        "<dark_gray>Status: <red>Tryb Optymalizacji"
        );

        motd.put("PROXY_MOTD", Arrays.asList(
                "<bold><gradient:#2afcff:#00bfff>ENDSECTORS</gradient></bold> <gray>•</gray> <gradient:#ffe259:#ffa751>FRAMEWORK</gradient>",
                "<gradient:#fffa65:#f79c4c>Support Discord: https://dsc.gg/endsectors</gradient>"
        ));

        motd.put("EMERGENCY_MOTD", Arrays.asList(
                "<bold><gradient:#ff4b2b:#ff416c>ENDSECTORS</gradient></bold> <gray>•</gray> <gradient:#ffe259:#ffa751>PRACE KONSERWACYJNE</gradient>",
                "<gradient:#fffa65:#f79c4c>Discord Support: https://dsc.gg/endsectors</gradient>"
        ));

        motd.put("EMERGENCY_HOVER", Arrays.asList(
                "§6§lDODATKOWE INFORMACJE",
                "§7Aktualnie przeprowadzamy §eplanowane §7prace",
                "§7nad wydajnością naszych systemów.",
                "",
                "§fPrzewidywany czas powrotu: §aKilka minut",
                "§eDziękujemy za cierpliwość!",
                "§6§lDiscord Support: §fhttps://dsc.gg/endsectors"
        ));

        motd.put("PROXY_HOVER", Arrays.asList(
                "§b§lENDSECTORS FRAMEWORK",
                "§7Status systemu: §aONLINE",
                "§7Support Discord: §6https://dsc.gg/endsectors",
                "",
                "§7Aktywne sektory: §a{ACTIVE_SECTORS}",
                "§7Gracze online: §a{ONLINE_PLAYERS}",
                "§7Obciążenie CPU: {CPU}",
                ""
        ));

        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            gson.toJson(config, writer);
            LoggerUtil.info("Default message.json has been generated successfully.");
        } catch (IOException e) {
            LoggerUtil.info("Failed to save default message.json: " + e.getMessage());
        }

        return config;
    }
    }