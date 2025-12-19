package pl.endixon.sectors.tools.command;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.endixon.sectors.common.sector.SectorType;
import pl.endixon.sectors.common.util.ChatUtil;
import pl.endixon.sectors.paper.SectorsAPI;
import pl.endixon.sectors.paper.sector.Sector;
import pl.endixon.sectors.paper.user.UserRedis;
import pl.endixon.sectors.tools.cache.UserCache;
import pl.endixon.sectors.tools.inventory.HomeWindow;
import pl.endixon.sectors.tools.service.users.PlayerProfile;
import pl.endixon.sectors.tools.utils.Messages;

public class HomeCommand implements CommandExecutor {

    private final SectorsAPI sectorsAPI = SectorsAPI.getInstance();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text(ChatUtil.fixHexColors(Messages.CONSOLE_BLOCK.get())));
            return true;
        }

        PlayerProfile profile = UserCache.get(player.getUniqueId());
        if (profile == null) {
            player.sendMessage(Component.text(ChatUtil.fixHexColors("&#FF5555Profil użytkownika nie został znaleziony!")));
            return true;
        }


        UserRedis user = sectorsAPI.getUser(player).orElse(null);
        if (user == null) {
            player.sendMessage(Component.text(ChatUtil.fixHexColors("&#FF5555Profil użytkownika nie został znaleziony!")));
            return true;
        }



        new HomeWindow(player, profile).open();
        return true;
    }
}
