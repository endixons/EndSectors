package pl.endixon.sectors.paper.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import pl.endixon.sectors.common.sector.SectorType;
import pl.endixon.sectors.paper.inventory.SectorChannelWindow;
import pl.endixon.sectors.paper.manager.SectorManager;
import pl.endixon.sectors.paper.sector.Sector;
import pl.endixon.sectors.paper.sector.SectorTeleport;
import pl.endixon.sectors.paper.user.profile.UserProfile;
import pl.endixon.sectors.paper.user.profile.UserProfileRepository;

import pl.endixon.sectors.paper.util.ChatAdventureUtil;
import pl.endixon.sectors.paper.util.MessagesUtil;

public class ChannelCommand implements CommandExecutor {

    private final SectorManager sectorManager;
    private final SectorTeleport teleportService;

    public ChannelCommand(SectorManager sectorManager, SectorTeleport teleportService) {
        this.sectorManager = sectorManager;
        this.teleportService = teleportService;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

        if (!(sender instanceof Player player)) {
            return true;
        }

        if (!this.isInSpawnSector(player)) {
            return true;
        }

        UserProfile user = UserProfileRepository.getIfPresent(player.getName()).orElse(null);
        if (user == null) {
            player.sendMessage((MessagesUtil.playerDataNotFoundMessage.get()));
            return true;
        }

        new SectorChannelWindow(player, sectorManager, user, teleportService).open();
        return true;
    }

    private boolean isInSpawnSector(Player player) {
        final Sector current = sectorManager.getCurrentSector();
        if (current == null || current.getType() != SectorType.SPAWN) {
            player.sendMessage((MessagesUtil.ONLY_IN_SPAWN_MESSAGE.get()));
            return false;
        }
        return true;
    }
}