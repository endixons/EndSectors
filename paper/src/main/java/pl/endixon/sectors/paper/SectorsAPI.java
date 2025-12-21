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

package pl.endixon.sectors.paper;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import pl.endixon.sectors.paper.manager.SectorManager;
import pl.endixon.sectors.paper.sector.Sector;
import pl.endixon.sectors.paper.sector.SectorTeleport;
import pl.endixon.sectors.paper.user.profile.UserProfile;
import pl.endixon.sectors.paper.user.profile.UserProfileRepository;
import pl.endixon.sectors.paper.util.LoggerUtil;

public class SectorsAPI {

    private static SectorsAPI instance;

    private final PaperSector plugin;
    private final SectorManager sectorManager;
    private final SectorTeleport teleportService;

    public SectorsAPI(PaperSector plugin) {
        this.plugin = plugin;
        this.sectorManager = plugin.getSectorManager();
        this.teleportService = new SectorTeleport(plugin);
        instance = this;

        LoggerUtil.info("SectorsAPI zainicjowane");
    }

    public static SectorsAPI getInstance() {
        return instance;
    }

    public PaperSector getPaperSector() {
        return this.plugin;
    }

    public Location getRandomLocation(@NonNull Player player, @NonNull UserProfile user) {
        return sectorManager.randomLocation(player, user);
    }

    public void teleportPlayer(Player player, UserProfile user, Sector sector, boolean force, boolean preserveCoordinates) {
        teleportService.teleportToSector(player, user, sector, force, preserveCoordinates);
    }

    public Optional<UserProfile> getUser(Player player) {
        return UserProfileRepository.getUser(player);
    }

    public SectorManager getSectorManager() {
        return this.sectorManager;
    }

    public CompletableFuture<Optional<UserProfile>> getUserAsync(String name) {
        return UserProfileRepository.getUserAsync(name);
    }
}
