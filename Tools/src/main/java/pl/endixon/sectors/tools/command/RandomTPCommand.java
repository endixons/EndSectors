package pl.endixon.sectors.tools.command;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import pl.endixon.sectors.common.sector.SectorType;
import pl.endixon.sectors.common.util.ChatUtil;
import pl.endixon.sectors.paper.event.sector.SectorChangeEvent;

import pl.endixon.sectors.paper.sector.Sector;
import pl.endixon.sectors.paper.sector.SectorManager;
import pl.endixon.sectors.paper.user.UserManager;
import pl.endixon.sectors.paper.user.UserRedis;
import pl.endixon.sectors.tools.Main;

import pl.endixon.sectors.tools.utils.Messages;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RandomTPCommand implements CommandExecutor {

    private static final int COUNTDOWN_TIME = 10;
    private final SectorManager sectorManager;

    public RandomTPCommand(SectorManager sectorManager) {
        this.sectorManager = sectorManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Messages.CONSOLE_BLOCK.get());
            return true;
        }

        UserRedis user = UserManager.getUser(player);

        List<Sector> sectors = sectorManager.getSectors().stream()
                .filter(Sector::isOnline)
                .filter(s -> s.getType() != SectorType.SPAWN
                        && s.getType() != SectorType.QUEUE
                        && s.getType() != SectorType.NETHER
                        && s.getType() != SectorType.END)
                .toList();

        if (sectors.isEmpty()) {
            player.sendTitle(
                    Messages.RANDOM_TITLE.get(),
                    Messages.RANDOM_NO_SECTORS.get(),
                    10, 40, 10
            );
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 0.8f);
            return true;
        }

        Sector sector = sectors.get(ThreadLocalRandom.current().nextInt(sectors.size()));
        var loc = sectorManager.randomLocation(sector);

        if (!sector.isOnline() || loc == null) {
            player.sendTitle(
                    Messages.RANDOM_TITLE.get(),
                    loc == null ? Messages.RANDOM_LOC_FAIL.get() : Messages.RANDOM_SECTOR_OFFLINE.get(),
                    10, 40, 10
            );
            return true;
        }

        user.setLastTransferTimestamp(System.currentTimeMillis());

        player.sendTitle(
                Messages.RANDOM_TITLE.get(),
                Messages.RANDOM_START.get(),
                0, 9999, 0
        );
        final Location startLocation = player.getLocation().clone();

        new BukkitRunnable() {
            int countdown = COUNTDOWN_TIME;

            @Override
            public void run() {


                if (!player.getLocation().getBlock().equals(startLocation.getBlock())) {
                    player.sendTitle(
                            ChatUtil.fixColors(Messages.SPAWN_TITLE.get()),
                            ChatUtil.fixColors("&cTeleport anulowany – ruszyłeś się!"),
                            5,
                            40,
                            10
                    );

                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 0.8f);
                    cancel();
                    return;
                }

                if (!sector.isOnline()) {
                    player.sendTitle(
                            Messages.RANDOM_TITLE.get(),
                            Messages.RANDOM_CANCEL.get(),
                            10, 40, 10
                    );
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 0.8f);
                    cancel();
                    return;
                }

                if (countdown > 0) {
                    player.sendTitle(
                            Messages.RANDOM_TITLE.get(),
                            Messages.RANDOM_COUNTDOWN.format(
                                    "sector", sector.getName(),
                                    "time", String.valueOf(countdown)
                            ),
                            0, 20, 0
                    );
                    countdown--;
                    return;
                }

                SectorChangeEvent event = new SectorChangeEvent(player, sector);
                Bukkit.getPluginManager().callEvent(event);

                if (!event.isCancelled()) {
                    player.teleport(loc);
                    user.setLastSectorTransfer(true);
                    user.updateFromPlayer(player, sector);

                    Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
                        Vector v = player.getLocation().getDirection().normalize().multiply(0.8);
                        player.setVelocity(v);
                    }, 2L);

                    player.sendTitle(
                            Messages.RANDOM_TITLE.get(),
                            Messages.RANDOM_TELEPORTED.format("sector", sector.getName()),
                            5, 40, 10
                    );
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
                }

                cancel();
            }
        }.runTaskTimer(Main.getInstance(), 20L, 20L);

        return true;
    }
}
