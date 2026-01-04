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

package pl.endixon.sectors.tools.utils;

import net.kyori.adventure.text.Component;
import pl.endixon.sectors.tools.EndSectorsToolsPlugin;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum MessagesUtil {
    CONSOLE_BLOCK,
    PLAYERDATANOT_FOUND_MESSAGE,
    PORTAL_COMBAT_TITLE,
    PORTAL_COMBAT_SUBTITLE,
    SECTOR_COMBAT_TITLE,
    SECTOR_COMBAT_SUBTITLE,
    COMBAT_NO_COMMAND,
    SPAWN_TITLE,
    SPAWN_OFFLINE,
    SPAWN_ALREADY,
    AFK_TITLE,
    AFK_OFFLINE,
    AFK_ALREADY,
    RANDOM_TITLE,
    RANDOM_START,
    RANDOM_SECTOR_NOTFOUND,
    RANDOM_SECTORSPAWN_NOTFOUND,
    RANDOM_SECTORAFK_NOTFOUND,
    TELEPORT_COUNTDOWN_TITLE,
    TELEPORT_COUNTDOWN_SUBTITLE,
    TELEPORT_CANCELLED_TITLE,
    TELEPORT_CANCELLED_SUBTITLE,
    HOME_CREATED_SUCCESS,
    HOME_DELETED_SUCCESS,
    HOME_TELEPORT_SUCCESS,
    HOME_CANT_CREATE_SPAWN,
    HOME_SECTOR_NOT_FOUND,
    HOME_WORLD_NOT_LOADED,
    HOME_GUI_TITLE,
    HOME_NAME_FORMAT,
    HOME_SET_LORE,
    HOME_EMPTY_LORE;

    private static final ChatAdventureUtil CHAT_HELPER = new ChatAdventureUtil();

    public String getRaw() {
        return EndSectorsToolsPlugin.getInstance().getMessageLoader().getMessages()
                .getOrDefault(this.name(), "<red>Missing message: " + this.name());
    }

    public String getText() {
        return CHAT_HELPER.toLegacyString(getRaw());
    }

    public String getText(String... replacements) {
        String raw = getRaw();
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                raw = raw.replace(replacements[i], replacements[i + 1]);
            }
        }
        return CHAT_HELPER.toLegacyString(raw);
    }

    public Component get() {
        return CHAT_HELPER.toComponent(getRaw());
    }

    public List<String> asLore(String... replacements) {
        List<String> rawLore = EndSectorsToolsPlugin.getInstance().getMessageLoader().getMessagesLore().get(this.name());

        if (rawLore == null) {
            return Collections.singletonList("§cMissing lore: " + this.name());
        }

        List<String> processedLore = new ArrayList<>();
        for (String line : rawLore) {
            String processedLine = line;
            for (int i = 0; i < replacements.length; i += 2) {
                if (i + 1 < replacements.length) {
                    processedLine = processedLine.replace(replacements[i], replacements[i + 1]);
                }
            }
            processedLore.add(CHAT_HELPER.toLegacyString(processedLine));
        }
        return processedLore;
    }
}