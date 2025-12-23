package pl.endixon.sectors.paper.task;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import pl.endixon.sectors.common.sector.SectorType;
import pl.endixon.sectors.paper.PaperSector;
import pl.endixon.sectors.paper.manager.SectorManager;
import pl.endixon.sectors.paper.sector.Sector;
import pl.endixon.sectors.paper.util.MessagesUtil;

public class BorderActionBarTask extends BukkitRunnable {

    private final PaperSector paperSector;
    private final Map<UUID, BossBar> bossBars = new HashMap<>();

    public BorderActionBarTask(PaperSector paperSector) {
        this.paperSector = paperSector;
    }

    @Override
    public void run() {
        SectorManager sectorManager = this.paperSector.getSectorManager();
        Sector currentSector = sectorManager.getCurrentSector();

        if (currentSector == null) {
            return;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            int borderDistance = currentSector.getBorderDistance(player.getLocation());
            Sector nearestSector = currentSector.getNearestSector(player.getLocation());

            if (nearestSector == null || borderDistance > this.paperSector.getConfiguration().borderMessageDistance) {
                this.removeBossBar(player);
                continue;
            }

            String displayName = nearestSector.getType() == SectorType.SPAWN ? "spawn" : nearestSector.getName();
            String distanceStr = String.valueOf(borderDistance);

            player.sendActionBar(MessagesUtil.BORDER_MESSAGE.get(
                    "{SECTOR}", displayName,
                    "{DISTANCE}", distanceStr
            ));

            String legacyMessage = MessagesUtil.BORDER_MESSAGE.getText(
                    "{SECTOR}", displayName,
                    "{DISTANCE}", distanceStr
            );

            double progress = 1.0 - ((double) borderDistance / this.paperSector.getConfiguration().borderMessageDistance);
            progress = Math.max(0.0, Math.min(1.0, progress));

            BossBar bossBar = this.bossBars.computeIfAbsent(player.getUniqueId(), uuid -> {
                BossBar newBar = Bukkit.createBossBar(legacyMessage, BarColor.GREEN, BarStyle.SOLID);
                newBar.addPlayer(player);
                return newBar;
            });

            this.updateBarStyle(bossBar, progress);

            bossBar.setTitle(legacyMessage);
            bossBar.setProgress(progress);
            bossBar.setVisible(true);
        }
    }

    private void updateBarStyle(BossBar bar, double progress) {
        if (progress < 0.4) {
            bar.setColor(BarColor.GREEN);
            bar.setStyle(BarStyle.SOLID);
        } else if (progress < 0.7) {
            bar.setColor(BarColor.YELLOW);
            bar.setStyle(BarStyle.SEGMENTED_10);
        } else {
            bar.setColor(BarColor.RED);
            bar.setStyle(BarStyle.SEGMENTED_20);
        }
    }

    private void removeBossBar(Player player) {
        BossBar bar = this.bossBars.remove(player.getUniqueId());
        if (bar != null) {
            bar.removePlayer(player);
        }
    }
}