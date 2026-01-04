/*
 *
 *  EndSectors  Non-Commercial License
 *  (c) 2025 Endixon
 *
 *  Permission is granted to use, copy, and
 *  modify this software **only** for personal
 *  or educational purposes.
 *
 *   Commercial use, redistribution, claiming
 *  this work as your own, or copying code
 *  without explicit permission is strictly
 *  prohibited.
 *
 *  Visit https://github.com/Endixon/EndSectors
 *  for more info.
 *
 */

package pl.endixon.sectors.paper.task;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import pl.endixon.sectors.common.sector.SectorType;
import pl.endixon.sectors.paper.manager.SectorManager;
import pl.endixon.sectors.paper.sector.Sector;
import pl.endixon.sectors.paper.util.LoggerUtil;

public class ProtocolLibWorldBorderTask extends BukkitRunnable {

    private static final double GROWTH = 999999999.0;
    private static final double OFFSET = 2.0;

    private final SectorManager sectorManager;

    public ProtocolLibWorldBorderTask(SectorManager sectorManager) {
        this.sectorManager = sectorManager;
    }

    @Override
    public void run() {
        Sector sector = sectorManager.getCurrentSector();
        if (sector == null)
            return;

        double borderSize = GROWTH * 2 + OFFSET;

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.getWorld().getName().equals(sector.getWorldName()))
                continue;

            List<Location> corners = Arrays.asList(new Location(player.getWorld(), sector.getFirstCorner().getPosX(), player.getLocation().getY(), sector.getFirstCorner().getPosZ()), new Location(player.getWorld(), sector.getSecondCorner().getPosX(), player.getLocation().getY(), sector.getFirstCorner().getPosZ()), new Location(player.getWorld(), sector.getFirstCorner().getPosX(), player.getLocation().getY(), sector.getSecondCorner().getPosZ()), new Location(player.getWorld(), sector.getSecondCorner().getPosX(), player.getLocation().getY(), sector.getSecondCorner().getPosZ()));

            Location nearestCorner = findNearestCorner(player, corners);
            if (nearestCorner == null)
                continue;

            Location fixedCorner = fixNearestCorner(sector, nearestCorner);

            ProtocolManager manager = ProtocolLibrary.getProtocolManager();
            try {
                PacketContainer centerPacket = new PacketContainer(PacketType.Play.Server.SET_BORDER_CENTER);
                centerPacket.getWorldBorderActions().write(0, EnumWrappers.WorldBorderAction.SET_CENTER);
                centerPacket.getDoubles().write(0, fixedCorner.getX());
                centerPacket.getDoubles().write(1, fixedCorner.getZ());

                manager.sendServerPacket(player, centerPacket);
                PacketContainer sizePacket = new PacketContainer(PacketType.Play.Server.SET_BORDER_LERP_SIZE);
                sizePacket.getWorldBorderActions().write(0, EnumWrappers.WorldBorderAction.LERP_SIZE);
                sizePacket.getDoubles().write(0, borderSize);
                sizePacket.getDoubles().write(1, borderSize);
                sizePacket.getLongs().write(0, 0L);
                manager.sendServerPacket(player, sizePacket);

            } catch (Exception e) {
                LoggerUtil.warn("Failed to send world border packets to player: " + player.getName(), e);

            }
        }
    }

    private Location findNearestCorner(Player player, List<Location> corners) {
        Location nearest = null;
        double minDist = Double.MAX_VALUE;
        for (Location corner : corners) {
            double dist = corner.distance(player.getLocation());
            if (dist < minDist) {
                nearest = corner;
                minDist = dist;
            }
        }
        return nearest;
    }

    private Location fixNearestCorner(Sector sector, Location corner) {
        if (corner == null)
            return null;
        Location clone = corner.clone();
        double centerX = (sector.getFirstCorner().getPosX() + sector.getSecondCorner().getPosX()) / 2.0;
        double centerZ = (sector.getFirstCorner().getPosZ() + sector.getSecondCorner().getPosZ()) / 2.0;
        boolean xNotNearCenter = clone.getX() < centerX;
        boolean zNotNearCenter = clone.getZ() < centerZ;
        double newX = clone.getX() + (xNotNearCenter ? GROWTH : -GROWTH);
        double newZ = clone.getZ() + (zNotNearCenter ? GROWTH : -GROWTH);
        clone.setX(newX + (xNotNearCenter ? -OFFSET : OFFSET));
        clone.setZ(newZ + (zNotNearCenter ? -OFFSET : OFFSET));
        return clone;
    }
}
