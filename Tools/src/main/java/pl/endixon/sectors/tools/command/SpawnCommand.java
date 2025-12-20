package pl.endixon.sectors.tools.command;

import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.Sound;
import pl.endixon.sectors.paper.user.UserRedis;
import pl.endixon.sectors.common.sector.SectorType;
import pl.endixon.sectors.paper.SectorsAPI;
import pl.endixon.sectors.paper.sector.Sector;
import pl.endixon.sectors.tools.utils.TeleportUtil;
import pl.endixon.sectors.tools.utils.MessagesUtil;

import java.time.Duration;

public class SpawnCommand implements CommandExecutor {

    private static final int COUNTDOWN_TIME = 10;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(MessagesUtil.CONSOLE_BLOCK.get());
            return true;
        }

        UserRedis user = SectorsAPI.getInstance().getUser(player).orElse(null);
        if (user == null) {
            player.sendMessage(MessagesUtil.PLAYERDATANOT_FOUND_MESSAGE.get());

            return true;
        }

        Sector currentSector = SectorsAPI.getInstance().getSectorManager().getCurrentSector();
        if (currentSector != null && currentSector.getType() == SectorType.SPAWN) {
            player.showTitle(Title.title(
                    MessagesUtil.SPAWN_TITLE.get(),
                    MessagesUtil.SPAWN_ALREADY.get(),
                    Title.Times.times(
                            Duration.ofMillis(10),
                            Duration.ofMillis(40),
                            Duration.ofMillis(10)
                    )
            ));

            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return true;
        }

        Sector spawnSector;
        try {
            spawnSector = SectorsAPI.getInstance().getSectorManager().getBalancedRandomSpawnSector();
        } catch (IllegalStateException e) {
            player.showTitle(Title.title(
                    MessagesUtil.SPAWN_TITLE.get(),
                    MessagesUtil.SPAWN_OFFLINE.get(),
                    Title.Times.times(
                            Duration.ofMillis(10),
                            Duration.ofMillis(40),
                            Duration.ofMillis(10)
                    )
            ));

            return true;
        }

        user.setTransferOffsetUntil(0);

        boolean isAdmin = player.hasPermission("endsectors.admin");
        int countdown = isAdmin ? 0 : COUNTDOWN_TIME;
        TeleportUtil.startTeleportCountdown(player, countdown, () -> {
            SectorsAPI.getInstance().teleportPlayer(player, user, spawnSector, false, true);
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
        });

        return true;
    }
}
