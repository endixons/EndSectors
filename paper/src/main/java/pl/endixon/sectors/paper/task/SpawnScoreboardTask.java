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

import com.sun.management.OperatingSystemMXBean;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import pl.endixon.sectors.common.util.ChatUtil;
import pl.endixon.sectors.paper.config.ConfigLoader;
import pl.endixon.sectors.paper.manager.SectorManager;
import pl.endixon.sectors.paper.sector.Sector;
import pl.endixon.sectors.paper.util.ChatAdventureUtil;

public class SpawnScoreboardTask extends BukkitRunnable {

    private final SectorManager sectorManager;
    private final ConfigLoader config;
    private final ChatAdventureUtil CHAT = new ChatAdventureUtil();

    public SpawnScoreboardTask(SectorManager sectorManager, ConfigLoader config) {
        this.sectorManager = sectorManager;
        this.config = config;
    }

    @Override
    public void run() {
        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        for (Player player : Bukkit.getOnlinePlayers()) {
            Sector sector = sectorManager.getCurrentSector();
            if (sector == null)
                continue;

            boolean isAdmin = player.hasPermission("sectors.admin");

            List<String> lines = new ArrayList<>(config.scoreboard.getOrDefault(sector.getType().name(), new ArrayList<>()));
            if (isAdmin) {
                lines.addAll(config.scoreboard.getOrDefault("ADMIN", new ArrayList<>()));
            }

            List<Component> parsedLines = new ArrayList<>();
            for (String line : lines) {
                parsedLines.add(parseLine(line, player, sector, osBean));
            }

            String title = getTitle(sector, isAdmin);
            sendSidebar(player, title, parsedLines);
        }
    }

    private Component parseLine(String line, Player player, Sector sector, OperatingSystemMXBean osBean) {
        double cpuLoad = getSystemCpuLoad();
        long freeMem = Runtime.getRuntime().freeMemory() / 1024 / 1024;
        long maxMem = Runtime.getRuntime().maxMemory() / 1024 / 1024;

        String cpuText = cpuLoad < 0 ? "N/A" : String.format("%.2f", cpuLoad * 100);

        String parsed = line.replace("{playerName}", player.getName()).replace("{sectorName}", sector.getName()).replace("{tps}", String.valueOf(sector.getTPSColored())).replace("{onlineCount}", String.valueOf(sector.getPlayerCount())).replace("{ping}", String.valueOf(player.getPing())).replace("{cpu}", cpuText).replace("{freeRam}", String.valueOf(freeMem)).replace("{maxRam}", String.valueOf(maxMem));

        return CHAT.toComponent(parsed);
    }

    private String getTitle(Sector sector, boolean isAdmin) {
        String icon = config.sectorTitles.getOrDefault(sector.getType().name(), config.sectorTitles.get("DEFAULT").replace("{sectorType}", sector.getType().name()));

        String prefix = isAdmin ? config.adminTitlePrefix : config.playerTitlePrefix;
        String suffix = isAdmin ? config.adminTitleSuffix : config.playerTitleSuffix;

        return prefix + icon + suffix;
    }

    private void sendSidebar(Player player, String title, List<Component> lines) {
        var board = Bukkit.getScoreboardManager().getNewScoreboard();

        var obj = board.registerNewObjective("spawnSB", "dummy", CHAT.toComponent(title));
        obj.setDisplaySlot(org.bukkit.scoreboard.DisplaySlot.SIDEBAR);

        int score = lines.size();
        for (Component line : lines) {
            obj.getScore(ChatUtil.fixHexColors(line.toString())).setScore(score--);
        }
        player.setScoreboard(board);
    }

    public static double getSystemCpuLoad() {
        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        double load = osBean.getCpuLoad();
        return load < 0 ? 0 : load * 100;
    }
}
