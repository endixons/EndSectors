/*
 *
 *  EndSectors  Non-Commercial License
 *  (c) 2025 Endixon
 *
 *  Permission is granted to use, copy, and
 *  modify this software **only** for personal
 *  or educational purposes.
 *
 *  Commercial use, redistribution, claiming
 *  this work as your own, or copying code
 *  without explicit permission is strictly
 *  prohibited.
 *
 *  Visit https://github.com/Endixon/EndSectors
 *  for more info.
 *
 */

package pl.endixon.sectors.paper.sector;

import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import pl.endixon.sectors.common.sector.SectorData;
import pl.endixon.sectors.common.sector.SectorType;
import pl.endixon.sectors.common.util.Corner;
import pl.endixon.sectors.paper.PaperSector;
import pl.endixon.sectors.paper.manager.SectorManager;

import pl.endixon.sectors.paper.util.ChatAdventureUtil;
import pl.endixon.sectors.paper.util.LoggerUtil;

@Getter
@Setter
@RequiredArgsConstructor
public class Sector {

    private final ChatAdventureUtil CHAT = new ChatAdventureUtil();
    private final SectorManager sectorManager = PaperSector.getInstance().getSectorManager();
    private final SectorData sectorData;
    private long lastInfoPacket = -1;
    private double tps;
    private int playerCount = 0;
    private int maxPlayers = 0;

    public String getName() {
        return this.sectorData.getName();
    }

    public Corner getFirstCorner() {
        return this.sectorData.getFirstCorner();
    }

    public Corner getSecondCorner() {
        return this.sectorData.getSecondCorner();
    }

    public String getWorldName() {
        return this.sectorData.getWorld();
    }

    public SectorType getType() {
        return this.sectorData.getType();
    }

    public Corner getCenter() {
        return this.sectorData.getCenter();
    }

    public boolean isOnline() {
        return this.sectorData.isOnline();
    }

    public void setOnline(boolean online) {
        this.sectorData.setOnline(online);
    }

    public double getTPS() {
        return this.isOnline() ? tps : 0;
    }

    public static boolean isSectorFull(Sector sector) {
        if (sector == null) {
            LoggerUtil.info("Attempted to check fullness of a null sector!");
            return true;
        }

        return sector.getPlayerCount() >= sector.getMaxPlayers();
    }

    public String getTPSColored() {
        double currentTps = this.getTPS();

        String colorTag = "<#FF0000>";

        if (currentTps >= 19.0) {
            colorTag = "<#00FF00>";
        } else if (currentTps >= 16.0) {
            colorTag = "<#FFFF00>";
        }
        String tpsFormatted = String.format("%.2f", Math.min(currentTps, 20.0));
        return colorTag + tpsFormatted;
    }


    public void setTPS(float tps) {
        this.tps = tps;
    }

    public double getLastInfoPacket() {
        if (lastInfoPacket == -1) {
            LoggerUtil.info("Last info packet has not been set yet.");
            return 0.0;
        }
        return (System.currentTimeMillis() - lastInfoPacket) / 1000.0;
    }

    public void setLastInfoPacket() {
        this.lastInfoPacket = System.currentTimeMillis();
    }

    public boolean isInSector(Location loc) {

        if (loc == null || loc.getWorld() == null) {
            LoggerUtil.info("[Sector] Location or world is null");
            return false;
        }

        int x = loc.getBlockX();
        int z = loc.getBlockZ();

        int minX = Math.min(this.getFirstCorner().getPosX(), this.getSecondCorner().getPosX());
        int maxX = Math.max(this.getFirstCorner().getPosX(), this.getSecondCorner().getPosX());
        int minZ = Math.min(this.getFirstCorner().getPosZ(), this.getSecondCorner().getPosZ());
        int maxZ = Math.max(this.getFirstCorner().getPosZ(), this.getSecondCorner().getPosZ());

        return loc.getWorld().getName().equals(this.getWorldName()) && x >= minX && x <= maxX && z >= minZ && z <= maxZ;
    }

    public int getBorderDistance(Location loc) {
        double x1 = Math.abs(loc.getBlockX() - this.getFirstCorner().getPosX());
        double x2 = Math.abs(loc.getBlockX() - this.getSecondCorner().getPosX());
        double z1 = Math.abs(loc.getBlockZ() - this.getFirstCorner().getPosZ());
        double z2 = Math.abs(loc.getBlockZ() - this.getSecondCorner().getPosZ());
        return (int) Math.min(Math.min(x1, x2), Math.min(z1, z2));
    }

    public Sector getNearestSector(Location location) {
        return this.getNearestSector(getBorderDistance(location) + 1, location);
    }

    public Sector getNearestSector(int distance, Location location) {
        List<Sector> sectors = Arrays.asList(this.sectorManager.getSector(location.clone().add(distance, 0, 0)), this.sectorManager.getSector(location.clone().add(-distance, 0, 0)), this.sectorManager.getSector(location.clone().add(0, 0, distance)), this.sectorManager.getSector(location.clone().add(0, 0, -distance)));

        for (Sector sector : sectors) {
            if (sector != null && sector.getWorldName().equals(this.getWorldName()) && !sector.getName().equals(this.getName())) {
                return sector;
            }
        }
        return null;
    }


    public void knockBorder(Player player, double power) {
        Location loc = player.getLocation();
        Corner c1 = getFirstCorner();
        Corner c2 = getSecondCorner();


        double minX = Math.min(c1.getPosX(), c2.getPosX());
        double maxX = Math.max(c1.getPosX(), c2.getPosX());
        double minZ = Math.min(c1.getPosZ(), c2.getPosZ());
        double maxZ = Math.max(c1.getPosZ(), c2.getPosZ());
        double distMinX = loc.getX() - minX;
        double distMaxX = maxX - loc.getX();
        double distMinZ = loc.getZ() - minZ;
        double distMaxZ = maxZ - loc.getZ();


        double minDist = Math.min(Math.min(distMinX, distMaxX), Math.min(distMinZ, distMaxZ));
        Vector direction = new Vector(0, 0, 0);

        if (minDist == distMinX) direction.setX(1);
        else if (minDist == distMaxX) direction.setX(-1);
        else if (minDist == distMinZ) direction.setZ(1);
        else if (minDist == distMaxZ) direction.setZ(-1);
        player.setVelocity(direction.normalize().multiply(power));
    }


}
