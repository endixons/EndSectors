/*
 *
 *  EndSectors  Non-Commercial License
 *  (c) 2025 Endixon
 *
 *  Permission is granted to use, copy, and
 *  modify this software **only** for personal
 *  or educational purposes.
 *
 *   Commercial use, redistribution, claiming
 *  this work as your own, or copying code
 *  without explicit permission is strictly
 *  prohibited.
 *
 *  Visit https://github.com/Endixon/EndSectors
 *  for more info.
 *
 */

package pl.endixon.sectors.proxy.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import pl.endixon.sectors.proxy.VelocitySectorPlugin;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum ProxyMessages {

    QUEUE_TITLE,
    QUEUE_OFFLINE,
    QUEUE_FULL,
    QUEUE_POSITION,
    DISCONNECT_MESSAGE,
    EMERGENCY_KICK,
    PROXY_MOTD,
    PROXY_HOVER,
    EMERGENCY_MOTD,
    EMERGENCY_HOVER;

    private static final MiniMessage MM = MiniMessage.miniMessage();

    public String getRaw() {
        return VelocitySectorPlugin.getInstance().getMessageLoader().getMessages()
                .getOrDefault(this.name(), "<red>Missing message: " + this.name() + ">");
    }

    public Component get(String... replacements) {
        String raw = getRaw();
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) raw = raw.replace(replacements[i], replacements[i + 1]);
        }
        return MM.deserialize(raw);
    }

    public Component getMotd(String... replacements) {
        List<String> motdLines = VelocitySectorPlugin.getInstance()
                .getMessageLoader()
                .getMotd()
                .get(this.name());

        if (motdLines == null || motdLines.isEmpty()) {
            return MM.deserialize("<red>Missing MOTD: " + this.name() + ">");
        }
        String raw = String.join("\n", motdLines);
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) raw = raw.replace(replacements[i], replacements[i + 1]);
        }
        return MM.deserialize(raw);
    }

    public List<String> getRawLines(String... replacements) {
        List<String> rawLines = VelocitySectorPlugin.getInstance()
                .getMessageLoader()
                .getMotd()
                .get(this.name());
        if (rawLines == null) return Collections.singletonList("§cMissing: " + this.name());

        List<String> lines = new ArrayList<>();
        for (String line : rawLines) {
            String processedLine = line;
            for (int i = 0; i < replacements.length; i += 2) {
                if (i + 1 < replacements.length) {
                    processedLine = processedLine.replace(replacements[i], replacements[i + 1]);
                }
            }
            lines.add(processedLine);
        }
        return lines;
    }
}
