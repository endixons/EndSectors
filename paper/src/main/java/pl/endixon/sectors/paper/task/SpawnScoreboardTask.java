package pl.endixon.sectors.paper.task;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import pl.endixon.sectors.paper.sector.Sector;
import pl.endixon.sectors.paper.sector.SectorManager;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.ArrayList;
import java.util.List;

public class SpawnScoreboardTask extends BukkitRunnable {

    private final SectorManager sectorManager;

    public SpawnScoreboardTask(SectorManager sectorManager) {
        this.sectorManager = sectorManager;
    }

    @Override
    public void run() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();

        for (Player player : Bukkit.getOnlinePlayers()) {
            Sector sector = sectorManager.getCurrentSector();
            if (sector == null) continue;

            boolean isAdmin = player.hasPermission("sectors.admin");

            String sectorTypeIcon = switch (sector.getType()) {
                case NETHER -> "🔥 Nether";
                case END -> "🌌 End";
                case SPAWN -> "🏰 Spawn";
                default -> "❓ " + sector.getType().name();
            };

            List<String> lines = new ArrayList<>();
            lines.add("§a ");
            lines.add("§a📍 Sektor: §f" + sector.getName());
            lines.add("§e👤 Nick: §f" + player.getName());
            lines.add("§a ");


            lines.add("§b⚡ TPS: §f" + sector.getTPSColored());
            lines.add("§c🟢 Online: §f" + sector.getPlayerCount());
            lines.add("§a ");

            if (isAdmin) {
                double cpuLoad = getSystemCpuLoad(osBean);
                long freeMem = Runtime.getRuntime().freeMemory() / 1024 / 1024;
                long maxMem = Runtime.getRuntime().maxMemory() / 1024 / 1024;

                lines.add("§b📶 Ping: §f" + player.getPing() + "ms");
                lines.add("§d🖥 CPU: §f" + String.format("%.2f", cpuLoad * 100) + "%");
                lines.add("§5💾 RAM: §f" + freeMem + "MB / " + maxMem + "MB");
                lines.add("§a ");
            } else {
                lines.add("§a ");
            }
            lines.add("§7Znajdujesz się na kanale: §f" + sector.getName());
            lines.add("§7Aby zmienić kanał użyj /ch");

            sendSidebar(player, (isAdmin ? "🛡 " : "✨ ") + sectorTypeIcon + (isAdmin ? " 🛡" : " ✨"), lines);
        }
    }

    private void sendSidebar(Player player, String title, List<String> lines) {
        var board = Bukkit.getScoreboardManager().getNewScoreboard();
        var obj = board.registerNewObjective("spawnSB", "dummy", Component.text(title));
        obj.setDisplaySlot(org.bukkit.scoreboard.DisplaySlot.SIDEBAR);

        int score = lines.size();
        for (String line : lines) {
            obj.getScore(line).setScore(score--);
        }

        player.setScoreboard(board);
    }

    private double getSystemCpuLoad(OperatingSystemMXBean osBean) {
        try {
            var method = osBean.getClass().getMethod("getSystemCpuLoad");
            method.setAccessible(true);
            return (double) method.invoke(osBean);
        } catch (Exception e) {
            return 0.0;
        }
    }
}
