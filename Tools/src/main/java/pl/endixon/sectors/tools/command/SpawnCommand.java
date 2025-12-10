package pl.endixon.sectors.tools.command;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import pl.endixon.sectors.common.sector.SectorType;
import pl.endixon.sectors.common.util.ChatUtil;
import pl.endixon.sectors.paper.sector.Sector;
import pl.endixon.sectors.paper.sector.SectorManager;
import pl.endixon.sectors.paper.user.UserManager;
import pl.endixon.sectors.paper.user.UserMongo;
import pl.endixon.sectors.tools.Main;
import pl.endixon.sectors.tools.utils.Messages;

public class SpawnCommand implements CommandExecutor {

    private static final int COUNTDOWN_TIME = 10;
    private static final Location SPAWN_LOC_TEMPLATE =
            new Location(null, 0.5, 70, 0.5, 0f, 0f);

    private final SectorManager sectorManager;

    public SpawnCommand(SectorManager sectorManager) {
        this.sectorManager = sectorManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Messages.CONSOLE_BLOCK.get());
            return true;
        }

        Sector currentSector = sectorManager.getCurrentSector();

        if (currentSector != null && currentSector.getType() == SectorType.SPAWN) {
            player.sendTitle(
                    Messages.SPAWN_TITLE.get(),
                    Messages.SPAWN_ALREADY.get(),
                    10, 40, 10
            );
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return true;
        }


        Sector spawnSector = sectorManager.getSectors().stream()
                .filter(s -> s.getType().name().equalsIgnoreCase("SPAWN"))
                .filter(Sector::isOnline)
                .findFirst()
                .orElse(null);

        if (spawnSector == null) {
            player.sendTitle(
                    Messages.SPAWN_TITLE.get(),
                    Messages.SPAWN_OFFLINE.get(),
                    10, 40, 10
            );
            return true;
        }

        Location spawnLoc = SPAWN_LOC_TEMPLATE.clone();
        spawnLoc.setWorld(Bukkit.getWorld(spawnSector.getWorldName()));

        UserMongo user = UserManager.getUser(player);
        user.setLastTransferTimestamp(System.currentTimeMillis());

        player.sendTitle(
                Messages.SPAWN_TITLE.get(),
                Messages.SPAWN_START.get(),
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
                            5, 40, 10
                    );

                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 0.8f);
                    cancel();
                    return;
                }

                if (!spawnSector.isOnline()) {
                    player.sendTitle(
                            Messages.SPAWN_TITLE.get(),
                            Messages.SPAWN_CANCEL.get(),
                            10, 40, 10
                    );
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 0.8f);
                    cancel();
                    return;
                }

                if (countdown > 0) {
                    player.sendTitle(
                            Messages.SPAWN_TITLE.get(),
                            Messages.SPAWN_COUNTDOWN.format(countdown),
                            0, 20, 0
                    );
                    countdown--;
                    return;
                }

                player.teleport(spawnLoc);
                user.updatePlayerData(player, spawnSector);

                player.sendTitle(
                        Messages.SPAWN_TITLE.get(),
                        Messages.SPAWN_TELEPORTED.get(),
                        5, 40, 10
                );
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);

                cancel();
            }
        }.runTaskTimer(Main.getInstance(), 20L, 20L);

        return true;
    }
}
