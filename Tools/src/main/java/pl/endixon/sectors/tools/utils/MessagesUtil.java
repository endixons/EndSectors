package pl.endixon.sectors.tools.utils;

import net.kyori.adventure.text.Component;

public enum MessagesUtil {


    CONSOLE_BLOCK("&#ef4444Ta komenda jest tylko dla gracza"),
    SPAWN_TITLE("&#ff5555Błąd"),
    PORTAL_COMBAT_TITLE("&#ff5555Błąd"),
    PORTAL_COMBAT_SUBTITLE("&#ef4444Nie możesz użyć portalu podczas walki"),
    SECTOR_COMBAT_TITLE("&#ff5555Błąd"),
    SECTOR_COMBAT_SUBTITLE("&#ef4444Nie możesz opuścić sektora podczas walki"),
    COMBAT_NO_COMMAND("&#ef4444Nie możesz używać komend w trakcie walki!"),
    SPAWN_OFFLINE("&#ef4444Ten sektor jest aktualnie wyłączony"),
    SPAWN_ALREADY("&#ef4444Już jesteś juz na spawnie"),
    RANDOM_TITLE("&#4ade80RandomTP"),
    RANDOM_START("&#9ca3afLosowanie sektora... &#4ade80proszę czekać"),
    PLAYERDATANOT_FOUND_MESSAGE("&#ef4444Profil użytkownika nie został znaleziony!"),
    RANDOM_SECTOR_NOTFOUND("&#FF5555Nie udało się znaleźć losowego sektora!");


    private final String text;

    MessagesUtil(String text) {
        this.text = text;
    }

    public Component get() {
        return ChatAdventureUtil.toComponent(text);
    }
}
