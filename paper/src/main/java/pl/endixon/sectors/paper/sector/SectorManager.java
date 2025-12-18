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
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import pl.endixon.sectors.common.sector.SectorData;
import pl.endixon.sectors.common.sector.SectorType;
import pl.endixon.sectors.paper.PaperSector;
import pl.endixon.sectors.paper.user.UserRedis;
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
                .filter(Sector::isOnline)
                .filter(s -> s.getType() == type)
                .filter(s -> s.getType() != SectorType.END)
                .filter(s -> s.getType() != SectorType.NETHER)
                .filter(s -> s.getType() != SectorType.QUEUE)
                .findFirst()
                .orElse(null);
    }

    public Sector getRandomSector() {
        List<Sector> candidates = sectors.values().stream()
                .filter(Sector::isOnline)
                .filter(s -> s.getType() != SectorType.NETHER)
                .filter(s -> s.getType() != SectorType.END)
                .filter(s -> s.getType() != SectorType.QUEUE)
                .filter(s -> s.getType() != SectorType.SPAWN)
                .toList();

        if (candidates.isEmpty()) {
            throw new IllegalStateException("Brak dostępnych sektorów do losowania!");
        }

        return candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
    }


    public Location randomLocation(@NonNull Player player, @NonNull UserRedis user) {
        Sector randomSector = getRandomSector();

        World world = Bukkit.getWorld(randomSector.getWorldName());
        if (world == null)
            throw new IllegalStateException("World not loaded for sector " + randomSector.getName());

        double safeMargin = 10;
        double minX = Math.min(randomSector.getFirstCorner().getPosX(), randomSector.getSecondCorner().getPosX()) + safeMargin;
        double maxX = Math.max(randomSector.getFirstCorner().getPosX(), randomSector.getSecondCorner().getPosX()) - safeMargin;
        double minZ = Math.min(randomSector.getFirstCorner().getPosZ(), randomSector.getSecondCorner().getPosZ()) + safeMargin;
        double maxZ = Math.max(randomSector.getFirstCorner().getPosZ(), randomSector.getSecondCorner().getPosZ()) - safeMargin;

        double x = minX + ThreadLocalRandom.current().nextDouble(maxX - minX);
        double z = minZ + ThreadLocalRandom.current().nextDouble(maxZ - minZ);
        int y = world.getHighestBlockYAt((int) x, (int) z) + 1;

        Location loc = new Location(world, x, y, z);
        user.setX(x);
        user.setY(y);
        user.setZ(z);
        user.setYaw(loc.getYaw());
        user.setPitch(loc.getPitch());

        if (paperSector.getSectorManager().getCurrentSector() != null && paperSector.getSectorManager().getCurrentSector().getName().equals(user.getSectorName())) {
            player.teleport(loc);
            user.updateAndSave(player,randomSector);
        } else {
            paperSector.getSectorTeleportService().teleportToSector(player, user, randomSector, false, true);
        }
        return loc;
    }

    public Sector getBalancedRandomSpawnSector() {
        List<Sector> onlineSpawns = new ArrayList<>(sectors.values().stream()
                .filter(s -> s.getType() == SectorType.SPAWN)
                .filter(Sector::isOnline)
                .filter(s -> s.getTPS() > 0)
                .sorted(Comparator.comparingDouble(
                        s -> ((double) s.getPlayerCount() / Math.max(s.getMaxPlayers(), 1)) / s.getTPS()
                ))
                .toList());

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

