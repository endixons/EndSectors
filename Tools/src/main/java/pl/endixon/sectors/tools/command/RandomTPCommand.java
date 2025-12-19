package pl.endixon.sectors.tools.command;

import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.Sound;

import pl.endixon.sectors.paper.SectorsAPI;
import pl.endixon.sectors.paper.event.sector.SectorChangeEvent;
import pl.endixon.sectors.paper.sector.Sector;
import pl.endixon.sectors.paper.user.RedisUserCache;
import pl.endixon.sectors.paper.user.UserRedis;
import pl.endixon.sectors.tools.helper.TeleportHelper;
import pl.endixon.sectors.common.util.ChatUtil;
import pl.endixon.sectors.tools.utils.Messages;

public class RandomTPCommand implements CommandExecutor {

    private static final int COUNTDOWN_TIME = 10;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatUtil.fixHexColors(Messages.CONSOLE_BLOCK.get()));
            return true;
        }

        SectorsAPI api = SectorsAPI.getInstance();
        if (api == null) return true;

        UserRedis user = api.getUser(player).orElse(null);
        if (user == null) return true;

        player.sendTitle(Messages.RANDOM_TITLE.get(),
                Messages.RANDOM_START.get(),
                0, 9999, 0);


api.resetTransferOffset(user);

        TeleportHelper.startTeleportCountdown(player, COUNTDOWN_TIME, () -> {
            // teleport random
            api.getRandomLocation(player, user);
            Sector randomSector = api.getSector(user.getSectorName());

            // wywołanie eventu
            SectorChangeEvent event = new SectorChangeEvent(player, randomSector);
            api.getPaperSector().getServer().getPluginManager().callEvent(event);

            if (!event.isCancelled()) {
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
            }
        });

        return true;
    }
}
