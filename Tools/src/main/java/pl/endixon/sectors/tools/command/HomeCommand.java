package pl.endixon.sectors.tools.command;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import pl.endixon.sectors.common.util.ChatUtil;
import pl.endixon.sectors.paper.SectorsAPI;
import pl.endixon.sectors.paper.sector.Sector;
import pl.endixon.sectors.paper.user.UserRedis;
import pl.endixon.sectors.tools.cache.UserCache;
import pl.endixon.sectors.tools.service.home.Home;
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

        user.setTransferOffsetUntil(0);

        String homeName = "home"; //todo dodać wiele home
        Home home = profile.getHomes().get(homeName);
        if (home == null) {
            player.sendMessage(Component.text(ChatUtil.fixHexColors("&#FF5555Nie masz ustawionego home o nazwie &#FFAA00" + homeName + "&#FF5555!")));
            return true;
        }

        Sector homeSector = sectorsAPI.getSectorManager().getSector(home.getSector());
        if (homeSector == null) {
            player.sendMessage(Component.text(ChatUtil.fixHexColors("&#FF5555Nie udało się znaleźć sektora dla home!")));
            return true;
        }

        World world = Bukkit.getWorld(homeSector.getWorldName());
        if (world == null) {
            player.sendMessage(Component.text(ChatUtil.fixHexColors("&#FF5555Świat dla home nie jest załadowany!")));
            return true;
        }

        user.setX(home.getX());
        user.setY(home.getY());
        user.setZ(home.getZ());
        user.setYaw(home.getYaw());
        user.setPitch(home.getPitch());

        Location loc = new Location(world, home.getX(), home.getY(), home.getZ(), home.getYaw(), home.getPitch());

        if (home.getSector().equals(user.getSectorName())) {
            player.teleport(loc);
            user.updateAndSave(player, homeSector);
            player.sendMessage(Component.text(ChatUtil.fixHexColors("&#00FFAAPomyślnie przeteleportowano!")));
        } else {
            sectorsAPI.getPaperSector().getSectorTeleportService().teleportToSector(player, user, homeSector, false, true);
            player.sendMessage(Component.text(ChatUtil.fixHexColors("&#00FFAAPomyślnie przeteleportowano!")));
        }

        return true;
    }
}
