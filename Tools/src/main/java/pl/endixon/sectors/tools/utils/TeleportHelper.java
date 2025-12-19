package pl.endixon.sectors.tools.utils;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import pl.endixon.sectors.tools.Main;
import pl.endixon.sectors.common.util.ChatUtil;

public class TeleportHelper {

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
                    player.sendTitle(
                            ChatUtil.fixHexColors("&#FF5555Teleport anulowany!"),
                            ChatUtil.fixHexColors("&#FF4444Ruszyłeś się!"),
                            5, 40, 10
                    );

                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 0.8f);
                    cancel();
                    return;
                }

                if (countdown > 0) {
                    player.sendTitle(
                            ChatUtil.fixHexColors("&#FFD700Teleport za..."),
                            ChatUtil.fixHexColors("&#FFA500" + countdown + " &#FFD700sekund"),
                            0, 20, 0
                    );

                    countdown--;
                    return;
                }
                onFinish.run();
                cancel();
            }
        }.runTaskTimer(Main.getInstance(), 20L, 20L);
    }
}
