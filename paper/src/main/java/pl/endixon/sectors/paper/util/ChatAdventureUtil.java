package pl.endixon.sectors.paper.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;


public class ChatAdventureUtil {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacySection();


    public String toLegacyString(String message) {
        if (message == null || message.isEmpty()) return "";
        return LEGACY_SERIALIZER.serialize(this.toComponent(message));
    }

    public Component toComponent(String message) {
        if (message == null || message.isEmpty()) return Component.empty();
        String modernized = message.replace("&#", "<#").replace("}", ">");
        modernized = modernized.replaceAll("&#([A-Fa-f0-9]{6})", "<#$1>");
        return MINI_MESSAGE.deserialize(modernized);
    }
}