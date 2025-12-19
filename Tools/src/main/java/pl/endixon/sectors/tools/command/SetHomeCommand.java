package pl.endixon.sectors.tools.command;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.endixon.sectors.common.util.ChatUtil;
import pl.endixon.sectors.paper.SectorsAPI;
import pl.endixon.sectors.tools.Main;
import pl.endixon.sectors.tools.cache.UserCache;
import pl.endixon.sectors.tools.service.home.Home;
import pl.endixon.sectors.tools.service.users.PlayerProfile;
import pl.endixon.sectors.tools.utils.Messages;

public class SetHomeCommand implements CommandExecutor {

    private final Main plugin = Main.getInstance();
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

        Location loc = player.getLocation();
        String sector = sectorsAPI.getSectorManager().getCurrentSector().getName();
        String homeName = "home";

        Home home = new Home(
                homeName,
                sector,
                loc.getWorld().getName(),
                loc.getX(),
                loc.getY(),
                loc.getZ(),
                loc.getYaw(),
                loc.getPitch()
        );

        profile.getHomes().put(homeName, home);
        plugin.getRepository().save(profile);
        player.sendMessage(Component.text(ChatUtil.fixHexColors("&#00FFAAPomyślnie ustawiono home na sektorze &#FFFF00" + sector)));
        return true;
    }
}
