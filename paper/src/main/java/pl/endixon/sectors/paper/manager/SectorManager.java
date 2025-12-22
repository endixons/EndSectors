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

package pl.endixon.sectors.paper.manager;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import pl.endixon.sectors.common.sector.SectorData;
import pl.endixon.sectors.common.sector.SectorType;
import pl.endixon.sectors.paper.PaperSector;
import pl.endixon.sectors.paper.sector.Sector;
import pl.endixon.sectors.paper.user.profile.UserProfile;
import pl.endixon.sectors.paper.user.profile.UserProfileCache;
import pl.endixon.sectors.paper.util.LoggerUtil;

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
                if (sector.getType() == SectorType.QUEUE)
                    continue;
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

        List<Sector> candidates = new ArrayList<>(
                sectors.values().stream()
                        .filter(Sector::isOnline)
                        .filter(s -> s.getType() != SectorType.SPAWN)
                        .filter(s -> s.getType() != SectorType.QUEUE)
                        .filter(s -> s.getType() != SectorType.NETHER)
                        .filter(s -> s.getType() != SectorType.END)
                        .filter(s -> !Sector.isSectorFull(s))
                        .toList()
        );

        if (candidates.isEmpty()) {
            throw new IllegalStateException("Brak dostępnych NIEPEŁNYCH sektorów do RTP");
        }

        Collections.shuffle(candidates);

        candidates.sort(Comparator.comparingDouble(s -> {
            double occupancy = (double) s.getPlayerCount() / Math.max(s.getMaxPlayers(), 1);
            return occupancy / Math.max(s.getTPS(), 1.0);
        }));

        LoggerUtil.info(String.format(
                "[RTP] Candidates: %d",
                candidates.size()
        ));

        for (Sector s : candidates) {
            LoggerUtil.info(String.format(
                    "[RTP] Candidate -> %s | Players: %d/%d | TPS: %.2f",
                    s.getName(),
                    s.getPlayerCount(),
                    s.getMaxPlayers(),
                    s.getTPS()
            ));
        }

        int poolSize = Math.max(1, (int) Math.ceil(candidates.size() * 0.5));

        LoggerUtil.info(String.format(
                "[RTP] Pool size: %d (top 50%%)",
                poolSize
        ));

        int index = ThreadLocalRandom.current().nextInt(poolSize);
        Sector chosen = candidates.get(index);

        LoggerUtil.info(String.format(
                "[RTP] Chosen sector: %s | Players: %d/%d | TPS: %.2f | Index: %d/%d",
                chosen.getName(),
                chosen.getPlayerCount(),
                chosen.getMaxPlayers(),
                chosen.getTPS(),
                index,
                poolSize
        ));

        return chosen;
    }





    public Location randomLocation(@NonNull Player player, @NonNull UserProfile user) {

        Sector sector = getRandomSector();
        World world = Bukkit.getWorld(sector.getWorldName());
        if (world == null) {
            throw new IllegalStateException("World not loaded for sector " + sector.getName());
        }

        int margin = 30;

        int minX = Math.min(sector.getFirstCorner().getPosX(), sector.getSecondCorner().getPosX()) + margin;
        int maxX = Math.max(sector.getFirstCorner().getPosX(), sector.getSecondCorner().getPosX()) - margin;
        int minZ = Math.min(sector.getFirstCorner().getPosZ(), sector.getSecondCorner().getPosZ()) + margin;
        int maxZ = Math.max(sector.getFirstCorner().getPosZ(), sector.getSecondCorner().getPosZ()) - margin;

        if (minX >= maxX || minZ >= maxZ) {
            throw new IllegalStateException("Sektor " + sector.getName() + " jest za mały na RTP");
        }

        Location loc = null;

        for (int i = 0; i < 10; i++) {
            int x = ThreadLocalRandom.current().nextInt(minX, maxX);
            int z = ThreadLocalRandom.current().nextInt(minZ, maxZ);
            int y = findSafeY(world, x, z);

            Location test = new Location(world, x + 0.5, y, z + 0.5);
            LoggerUtil.info(String.format("[RTP] Trying location: X=%d Z=%d Y=%d", x, z, y));

            if (sector.isInSector(test)) {
                loc = test;
                break;
            }
        }

        if (loc == null) {
            throw new IllegalStateException("Nie udało się znaleźć bezpiecznej lokalizacji RTP w sektorze " + sector.getName());
        }

        user.setLocationAndSave(loc);
         user.setTransferOffsetUntil(0);

        if (sector.getName().equals(user.getSectorName())) {
            player.teleport(loc);
            user.updateAndSave(player, sector, false);
        } else {
            paperSector.getSectorTeleport().teleportToSector(player, user, sector, false, true);
        }

        return loc;
    }

    private int findSafeY(World world, int x, int z) {
        int maxY = Math.min(150, world.getMaxHeight() - 2);
        int y = world.getHighestBlockYAt(x, z);
        while (y <= maxY) {
            Block base = world.getBlockAt(x, y, z);
            Block above = world.getBlockAt(x, y + 1, z);
            Block above2 = world.getBlockAt(x, y + 2, z);
            if (base.getType().isSolid() && above.isPassable() && above2.isPassable()) {
                return y + 1;
            }
            y++;
        }
        return maxY;
    }



    public Sector getBalancedRandomSpawnSector() {

        List<Sector> allSpawns = sectors.values().stream()
                .filter(s -> s.getType() == SectorType.SPAWN)
                .toList();

        List<Sector> healthySpawns = new ArrayList<>(
                allSpawns.stream()
                        .filter(Sector::isOnline)
                        .filter(s -> s.getTPS() > 15.0)
                        .toList()
        );

        LoggerUtil.info(String.format(
                "All spawns: %d | Healthy spawns: %d",
                allSpawns.size(), healthySpawns.size()
        ));

        if (healthySpawns.isEmpty()) {
            LoggerUtil.info(String.format(
                    "Balance error! All spawns (%d) are either offline or lagging!",
                    allSpawns.size()
            ));
            return null;
        }


        Collections.shuffle(healthySpawns);

        healthySpawns.sort(Comparator.comparingDouble(s -> {
            double occupancy = (double) s.getPlayerCount() / Math.max(s.getMaxPlayers(), 1);
            return occupancy / s.getTPS();
        }));

        int poolSize = Math.max(1, (int) Math.ceil(healthySpawns.size() * 0.3));

        LoggerUtil.info(String.format(
                "Spawn balance: Online: %d/%d | Random selection from top %d%% (~%d spawns)",
                healthySpawns.size(), allSpawns.size(), 30, poolSize
        ));

        Sector chosen = healthySpawns.get(
                ThreadLocalRandom.current().nextInt(poolSize)
        );

        LoggerUtil.info(String.format(
                "Chosen spawn: %s | Players: %d/%d | TPS: %.2f",
                chosen.getName(),
                chosen.getPlayerCount(),
                chosen.getMaxPlayers(),
                chosen.getTPS()
        ));

        return chosen;
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
