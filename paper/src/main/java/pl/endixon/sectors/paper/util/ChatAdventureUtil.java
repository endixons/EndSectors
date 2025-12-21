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

package pl.endixon.sectors.paper.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import pl.endixon.sectors.common.util.ChatUtil;

public class ChatAdventureUtil {

    private final Pattern hexPattern;

    public ChatAdventureUtil(Pattern hexPattern) {
        this.hexPattern = hexPattern;
    }

    public ChatAdventureUtil() {
        this(Pattern.compile("&#([A-Fa-f0-9]{6})"));
    }

    public Component toComponent(String message) {
        if (message == null || message.isEmpty())
            return Component.empty();

        Matcher matcher = hexPattern.matcher(message);
        Component component = Component.empty();
        int lastIndex = 0;

        while (matcher.find()) {
            if (matcher.start() > lastIndex) {
                component = component.append(Component.text(message.substring(lastIndex, matcher.start())));
            }
            String hex = matcher.group(1);
            TextColor color = TextColor.fromHexString("#" + hex);
            int endIndex = matcher.end();
            int nextColor = message.indexOf("&#", endIndex);
            if (nextColor == -1)
                nextColor = message.length();
            String textAfterHex = message.substring(endIndex, nextColor);
            component = component.append(Component.text(textAfterHex, color));
            lastIndex = nextColor;
        }

        if (lastIndex == 0) {
            return Component.text(ChatUtil.fixColors(message));
        }

        if (lastIndex < message.length()) {
            component = component.append(Component.text(message.substring(lastIndex)));
        }

        return component;
    }
}
