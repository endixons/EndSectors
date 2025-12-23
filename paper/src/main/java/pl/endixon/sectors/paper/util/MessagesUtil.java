package pl.endixon.sectors.paper.util;

import net.kyori.adventure.text.Component;
import pl.endixon.sectors.paper.PaperSector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum MessagesUtil {
    RELOAD_SUCCESS,
    NO_PERMISSION,
    UNKNOWN_OPTION,
    HELP_MENU,
    SECTOR_CONNECTED_MESSAGE,
    SECTOR_ERROR_TITLE,
    SECTOR_FULL_SUBTITLE,
    SECTOR_DISABLED_SUBTITLE,
    TITLE_WAIT_TIME,
    PROTECTION_ACTIONBAR,
    ONLY_IN_SPAWN_MESSAGE,
    BORDER_MESSAGE,
    BREAK_BORDER_DISTANCE_MESSAGE,
    PLACE_BORDER_DISTANCE_MESSAGE,
    playerAlreadyConnectedMessage,
    sectorIsOfflineMessage,
    playerDataNotFoundMessage,
    spawnSectorNotFoundMessage,
    SectorNotFoundMessage,
    PLAYER_NOT_FOUND_DB,
    PLAYER_ONLINE_STATUS,
    GLOBAL_ONLINE,
    CURRENT_SECTOR,
    USAGE_EXECUTE,
    COMMAND_BROADCASTED,
    SPECIFY_NICKNAME,
    SECTOR_STARTED_NOTIFICATION,
    SECTOR_STOPPED_NOTIFICATION,
    CHANNEL_GUI_TITLE,
    CHANNEL_ITEM_NAME,
    CHANNEL_OFFLINE,
    CHANNEL_CURRENT,
    CHANNEL_CLICK_TO_CONNECT,
    CHANNEL_LORE_FORMAT,
    SHOW_GUI_TITLE,
    SHOW_ITEM_NAME,
    SHOW_STATUS_ONLINE,
    SHOW_STATUS_OFFLINE,
    SHOW_LORE_FORMAT,
    INSPECT_FORMAT;


    private static final ChatAdventureUtil CHAT_HELPER = new ChatAdventureUtil();

    public String getRaw() {
        return PaperSector.getInstance().getMessageLoader().getMessages().getOrDefault(this.name(), "<red>Missing message: " + this.name());
    }

    public String getText(String... replacements) {
        String raw = getRaw();
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) raw = raw.replace(replacements[i], replacements[i + 1]);
        }
        return CHAT_HELPER.toLegacyString(raw);
    }

    public Component get(String... replacements) {
        String raw = getRaw();
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) raw = raw.replace(replacements[i], replacements[i + 1]);
        }
        return CHAT_HELPER.toComponent(raw);
    }

    public List<String> asLore(String... replacements) {
        List<String> rawLore = PaperSector.getInstance().getMessageLoader().getMessagesLore().get(this.name());
        if (rawLore == null) return Collections.singletonList("§cMissing lore: " + this.name());

        List<String> processed = new ArrayList<>();
        for (String line : rawLore) {
            String processedLine = line;
            for (int i = 0; i < replacements.length; i += 2) {
                if (i + 1 < replacements.length) {
                    processedLine = processedLine.replace(replacements[i], replacements[i + 1]);
                }
            }
            processed.add(CHAT_HELPER.toLegacyString(processedLine));
        }
        return processed;
    }
}