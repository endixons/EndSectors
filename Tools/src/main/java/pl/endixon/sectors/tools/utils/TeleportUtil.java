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

import java.time.Duration;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import pl.endixon.sectors.tools.Main;

public class TeleportUtil {

    private static final ChatAdventureUtil CHAT = new ChatAdventureUtil();

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
                    player.showTitle(Title.title(CHAT.toComponent("&#FF5555Teleport anulowany!"), CHAT.toComponent("&#FF4444Ruszyłeś się!"), Title.Times.times(Duration.ofMillis(200), Duration.ofSeconds(2), Duration.ofMillis(200))));

                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 0.8f);

                    cancel();
                    return;
                }

                if (countdown > 0) {
                    player.showTitle(Title.title(CHAT.toComponent("&#FFD700Teleport za..."), CHAT.toComponent("&#FFA500" + countdown + " &#FFD700sekund"), Title.Times.times(Duration.ofMillis(200), Duration.ofSeconds(1), Duration.ofMillis(200))));

                    countdown--;
                    return;
                }

                onFinish.run();
                cancel();
            }
        }.runTaskTimer(Main.getInstance(), 0L, 20L);
    }
}
