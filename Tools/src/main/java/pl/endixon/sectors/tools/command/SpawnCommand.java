package pl.endixon.sectors.tools.command;

import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.Sound;
import pl.endixon.sectors.common.util.ChatUtil;
import pl.endixon.sectors.paper.user.UserRedis;
import pl.endixon.sectors.common.sector.SectorType;
import pl.endixon.sectors.paper.SectorsAPI;
import pl.endixon.sectors.paper.sector.Sector;
import pl.endixon.sectors.tools.utils.TeleportHelper;
import pl.endixon.sectors.tools.utils.Messages;

public class SpawnCommand implements CommandExecutor {

    private static final int COUNTDOWN_TIME = 10;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatUtil.fixHexColors(Messages.CONSOLE_BLOCK.get()));
            return true;
        }

        UserRedis user = SectorsAPI.getInstance().getUser(player).orElse(null);
        if (user == null) {
            player.sendMessage("§cProfil użytkownika nie został znaleziony!");
            return true;
        }

        Sector currentSector = SectorsAPI.getInstance().getSectorManager().getCurrentSector();
        if (currentSector != null && currentSector.getType() == SectorType.SPAWN) {
            player.sendTitle(
                    ChatUtil.fixHexColors(Messages.SPAWN_TITLE.get()),
                    ChatUtil.fixHexColors(Messages.SPAWN_ALREADY.get()),
                    10, 40, 10
            );
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return true;
        }

        Sector spawnSector;
        try {
            spawnSector = SectorsAPI.getInstance().getSectorManager().getBalancedRandomSpawnSector();
        } catch (IllegalStateException e) {
            player.sendTitle(
                    ChatUtil.fixHexColors(Messages.SPAWN_TITLE.get()),
                    ChatUtil.fixHexColors(Messages.SPAWN_OFFLINE.get()),
                    10, 40, 10
            );
            return true;
        }

        user.setTransferOffsetUntil(0);
        TeleportHelper.startTeleportCountdown(player, COUNTDOWN_TIME, () -> {
            SectorsAPI.getInstance().teleportPlayer(player, user, spawnSector, false, true);
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
        });

        return true;
    }
}
