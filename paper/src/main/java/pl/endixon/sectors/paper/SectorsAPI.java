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

    public void callEvent(Event event) {
        if (event != null) {
            this.plugin.getServer().getPluginManager().callEvent(event);
        }
    }


    public Corner createCorner(int x, int y, int z) {
        return new Corner(x, y, z);
    }

    public Corner createCorner(int x, int z) {
        return new Corner(x, z);
    }

    public SectorData createSectorData(String name, Corner firstCorner, Corner secondCorner, String world, SectorType type) {
        return new SectorData(name, firstCorner, secondCorner, world, type);
    }

    public SectorType[] getSectorTypes() {
        return SectorType.values();
    }

    public void addSector(SectorData data) {
        sectorManager.addSector(data);
    }

    public void addSector(Sector sector) {
        sectorManager.addSector(sector);
    }

    public Sector getSector(String name) {
        return sectorManager.getSector(name);
    }

    public Sector getSector(Location location) {
        return sectorManager.getSector(location);
    }

    public Collection<Sector> getAllSectors() {
        return sectorManager.getSectors();
    }

    public Sector findSector(SectorType type) {
        return sectorManager.find(type);
    }

    public Location getRandomLocation(@NonNull Player player, @NonNull UserRedis user) {
        return sectorManager.randomLocation(player,user);
    }

    public Sector getBalancedSpawn() {
        return sectorManager.getBalancedRandomSpawnSector();
    }

    public Sector getCurrentSector() {
        return sectorManager.getCurrentSector();
    }

    public void teleportPlayer(Player player, UserRedis user, Sector sector, boolean force, boolean preserveCoordinates) {
        teleportService.teleportToSector(player, user, sector, force,preserveCoordinates);
    }

    public void getOnlinePlayers(Consumer<List<String>> callback) {
        sectorManager.getOnlinePlayers(callback);
    }

    public void isPlayerOnline(String playerName, Consumer<Boolean> callback) {
        sectorManager.isPlayerOnline(playerName, callback);
    }

    public boolean isSectorFull(Sector sector) {
        return Sector.isSectorFull(sector);
    }

    public long getBorderDistance(Sector sector, Location loc) {
        return sector.getBorderDistance(loc);
    }

    public void knockBorder(Sector sector, Player player, double power) {
        sector.knockBorder(player, power);
    }

    public Optional<UserRedis> getUser(Player player) {
        return UserManager.getUser(player);
    }

    public CompletableFuture<Optional<UserRedis>> getUserAsync(String name) {
        return UserManager.getUserAsync(name);
    }

    public CompletableFuture<UserRedis> getOrCreateUserAsync(String name) {
        return UserManager.getOrCreateAsync(name);
    }

    public CompletableFuture<UserRedis> getOrCreateUserAsync(Player player) {
        return UserManager.getOrCreateAsync(player);
    }


    public void activateTransferOffset(UserRedis user) {
        if (user != null) user.activateTransferOffset();
    }

    public void resetTransferOffset(UserRedis user) {
        if (user != null) {
            user.setTransferOffsetUntil(0);
            RedisUserCache.save(user);
        }
    }

    public long getTransferOffset(UserRedis user) {
        return user != null ? user.getTransferOffsetUntil() : 0L;
    }

    public void updateAndSave(UserRedis user, Player player) {
        if (user == null || player == null) return;
        user.updateAndSave(player, sectorManager.getCurrentSector());
    }
}
