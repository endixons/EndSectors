package pl.endixon.sectors.paper.sector.transfer;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import pl.endixon.sectors.common.packet.PacketChannel;
import pl.endixon.sectors.common.packet.object.PacketRequestTeleportSector;
import pl.endixon.sectors.common.sector.SectorType;
import pl.endixon.sectors.paper.PaperSector;
import pl.endixon.sectors.paper.event.sector.SectorChangeEvent;
import pl.endixon.sectors.paper.sector.Sector;
import pl.endixon.sectors.paper.sector.SectorManager;
import pl.endixon.sectors.paper.user.UserRedis;
import pl.endixon.sectors.paper.util.Logger;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class SectorTeleportService {

    private final PaperSector plugin;

    public SectorTeleportService(PaperSector plugin) {
        this.plugin = plugin;
    }

    public void teleportToSector(Player player, UserRedis user, Sector sector, boolean forceTransfer) {
        SectorManager sectorManager = plugin.getSectorManager();

        boolean blockSpawnTransfer = Optional.ofNullable(sectorManager.getCurrentSector())
                .map(current -> current.getType() == SectorType.SPAWN)
                .orElse(false) && sector.getType() == SectorType.SPAWN;

        if (blockSpawnTransfer && !forceTransfer) {
            Logger.info(() -> "[Transfer] Blocked spawn-to-spawn transfer for " + player.getName());
            return;
        }

        Logger.info(() -> "[Transfer] Starting connection for player " + player.getName() + " -> " + sector.getName());

        plugin.getServer().getScheduler().runTask(plugin, () -> {

            SectorChangeEvent event = new SectorChangeEvent(player, sector);
            Bukkit.getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                Logger.info(() -> "[Transfer] Cancelled by event for " + player.getName());
                return;
            }

            if (player.isInsideVehicle()) {
                Logger.info(() -> "[Transfer] Removing vehicle for " + player.getName());
                player.leaveVehicle();
            }

            CompletableFuture.runAsync(() -> {
                Logger.info(() -> "[Transfer] Updating player data for " + player.getName());
                user.updateAndSave(player, sector);

                Logger.info(() -> "[Transfer] Sending teleport request for " + player.getName());
                PacketRequestTeleportSector packet = new PacketRequestTeleportSector(player.getName(), sector.getName());
                PaperSector.getInstance().getRedisService().publish(PacketChannel.PACKET_TELEPORT_TO_SECTOR, packet);

            }).thenRun(() -> {
                plugin.getServer().getScheduler().runTask(plugin,
                        () -> Logger.info(() -> "[Transfer] Finished for " + player.getName()));
            });
        });
    }
}
