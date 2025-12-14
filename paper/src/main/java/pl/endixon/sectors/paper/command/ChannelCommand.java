/*
 * EndSectors Non-Commercial License
 * (c) 2025 Endixon
 *
 * Permission is granted to use, copy, and
 * modify this software **only** for personal
 * or educational purposes.
 *
 * Commercial use, redistribution, claiming
 * this work as your own, or copying code
 * without explicit permission is strictly
 * prohibited.
 *
 * Visit https://github.com/Endixon/EndSectors
 * for more info.
 */

package pl.endixon.sectors.paper.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.endixon.sectors.common.sector.SectorType;
import pl.endixon.sectors.paper.inventory.SectorChannelWindow;
import pl.endixon.sectors.paper.sector.Sector;
import pl.endixon.sectors.paper.sector.SectorManager;
import pl.endixon.sectors.paper.sector.transfer.SectorTeleportService;
import pl.endixon.sectors.paper.user.UserManager;
import pl.endixon.sectors.paper.user.UserRedis;

public class ChannelCommand implements CommandExecutor {

    private final SectorManager sectorManager;
    private final SectorTeleportService teleportService;

    public ChannelCommand(SectorManager sectorManager, SectorTeleportService teleportService) {
        this.sectorManager = sectorManager;
        this.teleportService = teleportService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) return true;

        if (!isInSpawnSector(player)) return true;

        UserRedis user = UserManager.getUser(player);
        new SectorChannelWindow(player, sectorManager, user, teleportService).open();
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
