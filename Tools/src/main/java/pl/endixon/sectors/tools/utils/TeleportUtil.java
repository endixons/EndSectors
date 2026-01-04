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

import java.time.Duration;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import pl.endixon.sectors.tools.EndSectorsToolsPlugin;

public class TeleportUtil {

    public static void startTeleportCountdown(Player player, int seconds, Runnable onFinish) {
        Location startLocation = player.getLocation().clone();

        new BukkitRunnable() {
            int countdown = seconds;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }

                if (!player.getLocation().getBlock().equals(startLocation.getBlock())) {
                    player.showTitle(Title.title(
                            MessagesUtil.TELEPORT_CANCELLED_TITLE.get(),
                            MessagesUtil.TELEPORT_CANCELLED_SUBTITLE.get(),
                            Title.Times.times(Duration.ofMillis(200), Duration.ofSeconds(2), Duration.ofMillis(200))
                    ));

                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 0.8f);
                    cancel();
                    return;
                }

                if (countdown > 0) {
                    String subtitleRaw = MessagesUtil.TELEPORT_COUNTDOWN_SUBTITLE.getRaw()
                            .replace("{TIME}", String.valueOf(countdown));

                    player.showTitle(Title.title(
                            MessagesUtil.TELEPORT_COUNTDOWN_TITLE.get(),
                            new ChatAdventureUtil().toComponent(subtitleRaw),
                            Title.Times.times(Duration.ofMillis(100), Duration.ofMillis(800), Duration.ofMillis(100))
                    ));
                    countdown--;
                    return;
                }

                onFinish.run();
                player.sendMessage(MessagesUtil.TELEPORT_SUCCESS.get());
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);
                cancel();
            }
        }.runTaskTimer(EndSectorsToolsPlugin.getInstance(), 0L, 20L);
    }
}