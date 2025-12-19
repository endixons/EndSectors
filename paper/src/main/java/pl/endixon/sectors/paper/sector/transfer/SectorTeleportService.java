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
    import pl.endixon.sectors.paper.user.RedisUserCache;
    import pl.endixon.sectors.paper.user.UserRedis;
    import pl.endixon.sectors.paper.util.Logger;

    import java.util.Optional;
    import java.util.concurrent.CompletableFuture;

    public class SectorTeleportService {

        private final PaperSector plugin;

        public SectorTeleportService(PaperSector plugin) {
            this.plugin = plugin;
        }

        public void teleportToSector(Player player, UserRedis user, Sector sector, boolean forceTransfer, boolean preserveCoordinates) {
            long startTime = System.currentTimeMillis();

            SectorManager sectorManager = plugin.getSectorManager();
            Sector current = sectorManager.getCurrentSector();

            if (!forceTransfer && current != null && current.getType() == SectorType.SPAWN && sector.getType() == SectorType.SPAWN) {
                Logger.info(() -> String.format("[Transfer] Blocked spawn-to-spawn transfer for %s", player.getName()));
                return;
            }

            Logger.info(() -> String.format("[Transfer] Starting teleport for player %s -> %s", player.getName(), sector.getName()));

            Bukkit.getScheduler().runTask(plugin, () -> {
                SectorChangeEvent event = new SectorChangeEvent(player, sector);
                Bukkit.getPluginManager().callEvent(event);

                if (event.isCancelled()) {
                    Logger.info(() -> String.format("[Transfer] Teleport cancelled by event for %s", player.getName()));
                    return;
                }

                if (player.isInsideVehicle()) {
                    Logger.info(() -> String.format("[Transfer] Removing vehicle for %s", player.getName()));
                    player.leaveVehicle();
                }

                Logger.info(() -> String.format("[Transfer] Updating player data for %s", player.getName()));

                if (!preserveCoordinates) {
                    user.updateAndSave(player, sector);

                } else {
                    user.setSectorName(sector.getName());
                    RedisUserCache.save(user);
                }

                Logger.info(() -> String.format("[Transfer] Sending teleport packet for %s", player.getName()));
                PacketRequestTeleportSector packet = new PacketRequestTeleportSector(player.getName(), sector.getName());
                PaperSector.getInstance().getRedisService().publish(PacketChannel.PACKET_TELEPORT_TO_SECTOR, packet);

                long duration = System.currentTimeMillis() - startTime;
                Logger.info(() -> String.format("[Transfer] Teleport process finished for %s (ms: %d)", player.getName(), duration));
            });
        }

    }
