package pl.endixon.sectors.paper.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import pl.endixon.sectors.common.sector.SectorType;
import pl.endixon.sectors.paper.inventory.SectorChannelWindow;
import pl.endixon.sectors.paper.manager.SectorManager;
import pl.endixon.sectors.paper.sector.Sector;
import pl.endixon.sectors.paper.sector.SectorTeleport;
import pl.endixon.sectors.paper.user.profile.UserProfile;
import pl.endixon.sectors.paper.user.profile.UserProfileRepository;
import pl.endixon.sectors.paper.util.LoggerUtil;

public class ChannelCommand implements CommandExecutor {

    private final SectorManager sectorManager;
    private final SectorTeleport teleportService;

    public ChannelCommand(SectorManager sectorManager, SectorTeleport teleportService) {
        this.sectorManager = sectorManager;
        this.teleportService = teleportService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            LoggerUtil.info("[ChannelCommand] Command sender is not a player.");
            return true;
        }

        if (!isInSpawnSector(player)) {
            LoggerUtil.info("[ChannelCommand] Player " + player.getName() + " tried to use the command outside SPAWN sector.");
            return true;
        }

        UserProfile user = UserProfileRepository.getUser(player).orElse(null);
        if (user == null) {
            LoggerUtil.info("[ChannelCommand] Could not retrieve profile for player " + player.getName());
            player.sendMessage("§cNie znaleziono twojego profilu!");
            return true;
        }

        new SectorChannelWindow(player, sectorManager, user, teleportService).open();
        LoggerUtil.info("[ChannelCommand] Player " + player.getName() + " opened the SectorChannelWindow.");
        return true;
    }

    private boolean isInSpawnSector(Player player) {
        Sector current = sectorManager.getCurrentSector();
        if (current == null || current.getType() != SectorType.SPAWN) {
            player.sendMessage("§cNie możesz używać tej komendy poza sektorem SPAWN!");
            return false;
        }
        return true;
    }
}
