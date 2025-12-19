package pl.endixon.sectors.tools.command;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.endixon.sectors.paper.SectorsAPI;
import pl.endixon.sectors.tools.Main;
import pl.endixon.sectors.tools.cache.UserCache;
import pl.endixon.sectors.tools.service.home.Home;
import pl.endixon.sectors.tools.service.users.PlayerProfile;

public class SetHomeCommand implements CommandExecutor {

    private final Main plugin = Main.getInstance();
    private final SectorsAPI sectorsAPI = SectorsAPI.getInstance();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cTa komenda jest tylko dla gracza");
            return true;
        }

        PlayerProfile profile = UserCache.get(player.getUniqueId());
        if (profile == null) {
            player.sendMessage("§cProfil niezaładowany");
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

        player.sendMessage("§aHome ustawiony na sektorze §e" + sector);
        return true;
    }
}
