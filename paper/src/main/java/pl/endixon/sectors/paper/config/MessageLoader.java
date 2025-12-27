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
import lombok.Getter;
import lombok.Setter;
import pl.endixon.sectors.paper.PaperSector;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Getter
@Setter
public class MessageLoader {

    private Map<String, String> messages = new HashMap<>();
    private Map<String, List<String>> messagesLore = new HashMap<>();

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static MessageLoader load(File dataFolder) {
        try {
            if (!dataFolder.exists() && !dataFolder.mkdirs()) {
                PaperSector.getInstance().getLogger().warning("Failed to create configuration directory: " + dataFolder.getAbsolutePath());
            }

            File file = new File(dataFolder, "message.json");

            if (file.exists()) {
                try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
                    return gson.fromJson(reader, MessageLoader.class);
                } catch (Exception e) {
                    PaperSector.getInstance().getLogger().warning("Error while parsing message.json, rolling back to defaults: " + e.getMessage());
                    return defaultMessages(file);
                }
            } else {
                return defaultMessages(file);
            }

        } catch (Exception e) {
            PaperSector.getInstance().getLogger().severe("Unexpected critical error during message load: " + e.getMessage());
            return new MessageLoader();
        }
    }

    private static MessageLoader defaultMessages(File file) {
        MessageLoader config = new MessageLoader();
        Map<String, String> m = config.messages;
        Map<String, List<String>> l = config.messagesLore;

        // ===== BASIC MESSAGES =====
        m.put("SECTOR_CONNECTED_MESSAGE", "<#ff5555>Połączono się na sektor <#f5c542>{SECTOR}");
        m.put("SECTOR_ERROR_TITLE", "<#ff5555>Błąd");
        m.put("SECTOR_FULL_SUBTITLE", "<#ef4444>Sektor jest pełen graczy!");
        m.put("SECTOR_DISABLED_SUBTITLE", "<#ef4444>Ten sektor jest aktualnie wyłączony");

        m.put("BORDER_MESSAGE", "<#f5c542>Zbliżasz się do granicy sektora <#4ade80>{SECTOR} <#7dd3fc>{DISTANCE}m");
        m.put("BORDER_REFRESHED", "<#4ade80><b>BORDER</b> <#888888>» <#f2f2f2>Wysłano żądanie synchronizacji granic do Proxy.");
        m.put("BREAK_BORDER_DISTANCE_MESSAGE", "<#ef4444>Nie możesz niszczyć bloków przy granicy sektora!");
        m.put("PLACE_BORDER_DISTANCE_MESSAGE", "<#ef4444>Nie możesz stawiać bloków przy granicy sektora!");

        m.put("RELOAD_SUCCESS", "<#4ade80><b>SYSTEM</b> <#888888>» <#f2f2f2>Konfiguracja i wiadomości zostały przeładowane!");
        m.put("NO_PERMISSION", "<red>Brak uprawnień!");
        m.put("UNKNOWN_OPTION", "<red>Nieznana opcja. Użyj /sector, aby uzyskać pomoc.");

        m.put("TITLE_WAIT_TIME", "<#ef4444>Musisz odczekać {SECONDS}s przed ponowną zmianą sektora");
        m.put("PROTECTION_ACTIONBAR", "<#facc15>🛡 Ochrona przed obrażeniami: <#ffffff>{SECONDS}s");

        m.put("CURRENT_SECTOR", "<gray>Aktualny sektor: <gold>{SECTOR}");
        m.put("USAGE_EXECUTE", "<red>Błąd: <gold>/sector execute <komenda>");
        m.put("COMMAND_BROADCASTED", "<green>Komenda została wysłana do wszystkich sektorów.");
        m.put("SPECIFY_NICKNAME", "<red>Podaj nick: <gold>/sector {SUB}");

        m.put("PLAYER_ONLINE_STATUS", "<gray>Gracz <gold>{NICK} <gray>jest: {STATUS}");
        m.put("GLOBAL_ONLINE", "<gray>Globalnie Online (<gold>{SIZE}<gray>): <gold>{PLAYERS}");
        m.put("PLAYER_NOT_FOUND_DB", "<red>Gracz nie został znaleziony w bazie danych ani cache.");

        m.put("playerAlreadyConnectedMessage", "<#ef4444>Jesteś już połączony z tym kanałem");
        m.put("sectorIsOfflineMessage", "<#ef4444>Sektor jest wyłączony!");
        m.put("playerDataNotFoundMessage", "<#ef4444>Profil użytkownika nie został znaleziony!");
        m.put("spawnSectorNotFoundMessage", "<#ef4444>Nie odnaleziono dostępnego sektora spawn");
        m.put("SectorNotFoundMessage", "<#ef4444>Brak dostępnych sektorów");
        m.put("ONLY_IN_SPAWN_MESSAGE", "<#ef4444>Tej komendy możesz użyć tylko na sektorze SPAWN!");


        m.put("SHOW_GUI_TITLE", "<#ff7f11>Lista sektorów");
        m.put("SHOW_ITEM_NAME", "<#4ade80>Sektor <#facc15>{SECTOR}");
        m.put("SHOW_STATUS_ONLINE", "<#4ade80>Online");
        m.put("SHOW_STATUS_OFFLINE", "<#ef4444>Offline");


        m.put("CHANNEL_GUI_TITLE", "<#60a5fa>Lista kanałów");
        m.put("CHANNEL_ITEM_NAME", "<gray>Kanal <green>{SECTOR}");
        m.put("CHANNEL_OFFLINE", "<#ef4444>Kanał jest offline");
        m.put("CHANNEL_CURRENT", "<#facc15>Znajdujesz się na tym kanale");
        m.put("CHANNEL_CLICK_TO_CONNECT", "<#facc15>Kliknij, aby się połączyć");


        l.put("SHOW_LORE_FORMAT", List.of(
                "",
                "<#9ca3af>Status: {STATUS}",
                "<#9ca3af>TPS: {TPS}",
                "<#9ca3af>Online: <#7dd3fc>{COUNT}/{MAX}",
                "<#9ca3af>Zapełnienie: <#fbbf24>{PERCENT}%",
                "<#9ca3af>Ostatnia aktualizacja: <#a78bfa>{UPDATE}s"
        ));

        l.put("CHANNEL_LORE_FORMAT", List.of(
                "",
                "<#9ca3af>Online: <#4ade80>{ONLINE}",
                "<#9ca3af>TPS: {TPS}",
                "<#9ca3af>Ostatnia aktualizacja: <#4ade80>{UPDATE}s",
                "",
                "{STATUS}"
        ));

        l.put("INSPECT_FORMAT", List.of(
                "<#00FFFF>      INFORMACJE O GRACZU",
                " <white>Nick: <green>{NICK}",
                " <white>Sektor: <#00BFFF>{SECTOR}",
                " <white>Tryb gry: <#FF69B4>{GM}",
                " <white>Poziom: <#ADFF2F>{LVL}",
                " <white>Doświadczenie: <yellow>{EXP}",
                " <white>Ostatni transfer: <#FF8C00>{LAST}",
                " <white>Cooldown: <#FF4500>{COOLDOWN}",
                " "
        ));

        l.put("HELP_MENU", List.of(
                "<dark_gray>────────── <gold><b>POMOC SEKTORY</b> <dark_gray>──────────",
                "<gold>/sector reload <dark_gray>- <gray>Przeładowanie konfiguracji",
                "<gold>/sector border <dark_gray>- <gray>Synchronize sector borders\",",
                "<gold>/sector where <dark_gray>- <gray>Aktualny sektor",
                "<gold>/sector show <dark_gray>- <gray>Lista sektorów",
                "<gold>/sector execute <komenda> <dark_gray>- <gray>Globalna komenda",
                "<gold>/sector inspect <nick> <dark_gray>- <gray>Podgląd gracza",
                "<dark_gray>──────────────────────────────────"
        ));

        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            gson.toJson(config, writer);
            PaperSector.getInstance().getLogger().info("Default message.json generated.");
        } catch (IOException e) {
            PaperSector.getInstance().getLogger().warning("Failed to save message.json: " + e.getMessage());
        }

        return config;
    }
}