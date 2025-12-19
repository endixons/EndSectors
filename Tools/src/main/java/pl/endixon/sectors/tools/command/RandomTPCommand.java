package pl.endixon.sectors.tools.command;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.Sound;

import pl.endixon.sectors.paper.SectorsAPI;
import pl.endixon.sectors.paper.event.sector.SectorChangeEvent;
import pl.endixon.sectors.paper.sector.Sector;
import pl.endixon.sectors.paper.user.UserRedis;
import pl.endixon.sectors.tools.utils.TeleportHelper;
import pl.endixon.sectors.common.util.ChatUtil;
import pl.endixon.sectors.tools.utils.Messages;

public class RandomTPCommand implements CommandExecutor {

    private static final int COUNTDOWN_TIME = 10;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text(ChatUtil.fixHexColors(Messages.CONSOLE_BLOCK.get())));
            return true;
        }

        SectorsAPI api = SectorsAPI.getInstance();
        if (api == null) return true;

        player.sendTitle(Messages.RANDOM_TITLE.get(),
                Messages.RANDOM_START.get(),
                0, 9999, 0);

        UserRedis user = api.getUser(player).orElse(null);
        if (user == null) {
            player.sendMessage(Component.text(ChatUtil.fixHexColors("&#FF5555Profil użytkownika nie został znaleziony!")));
            return true;
        }
        boolean isAdmin = player.hasPermission("endsectors.admin");
        int countdown = isAdmin ? 0 : COUNTDOWN_TIME;
        user.setTransferOffsetUntil(0);
        TeleportHelper.startTeleportCountdown(player, countdown, () -> {
            api.getRandomLocation(player, user);

            Sector randomSector = api.getSectorManager().getSector(user.getSectorName());
            if (randomSector == null) {
                player.sendMessage(Component.text(ChatUtil.fixHexColors("&#FF5555Nie udało się znaleźć losowego sektora!")));
                return;
            }
            SectorChangeEvent event = new SectorChangeEvent(player, randomSector);
            api.getPaperSector().getServer().getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
            }
        });
        return true;
    }
}
