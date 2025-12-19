package pl.endixon.sectors.paper;

import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import pl.endixon.sectors.common.sector.SectorData;
import pl.endixon.sectors.common.sector.SectorType;
import pl.endixon.sectors.common.util.Corner;
import pl.endixon.sectors.paper.sector.Sector;
import pl.endixon.sectors.paper.sector.SectorManager;
import pl.endixon.sectors.paper.sector.transfer.SectorTeleportService;
import pl.endixon.sectors.paper.user.RedisUserCache;
import pl.endixon.sectors.paper.user.UserManager;
import pl.endixon.sectors.paper.user.UserRedis;
import pl.endixon.sectors.paper.util.Logger;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class SectorsAPI {

    private static SectorsAPI instance;

    private final PaperSector plugin;
    private final SectorManager sectorManager;
    private final SectorTeleportService teleportService;

    public SectorsAPI(PaperSector plugin) {
        this.plugin = plugin;
        this.sectorManager = plugin.getSectorManager();
        this.teleportService = new SectorTeleportService(plugin);
        instance = this;
        Logger.info("SectorsAPI zainicjowane");
    }

    public static SectorsAPI getInstance() {
        return instance;
    }

    public PaperSector getPaperSector() {
        return this.plugin;
    }


    public Location getRandomLocation(@NonNull Player player, @NonNull UserRedis user) {
        return sectorManager.randomLocation(player,user);
    }

    public void teleportPlayer(Player player, UserRedis user, Sector sector, boolean force, boolean preserveCoordinates) {
        teleportService.teleportToSector(player, user, sector, force,preserveCoordinates);
    }


    public Optional<UserRedis> getUser(Player player) {
        return UserManager.getUser(player);
    }

    public SectorManager getSectorManager() {
        return this.sectorManager;
    }


    public CompletableFuture<Optional<UserRedis>> getUserAsync(String name) {
        return UserManager.getUserAsync(name);
    }

}
