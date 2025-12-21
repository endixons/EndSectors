/*
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
 */

package pl.endixon.sectors.paper.util;

import java.util.function.Supplier;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import pl.endixon.sectors.common.util.ChatUtil;

public class LoggerUtil {

    private static final ConsoleCommandSender CONSOLE = Bukkit.getServer().getConsoleSender();

    private LoggerUtil() {
    }

    public static void info(String message) {
        System.out.println("[INFO] " + message);
    }

    public static void info(String message, Exception e) {
        System.out.println("[INFO] " + message);
        e.printStackTrace();
    }

    public static void info(Object object) {
        CONSOLE.sendMessage(ChatUtil.fixColorsLogger("%M[EndSectors-Paper] %C" + object.toString()));
    }

    public static void info(Supplier<String> supplier) {
        info(supplier.get());
    }
}
