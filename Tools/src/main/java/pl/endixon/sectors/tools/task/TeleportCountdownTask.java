package pl.endixon.sectors.tools.task;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import pl.endixon.sectors.tools.Main;

public abstract class TeleportCountdownTask extends BukkitRunnable {

    private final Player player;
    private final int countdownStart;
    private final Location startLocation;
    private int countdown;

    public TeleportCountdownTask(Player player, int countdownStart) {
        this.player = player;
        this.countdownStart = countdownStart;
        this.countdown = countdownStart;
        this.startLocation = player.getLocation().clone();
    }

    @Override
    public void run() {

        if (!player.isOnline()) {
            cancel();
            return;
        }

        if (!player.getLocation().getBlock().equals(startLocation.getBlock())) {
            onCancelledByMove();
            cancel();
            return;
        }

        if (countdown > 0) {
            onCountdown(countdown);
            countdown--;
        } else {
            onFinish();
            cancel();
        }
    }

    public abstract void onCountdown(int countdown);

    public abstract void onFinish();

    public void onCancelledByMove() {
        player.sendTitle("&cTeleport anulowany!", "&7Ruszyłeś się!", 5, 40, 10);
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 0.8f);
    }

    public void start() {
        runTaskTimer(Main.getInstance(), 20L, 20L);
    }
}
