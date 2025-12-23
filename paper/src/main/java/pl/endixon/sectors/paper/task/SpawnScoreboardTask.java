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
import pl.endixon.sectors.paper.util.CpuUtil;

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

            if (current == null || (current.getType() != SectorType.SPAWN && current.getType() != SectorType.NETHER && current.getType() != SectorType.END)) {
                removeBoard(player);
                continue;
            }
            FastBoard board = boards.computeIfAbsent(player.getUniqueId(), k -> new FastBoard(player));
            String scoreboardKey = player.hasPermission("endsectors.admin") ? "ADMIN" : current.getType().name();
            String rawTitle = config.sectorTitles.getOrDefault(scoreboardKey, config.sectorTitles.getOrDefault("DEFAULT", "<red>EndSectors").replace("{sectorType}", current.getType().name()));
            board.updateTitle(chatUtil.toComponent(rawTitle));
            List<String> rawLines = config.scoreboard.getOrDefault(scoreboardKey, List.of());
            List<Component> parsedLines = rawLines.stream().map(line -> parseLine(line, player, current)).toList();
            board.updateLines(parsedLines);
        }

        boards.keySet().removeIf(uuid -> Bukkit.getPlayer(uuid) == null);
    }

    private Component parseLine(String line, Player player, Sector sector) {
        double cpuLoad = CpuUtil.getCpuLoad();

        long totalMemory = Runtime.getRuntime().totalMemory() / 1024 / 1024;
        long freeMemory = Runtime.getRuntime().freeMemory() / 1024 / 1024;
        long maxMemory = Runtime.getRuntime().maxMemory() / 1024 / 1024;
        long usedMemory = totalMemory - freeMemory;


        String replaced = line
                .replace("{playerName}", player.getName())
                .replace("{sectorName}", sector.getName())
                .replace("{tps}", sector.getTPSColored())
                .replace("{onlineCount}", String.valueOf(sector.getPlayerCount()))
                .replace("{ping}", String.valueOf(player.getPing()))
                .replace("{cpu}", String.valueOf(cpuLoad))
                .replace("{usedRam}", String.valueOf(usedMemory))
                .replace("{freeRam}", String.valueOf(freeMemory))
                .replace("{maxRam}", String.valueOf(maxMemory));

        return chatUtil.toComponent(replaced);
    }

    private void removeBoard(Player player) {
        FastBoard board = boards.remove(player.getUniqueId());
        if (board != null && !board.isDeleted()) {
            board.delete();
        }
    }
}