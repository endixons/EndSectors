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
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import pl.endixon.sectors.common.sector.SectorData;
import pl.endixon.sectors.common.sector.SectorType;
import pl.endixon.sectors.paper.PaperSector;
import pl.endixon.sectors.paper.util.Logger;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class SectorManager {

    private final Map<String, Sector> sectors = new HashMap<>();
    private final PaperSector paperSector;

    @Getter
    private final String currentSectorName;

    public void addSector(SectorData sectorData) {
        this.addSector(new Sector(sectorData));
    }

    public void addSector(Sector sector) {
        this.sectors.put(sector.getName(), sector);
    }

    public void loadSectorsData(SectorData[] sectorsData) {
        for (SectorData sectorData : sectorsData) {
            this.addSector(sectorData);
        }
    }

    public Sector getSector(String sectorName) {
        return this.sectors.get(sectorName);
    }



    public Sector getSector(Location location) {
        for (Sector sector : sectors.values()) {
            if (sector.isInSector(location)) {
                if (sector.getType() == SectorType.QUEUE) continue;
                if (getCurrentSector() == null || getCurrentSector().getType() != SectorType.SPAWN) {
                    return sector;
                } else if (sector.getType() != SectorType.SPAWN) {
                    return sector;
                }
            }
        }
        return null;
    }

    public Sector find(SectorType type) {
        return this.sectors.values().stream()
                .filter(s -> s.getType() == type)
                .filter(Sector::isOnline)
                .findFirst()
                .orElse(null);
    }

    public Location randomLocation(Sector sector) {
        World world = Bukkit.getWorld(sector.getWorldName());
        if (world == null) return null;

        double safeMargin = 10;
        double minX = Math.min(sector.getFirstCorner().getPosX(), sector.getSecondCorner().getPosX()) + safeMargin;
        double maxX = Math.max(sector.getFirstCorner().getPosX(), sector.getSecondCorner().getPosX()) - safeMargin;
        double minZ = Math.min(sector.getFirstCorner().getPosZ(), sector.getSecondCorner().getPosZ()) + safeMargin;
        double maxZ = Math.max(sector.getFirstCorner().getPosZ(), sector.getSecondCorner().getPosZ()) - safeMargin;
        double x = minX + Math.random() * (maxX - minX);
        double z = minZ + Math.random() * (maxZ - minZ);
        int y = world.getHighestBlockYAt((int) x, (int) z) + 5;
        Bukkit.getLogger().info("RandomLocation -> x: " + x + ", y: " + y + ", z: " + z);

        return new Location(world, x, y, z);
    }


    public Sector getBalancedRandomSpawnSector() {
        List<Sector> onlineSpawns = sectors.values().stream()
                .filter(s -> s.getType() == SectorType.SPAWN)
                .filter(Sector::isOnline)
                .filter(s -> s.getTPS() > 0)
                .sorted(Comparator.comparingDouble(
                        s -> ((double) s.getPlayerCount() / Math.max(s.getMaxPlayers(), 1)) / s.getTPS()
                ))
                .toList();

        if (onlineSpawns.isEmpty()) {
            throw new IllegalStateException("Brak dostępnych online sektorów spawn!");
        }
        Collections.reverse(onlineSpawns);
        int topN = Math.min(3, onlineSpawns.size());
        return onlineSpawns.get(ThreadLocalRandom.current().nextInt(topN));
    }


    public Sector getCurrentSector() {
        return this.getSector(currentSectorName);
    }

    public Collection<Sector> getSectors() {
        return this.sectors.values();
    }

    public void getOnlinePlayers(Consumer<List<String>> callback) {
        paperSector.getRedisManager().getOnlinePlayers(callback);
    }


    public void isPlayerOnline(String playerName, Consumer<Boolean> callback) {
        paperSector.getRedisManager().isPlayerOnline(playerName, callback);
    }

}

