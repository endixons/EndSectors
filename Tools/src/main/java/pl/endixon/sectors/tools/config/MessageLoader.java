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

package pl.endixon.sectors.tools.config;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.Setter;
import pl.endixon.sectors.tools.utils.LoggerUtil;

@Getter
@Setter
public class MessageLoader {

    public Map<String, String> messages = new HashMap<>();
    public Map<String, List<String>> messagesLore = new HashMap<>();

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static MessageLoader load(File dataFolder) {
        try {
            if (!dataFolder.exists() && !dataFolder.mkdirs()) {
                LoggerUtil.info("Failed to create configuration directory: " + dataFolder.getAbsolutePath());
            }

            File file = new File(dataFolder, "messages.json");

            if (file.exists()) {
                try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
                    return gson.fromJson(reader, MessageLoader.class);
                } catch (IOException e) {
                    LoggerUtil.info("Error while parsing messages.json, rolling back to defaults: " + e.getMessage());
                    return createDefault();
                }
            } else {
                MessageLoader defaultMessages = createDefault();
                try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
                    gson.toJson(defaultMessages, writer);
                    LoggerUtil.info("Default messages.json has been generated successfully.");
                } catch (IOException e) {
                    LoggerUtil.info("Failed to save default messages.json: " + e.getMessage());
                }
                return defaultMessages;
            }

        } catch (Exception e) {
            LoggerUtil.info("Critical error during messages configuration load: " + e.getMessage());
            return createDefault();
        }
    }

    private static MessageLoader createDefault() {
        MessageLoader config = new MessageLoader();
        Map<String, String> m = config.messages;
        Map<String, List<String>> l = config.messagesLore;

        m.put("CONSOLE_BLOCK", "<#ef4444>Ta komenda jest tylko dla gracza");

        m.put("PORTAL_COMBAT_TITLE", "<#ff5555>Błąd");
        m.put("PORTAL_COMBAT_SUBTITLE", "<#ef4444>Nie możesz użyć portalu podczas walki");
        m.put("SECTOR_COMBAT_TITLE", "<#ff5555>Błąd");
        m.put("SECTOR_COMBAT_SUBTITLE", "<#ef4444>Nie możesz opuścić sektora podczas walki");
        m.put("COMBAT_NO_COMMAND", "<#ef4444>Nie możesz używać komend w trakcie walki!");

        m.put("SPAWN_TITLE", "<#ff5555>Błąd");
        m.put("SPAWN_OFFLINE", "<#ef4444>Ten sektor jest aktualnie wyłączony");
        m.put("SPAWN_ALREADY", "<#ef4444>Już jesteś juz na spawnie");

        m.put("AFK_TITLE", "<#ff5555>Błąd");
        m.put("AFK_OFFLINE", "<#ef4444>Ten sektor jest aktualnie wyłączony");
        m.put("AFK_ALREADY", "<#ef4444>Już jesteś juz na AFK");


        m.put("PLAYERDATANOT_FOUND_MESSAGE", "<#ef4444>Profil użytkownika nie został znaleziony!");

        m.put("RANDOM_TITLE", "<#4ade80>RandomTP");
        m.put("RANDOM_START", "<#9ca3af>Losowanie sektora... <#4ade80>proszę czekać");
        m.put("RANDOM_SECTOR_NOTFOUND", "<#ff5555>Nie udało się znaleźć losowego sektora!");
        m.put("RANDOM_SECTORSPAWN_NOTFOUND", "<#ef4444>Nie odnaleziono dostepnego sektora spawn");
        m.put("RANDOM_SECTORAFK_NOTFOUND", "<#ef4444>Nie odnaleziono dostepnego sektora afk");
        m.put("TELEPORT_SUCCESS", "<#00ffaa>Pomyślnie przeteleportowano!");
        m.put("TELEPORT_COUNTDOWN_TITLE", "<#FFD700>Teleportacja za...");
        m.put("TELEPORT_COUNTDOWN_SUBTITLE", "<#FFA500>{TIME} <#FFD700>sekund");
        m.put("TELEPORT_CANCELLED_TITLE", "<#FF5555>Teleportacja przerwana!");
        m.put("TELEPORT_CANCELLED_SUBTITLE", "<#FF4444>Wykryto ruch gracza");

        m.put("HOME_GUI_TITLE", "<gray>Twoje Domki");
        m.put("HOME_NAME_FORMAT", "<#4ade80>{NAME}");
        m.put("HOME_CREATED_SUCCESS", "<#00ffaa>Pomyślnie utworzono Domek");
        m.put("HOME_DELETED_SUCCESS", "<#ff5555>Usunięto Domek");
        m.put("HOME_CANT_CREATE_SPAWN", "<#ff5555>Nie możesz tworzyć domków na sektorze SPAWN!");
        m.put("HOME_SECTOR_NOT_FOUND", "<#ff5555>Nie udało się znaleźć sektora dla twojego domku!");
        m.put("HOME_WORLD_NOT_LOADED", "<#ff5555>Świat dla tego sektora nie jest załadowany!");

        m.put("BACKPACK_ADMIN_NO_PERMISSION", "<#ff4b2b>Brak uprawnień administracyjnych.");
        m.put("BACKPACK_ADMIN_USAGE", "<gradient:#ed213a:#93291e><bold>ADMIN:</bold></gradient> <#aaaaaa>Poprawne użycie: <#ff5f6d>/bpadmin <nick/uuid>");
        m.put("BACKPACK_ADMIN_PLAYER_NOT_FOUND", "<gradient:#ed213a:#93291e><bold>ADMIN:</bold></gradient> <#ff4b2b>Nie znaleziono profilu gracza: <#ff5f6d>{PLAYER}");
        m.put("BACKPACK_ADMIN_OPENING_FOR_PLAYER", "<gradient:#ed213a:#93291e><bold>ADMIN:</bold></gradient> <#aaaaaa>Otwierasz plecak gracza: <#ff5f6d>{PLAYER}");


        l.put("HOME_SET_LORE", Arrays.asList(
                "",
                "",
                "<#9ca3af>Kliknij <#4ade80>lewy przycisk<#9ca3af>, aby",
                "<#9ca3af>przeteleportować się do tego domku.",
                "",
                "<#9ca3af>Kliknij <#4ade80>prawy przycisk<#9ca3af>, aby",
                "<#9ca3af>usunąć ten domek.",
                "",
                "<white>Twój domek znajduje się w sektorze <#facc15>{SECTOR}",
                "<white>Pozycja: X:{X} Y:{Y} Z:{Z}",
                ""
        ));

        l.put("HOME_EMPTY_LORE", Arrays.asList(
                "",
                "",
                "<#9ca3af>Kliknij <#4ade80>lewy lub prawy przyciskiem<#9ca3af>, aby",
                "<#9ca3af>zapisać aktualną pozycję jako nowy domek.",
                "",
                "<white>Twój domek zostanie automatycznie zapisany",
                "<white>na tej pozycji w sektorze <#facc15>{SECTOR}",
                ""
        ));

        return config;
    }
}