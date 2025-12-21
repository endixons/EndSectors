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

package pl.endixon.sectors.tools.utils;

import java.util.Objects;
import java.util.function.Supplier;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import pl.endixon.sectors.common.util.ChatUtil;

public final class LoggerUtil {

    private static final String PREFIX = "%M[EndSectors-Tools] %C";

    private static final ConsoleCommandSender CONSOLE = Bukkit.getServer().getConsoleSender();

    private LoggerUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void info(Object message) {
        Objects.requireNonNull(message, "message");

        CONSOLE.sendMessage(ChatUtil.fixColorsLogger(PREFIX + message));
    }

    public static void info(Supplier<?> messageSupplier) {
        Objects.requireNonNull(messageSupplier, "messageSupplier");

        info(messageSupplier.get());
    }
}
