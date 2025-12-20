package pl.endixon.sectors.tools.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.endixon.sectors.paper.SectorsAPI;
import pl.endixon.sectors.paper.user.UserRedis;
import pl.endixon.sectors.tools.service.users.ProfileCache;
import pl.endixon.sectors.tools.inventory.HomeWindow;
import pl.endixon.sectors.tools.service.users.PlayerProfile;
import pl.endixon.sectors.tools.utils.MessagesUtil;

public class HomeCommand implements CommandExecutor {

    private final SectorsAPI sectorsAPI = SectorsAPI.getInstance();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(MessagesUtil.CONSOLE_BLOCK.get());
            return true;
        }

        PlayerProfile profile = ProfileCache.get(player.getUniqueId());
        if (profile == null) {
            player.sendMessage(MessagesUtil.PLAYERDATANOT_FOUND_MESSAGE.get());
            return true;
        }
        UserRedis user = sectorsAPI.getUser(player).orElse(null);
        if (user == null) {
            player.sendMessage(MessagesUtil.PLAYERDATANOT_FOUND_MESSAGE.get());
            return true;
        }

        new HomeWindow(player, profile).open();
        return true;
    }
}
