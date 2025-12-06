    package pl.endixon.sectors.paper.listener.player;

    import lombok.RequiredArgsConstructor;
    import net.kyori.adventure.text.Component;
    import net.kyori.adventure.title.Title;
    import org.bukkit.Bukkit;
    import org.bukkit.Material;
    import org.bukkit.entity.Player;
    import org.bukkit.event.EventHandler;
    import org.bukkit.event.Listener;
    import org.bukkit.event.player.PlayerPortalEvent;
    import pl.endixon.sectors.common.util.ChatUtil;
    import pl.endixon.sectors.paper.PaperSector;
    import pl.endixon.sectors.paper.event.sector.SectorChangeEvent;
    import pl.endixon.sectors.paper.sector.Sector;
    import pl.endixon.sectors.paper.sector.SectorManager;
    import pl.endixon.sectors.paper.user.UserManager;
    import pl.endixon.sectors.paper.user.UserMongo;
    import pl.endixon.sectors.common.sector.SectorType;
    import pl.endixon.sectors.paper.util.Configuration;
    import pl.endixon.sectors.paper.util.Logger;

    import java.time.Duration;

    @RequiredArgsConstructor
    public class PortalListener implements Listener {

        private final PaperSector paperSector;

        @EventHandler
        public void onPortalEnter(PlayerPortalEvent event) {
            Player player = event.getPlayer();
            event.setCancelled(true);
            player.setPortalCooldown(0);
            player.resetCooldown();
                UserManager.getUser(player.getName()).thenAccept(userMongo -> {
                    if (userMongo == null) return;

                    Bukkit.getScheduler().runTask(paperSector, () -> {
                        SectorManager sectorManager = paperSector.getSectorManager();
                        Sector current = sectorManager.getCurrentSector();
                        if (current == null) return;

                        if (System.currentTimeMillis() - userMongo.getLastTransferTimestamp() < 5000L) return;
                        if (System.currentTimeMillis() - userMongo.getLastSectorTransfer() < 5000L) return;

                        userMongo.setLastSectorTransfer(true);
                        Sector targetSector;

                        try {
                            if (current.getType() == SectorType.SPAWN) {
                                targetSector = sectorManager.getSector("nether01");
                            } else if (current.getType() == SectorType.NETHER) {
                                targetSector = sectorManager.getBalancedRandomSpawnSector();
                            } else {
                                return;
                            }
                        } catch (IllegalStateException e) {
                            Logger.info("Could not find a valid sector to transfer the player!");

                            return;
                        }

                        if (targetSector == null || !targetSector.isOnline()) {
                            player.showTitle(Title.title(
                                    Component.text(ChatUtil.fixColors(Configuration.SECTOR_DISABLED_TITLE)),
                                    Component.text(ChatUtil.fixColors(Configuration.SECTOR_DISABLED_SUBTITLE)),
                                    Title.Times.times(
                                            Duration.ofMillis(500),
                                            Duration.ofMillis(2000),
                                            Duration.ofMillis(500)
                                    )
                            ));
                            current.knockBorder(player, 1.5);
                            return;
                        }

                        SectorChangeEvent ev = new SectorChangeEvent(player, targetSector);
                        Bukkit.getPluginManager().callEvent(ev);
                        if (ev.isCancelled()) return;

                        Bukkit.getScheduler().runTaskLater(paperSector,
                                () -> paperSector.getSectorTeleportService().teleportToSector(player, userMongo, targetSector, false),
                                1L);
                    });
                });

        }
    }
