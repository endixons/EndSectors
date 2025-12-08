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


package pl.endixon.sectors.paper.sector;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import pl.endixon.sectors.common.packet.Packet;
import pl.endixon.sectors.common.packet.PacketChannel;
import pl.endixon.sectors.common.redis.RedisManager;
import pl.endixon.sectors.common.sector.SectorData;
import pl.endixon.sectors.common.sector.SectorType;
import pl.endixon.sectors.common.util.Corner;
import pl.endixon.sectors.paper.PaperSector;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Getter
@Setter
@RequiredArgsConstructor
public class Sector {

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

    public String getTPSColored() {
        double currentTps = getTPS();
        ChatColor color = ChatColor.RED;
        if (currentTps >= 19.0) color = ChatColor.GREEN;
        else if (currentTps >= 16.0) color = ChatColor.YELLOW;

        String tpsFormatted = String.format(Locale.US, "%.2f", Math.min(currentTps, 20.0));
        return color + tpsFormatted;
    }

    public void setTPS(float tps) {
        this.tps = tps;
    }

    public double getLastInfoPacket() {
        if (lastInfoPacket == -1) return 0.0;
        return (System.currentTimeMillis() - lastInfoPacket) / 1000.0;
    }

    public void setLastInfoPacket() {
        this.lastInfoPacket = System.currentTimeMillis();
    }

    public boolean isInSector(Location loc) {
        return loc.getBlockX() <= Math.max(this.getFirstCorner().getPosX(), this.getSecondCorner().getPosX())
                && loc.getBlockX() >= Math.min(this.getFirstCorner().getPosX(), this.getSecondCorner().getPosX())
                && loc.getBlockZ() <= Math.max(this.getFirstCorner().getPosZ(), this.getSecondCorner().getPosZ())
                && loc.getBlockZ() >= Math.min(this.getFirstCorner().getPosZ(), this.getSecondCorner().getPosZ())
                && this.getWorldName().equals(loc.getWorld().getName());
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
        List<Sector> sectors = Arrays.asList(
                this.sectorManager.getSector(location.clone().add(distance, 0, 0)),
                this.sectorManager.getSector(location.clone().add(-distance, 0, 0)),
                this.sectorManager.getSector(location.clone().add(0, 0, distance)),
                this.sectorManager.getSector(location.clone().add(0, 0, -distance))
        );

        for (Sector sector : sectors) {
            if (sector != null
                    && sector.getWorldName().equals(this.getWorldName())
                    && !sector.getName().equals(this.getName())) {
                return sector;
            }
        }

        return null;
    }

    public void knockBorder(Player player, double power) {
        Location center = new Location(
                player.getWorld(),
                this.getCenter().getPosX(),
                player.getLocation().getY(),
                this.getCenter().getPosZ()
        );

        Vector direction = center.toVector()
                .subtract(player.getLocation().toVector())
                .normalize();

        player.setVelocity(direction.multiply(power));
    }

    public void sendPacketProxy(Packet packet) {
        RedisManager.getInstance().publish(PacketChannel.PROXY, packet);
    }

    public void sendPacketSectors(Packet packet) {
        RedisManager.getInstance().publish(PacketChannel.SECTORS, packet);
    }
}

