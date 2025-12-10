package pl.endixon.sectors.tools.utils;

import pl.endixon.sectors.common.util.ChatUtil;

public enum Messages {

    CONSOLE_BLOCK("&cTa komenda jest tylko dla gracza"),
    SPAWN_TITLE("&6Spawn"),
    SPAWN_OFFLINE("&cSpawn aktualnie offline"),
    SPAWN_START("&7Teleport za &f10s"),
    SPAWN_COUNTDOWN("&7Teleport za &e%time%s"),
    SPAWN_CANCEL("&cTeleport anulowany – spawn padł"),
    SPAWN_TELEPORTED("&7Teleportowano na spawn"),
    SPAWN_ALREADY("&cJuż jesteś na spawnie, nie udawaj że nie wiesz"),


    RANDOM_TITLE("&6RandomTP"),
    RANDOM_NO_SECTORS("&cBrak dostępnych sektorów do teleportacji"),
    RANDOM_SECTOR_OFFLINE("&cSektor jest offline – teleport przerwany"),
    RANDOM_LOC_FAIL("&cNie udało się wylosować lokacji"),
    RANDOM_START("&7Losowanie sektora..."),
    RANDOM_COUNTDOWN("&7Teleportacja na &e%sector% &7za &f%time%s"),
    RANDOM_CANCEL("&cSektor padł – teleport anulowany"),
    RANDOM_TELEPORTED("&7Sektor: &e%sector%");

    private final String text;

    Messages(String text) {
        this.text = text;
    }

    public String get() {
        return ChatUtil.fixColors(text);
    }

    public String format(String key, String value) {
        return ChatUtil.fixColors(text.replace("%" + key + "%", value));
    }

    public String format(int time) {
        return ChatUtil.fixColors(text.replace("%time%", String.valueOf(time)));
    }



    public String format(String key1, String value1, String key2, String value2) {
        return ChatUtil.fixColors(
                text.replace("%" + key1 + "%", value1)
                        .replace("%" + key2 + "%", value2)
        );
    }
}
