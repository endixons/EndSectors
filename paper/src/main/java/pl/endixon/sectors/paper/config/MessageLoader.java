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
        m.put("BORDER_REFRESHED", "<#38bdf8>» <#38bdf8>Granice sektorów zostały zsynchronizowane.");
        m.put("BREAK_BORDER_DISTANCE_MESSAGE", "<#ef4444>Nie możesz niszczyć bloków przy granicy sektora!");
        m.put("PLACE_BORDER_DISTANCE_MESSAGE", "<#ef4444>Nie możesz stawiać bloków przy granicy sektora!");

        m.put("RELOAD_SUCCESS", "<#38bdf8>» <#38bdf8>Konfiguracja została pomyślnie przeładowana.");
        m.put("NO_PERMISSION", "<red>Brak uprawnień!");
        m.put("UNKNOWN_OPTION", "<#38bdf8>» Nieznana opcja <#94a3b8>Sprawdź pomoc pod <#38bdf8>/sector");

        m.put("TITLE_WAIT_TIME", "<#ef4444>Musisz odczekać {SECONDS}s przed ponowną zmianą sektora");
        m.put("PROTECTION_ACTIONBAR", "<#facc15>🛡 Ochrona przed obrażeniami: <#ffffff>{SECONDS}s");

        m.put("CURRENT_SECTOR", "<#38bdf8>» <#94a3b8>Aktualny sektor: <#38bdf8>{SECTOR}");
        m.put("USAGE_EXECUTE", "<#38bdf8>» Poprawne użycie: <#38bdf8>/sector execute <komenda>");
        m.put("COMMAND_BROADCASTED", "<#38bdf8>» <#38bdf8>Komenda została wysłana do wszystkich sektorów");
        m.put("SPECIFY_NICKNAME", "<#38bdf8>» Poprawne użycie: <#38bdf8>/sector {SUB}");

        m.put("PLAYER_ONLINE_STATUS", "<#38bdf8>» <#94a3b8>Gracz <#38bdf8>{NICK} <#94a3b8>jest obecnie: <#38bdf8>{STATUS}");
        m.put("GLOBAL_ONLINE", "<#38bdf8>» <#94a3b8>Online <#38bdf8>({SIZE})<#94a3b8>: <#38bdf8>{PLAYERS}");
        m.put("PLAYER_NOT_FOUND_DB", "<red>Gracz nie został znaleziony w bazie danych");

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
                " ",
                "  <#38bdf8><b>INFORMACJE O GRACZU</b>",
                "  <#94a3b8>Nick: <#38bdf8>{NICK}",
                "  <#94a3b8>Sektor: <#38bdf8>{SECTOR}",
                "  <#94a3b8>Poziom: <#38bdf8>{LVL} <#94a3b8>({EXP} XP)",
                "  <#94a3b8>Ostatni transfer: <#38bdf8>{LAST}",
                "  <#94a3b8>Cooldown: <#38bdf8>{COOLDOWN}s",
                " "
        ));

        l.put("HELP_MENU", List.of(
                " ",
                "  <#38bdf8><b>POMOC</b>",
                "  <#38bdf8>/sector reload <#94a3b8>» Przeładowuje konfigurację",
                "  <#38bdf8>/sector border <#94a3b8>» Synchronizuje granice sektorów",
                "  <#38bdf8>/sector where <#94a3b8>» Sprawdza aktualny sektor",
                "  <#38bdf8>/sector show <#94a3b8>» Wyświetla listę sektorów",
                "  <#38bdf8>/sector who <#94a3b8>» Wyświetla listę graczy online globalnie",
                "  <#38bdf8>/sector execute <#94a3b8>» Wykonuje komendę na wszystkich sektorach",
                "  <#38bdf8>/sector inspect <#94a3b8>» Wyświetla szczegółowe informacje o graczu",
                " "
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