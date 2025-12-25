/*
 *
 * EndSectors – Non-Commercial License
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
 *
 */

package pl.endixon.sectors.paper.sector;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import pl.endixon.sectors.common.Common;
import pl.endixon.sectors.common.packet.PacketChannel;
import pl.endixon.sectors.common.packet.object.PacketRequestTeleportSector;
import pl.endixon.sectors.common.sector.SectorType;
import pl.endixon.sectors.paper.PaperSector;
import pl.endixon.sectors.paper.event.SectorChangeEvent;
import pl.endixon.sectors.paper.manager.SectorManager;
import pl.endixon.sectors.paper.user.profile.UserProfile;
import pl.endixon.sectors.paper.util.LoggerUtil;

public class SectorTeleport {

    private final PaperSector plugin;

    public SectorTeleport(PaperSector plugin) {
        this.plugin = plugin;
    }

    public void teleportToSector(Player player, UserProfile user, Sector sector, boolean forceTransfer, boolean preserveCoordinates) {
        long startTime = System.currentTimeMillis();

        SectorManager sectorManager = plugin.getSectorManager();
        Sector current = sectorManager.getCurrentSector();

        if (!forceTransfer && current != null && current.getType() == SectorType.SPAWN && sector.getType() == SectorType.SPAWN) {
            LoggerUtil.info(() -> String.format("[Transfer] Blocked spawn-to-spawn transfer for %s", player.getName()));
            return;
        }

        LoggerUtil.info(() -> String.format("[Transfer] Starting teleport for player %s -> %s", player.getName(), sector.getName()));

        Bukkit.getScheduler().runTask(plugin, () -> {
            SectorChangeEvent event = new SectorChangeEvent(player, sector);
            Bukkit.getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                LoggerUtil.info(() -> String.format("[Transfer] Teleport cancelled by event for %s", player.getName()));
                return;
            }

            if (player.isInsideVehicle()) {
                player.leaveVehicle();
            }

            LoggerUtil.info(() -> String.format("[Transfer] Updating player data for %s", player.getName()));
            user.updateAndSave(player, sector, preserveCoordinates);

            LoggerUtil.info(() -> String.format("[Transfer] Sending teleport packet for %s", player.getName()));
            PacketRequestTeleportSector packet = new PacketRequestTeleportSector(player.getName(), sector.getName());
            Common.getInstance().getNatsManager().publish(PacketChannel.PACKET_TELEPORT_TO_SECTOR.getSubject(), packet);
            long duration = System.currentTimeMillis() - startTime;
            LoggerUtil.info(() -> String.format("[Transfer] Teleport process finished for %s (ms: %d)", player.getName(), duration));
        });
    }
}