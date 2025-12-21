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

package pl.endixon.sectors.paper.task;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import pl.endixon.sectors.common.sector.SectorType;
import pl.endixon.sectors.common.util.ChatUtil;
import pl.endixon.sectors.paper.PaperSector;
import pl.endixon.sectors.paper.manager.SectorManager;
import pl.endixon.sectors.paper.sector.Sector;
import pl.endixon.sectors.paper.util.ConfigurationUtil;

public class BorderActionBarTask extends BukkitRunnable {

    private final PaperSector paperSector;
    private final Map<Player, BossBar> bossBars = new HashMap<>();

    public BorderActionBarTask(PaperSector paperSector) {
        this.paperSector = paperSector;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            SectorManager sectorManager = paperSector.getSectorManager();
            Sector currentSector = sectorManager.getCurrentSector();
            if (currentSector == null)
                continue;

            int borderDistance = currentSector.getBorderDistance(player.getLocation());
            Sector nearestSector = currentSector.getNearestSector(player.getLocation());

            if (nearestSector == null || borderDistance > ConfigurationUtil.BORDER_MESSAGE_DISTANCE) {
                BossBar bar = bossBars.remove(player);
                if (bar != null)
                    bar.removePlayer(player);
                continue;
            }

            String displayName = nearestSector.getType() == SectorType.SPAWN ? "spawn" : nearestSector.getName();

            String message = ConfigurationUtil.BORDER_MESSAGE.replace("{SECTOR}", displayName).replace("{DISTANCE}", String.valueOf(borderDistance));

            player.sendActionBar(ChatUtil.fixHexColors(message));
            double progress = 1.0 - ((double) borderDistance / ConfigurationUtil.BORDER_MESSAGE_DISTANCE);
            progress = Math.max(0.0, Math.min(1.0, progress));

            BossBar bossBar = bossBars.computeIfAbsent(player, p -> {
                BossBar newBar = Bukkit.createBossBar((message), BarColor.GREEN, BarStyle.SOLID);
                newBar.addPlayer(player);
                return newBar;
            });

            if (progress > 0.66)
                bossBar.setColor(BarColor.GREEN);
            else if (progress > 0.33)
                bossBar.setColor(BarColor.YELLOW);
            else
                bossBar.setColor(BarColor.RED);

            bossBar.setTitle(ChatUtil.fixHexColors(message));
            bossBar.setProgress(progress);
            bossBar.setVisible(progress > 0);
        }
    }
}
