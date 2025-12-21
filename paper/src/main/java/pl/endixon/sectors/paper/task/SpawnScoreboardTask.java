package pl.endixon.sectors.paper.task;

import fr.mrmicky.fastboard.adventure.FastBoard;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import pl.endixon.sectors.common.sector.SectorType;
import pl.endixon.sectors.paper.config.ConfigLoader;
import pl.endixon.sectors.paper.manager.SectorManager;
import pl.endixon.sectors.paper.sector.Sector;
import pl.endixon.sectors.paper.util.ChatAdventureUtil;

import com.sun.management.OperatingSystemMXBean;
import pl.endixon.sectors.paper.util.CpuUtil;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SpawnScoreboardTask extends BukkitRunnable {

    private final SectorManager sectorManager;
    private final ConfigLoader config;
    private final Map<UUID, FastBoard> boards = new HashMap<>();

    private final ChatAdventureUtil chatUtil = new ChatAdventureUtil();

    public SpawnScoreboardTask(SectorManager sectorManager, ConfigLoader config) {
        this.sectorManager = sectorManager;
        this.config = config;
    }
    @Override
    public void run() {

        for (Player player : Bukkit.getOnlinePlayers()) {
            Sector current = sectorManager.getCurrentSector();

            if (current.getType() != SectorType.SPAWN && current.getType() != SectorType.NETHER && current.getType() != SectorType.END) {
                removeBoard(player);
                continue;
            }

            UUID uuid = player.getUniqueId();

            FastBoard oldBoard = boards.remove(uuid);
            if (oldBoard != null) {
                oldBoard.delete();
            }

            FastBoard board = new FastBoard(player);
            boards.put(uuid, board);
            String scoreboardKey = player.hasPermission("endsectors.admin") ? "ADMIN" : current.getType().name();
            List<String> rawLines = config.scoreboard.getOrDefault(scoreboardKey, List.of());
            List<Component> parsedLines = rawLines.stream().map(line -> parseLine(line, player, current)).toList();

            String rawTitle = config.sectorTitles.getOrDefault(scoreboardKey, config.sectorTitles.get("DEFAULT").replace("{sectorType}", current.getType().name()));
            Component title = chatUtil.toComponent(rawTitle);
            board.updateTitle(title);
            board.updateLines(parsedLines);
        }
    }



    private Component parseLine(String line, Player player, Sector sector) {
        double cpuLoad = CpuUtil.getProcessCpuLoad();
        long freeMem = Runtime.getRuntime().freeMemory() / 1024 / 1024;
        long maxMem = Runtime.getRuntime().maxMemory() / 1024 / 1024;
        String cpuText = cpuLoad < 0 ? "N/A" : String.format("%.2f", cpuLoad * 100);
        String replaced = line
                .replace("{playerName}", player.getName())
                .replace("{sectorName}", sector.getName())
                .replace("{tps}", sector.getTPSColored())
                .replace("{onlineCount}", String.valueOf(sector.getPlayerCount()))
                .replace("{ping}", String.valueOf(player.getPing()))
                .replace("{cpu}", cpuText)
                .replace("{freeRam}", String.valueOf(freeMem))
                .replace("{maxRam}", String.valueOf(maxMem));

        return chatUtil.toComponent(replaced);
    }


    private void removeBoard(Player player) {
        FastBoard board = boards.remove(player.getUniqueId());
        if (board != null) {
            board.delete();

        }
    }

}
