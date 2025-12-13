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
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import pl.endixon.sectors.common.sector.SectorData;
import pl.endixon.sectors.common.sector.SectorType;
import pl.endixon.sectors.paper.PaperSector;
import pl.endixon.sectors.paper.util.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class SectorManager {

    private final Map<String, Sector> sectors = new ConcurrentHashMap<>();
    private final Map<SectorType, List<Sector>> sectorsByType = new ConcurrentHashMap<>();
    private final PaperSector paperSector;

    @Getter
    private final String currentSectorName;

    public void addSector(SectorData sectorData) {
        this.addSector(new Sector(sectorData));
    }

    public void addSector(Sector sector) {
        this.sectors.put(sector.getName(), sector);
        sectorsByType.computeIfAbsent(sector.getType(), k -> new ArrayList<>()).add(sector);
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
        Sector current = getCurrentSector();
        SectorType currentType = current != null ? current.getType() : null;

        for (Sector sector : sectors.values()) {
            if (!sector.isInSector(location)) continue;
            if (sector.getType() == SectorType.QUEUE) continue;
            if (currentType != SectorType.SPAWN) return sector;
            if (sector.getType() != SectorType.SPAWN) return sector;
        }
        return null;
    }

    public Sector find(SectorType type) {
        List<Sector> list = sectorsByType.get(type);
        if (list == null) return null;

        for (Sector sector : list) {
            if (sector.isOnline()) return sector;
        }

        return null;
    }



    public Location randomLocation(Sector sector) {
        World world = Bukkit.getWorld(sector.getWorldName());
        if (world == null) return null;

        double safeMargin = 10;
        int minX = (int) (Math.min(sector.getFirstCorner().getPosX(), sector.getSecondCorner().getPosX()) + safeMargin);
        int maxX = (int) (Math.max(sector.getFirstCorner().getPosX(), sector.getSecondCorner().getPosX()) - safeMargin);
        int minZ = (int) (Math.min(sector.getFirstCorner().getPosZ(), sector.getSecondCorner().getPosZ()) + safeMargin);
        int maxZ = (int) (Math.max(sector.getFirstCorner().getPosZ(), sector.getSecondCorner().getPosZ()) - safeMargin);

        int x = minX + (int) (Math.random() * (maxX - minX + 1));
        int z = minZ + (int) (Math.random() * (maxZ - minZ + 1));
        int y = getSafeHighestY(world, x, z);

        return new Location(world, x + 0.5, y + 1, z + 0.5);
    }


    public int getSafeHighestY(World world, int x, int z) {
        int y = world.getHighestBlockYAt(x, z);
        while (y > 0) {
            Material type = world.getBlockAt(x, y, z).getType();
            if (type.isSolid() && type != Material.BEDROCK && type != Material.WATER && type != Material.LAVA) {
                return y;
            }
            y--;
        }

        return 1;
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

