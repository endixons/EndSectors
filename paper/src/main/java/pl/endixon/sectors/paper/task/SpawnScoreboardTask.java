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

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import pl.endixon.sectors.common.sector.SectorType;
import pl.endixon.sectors.paper.sector.Sector;
import pl.endixon.sectors.paper.sector.SectorManager;

import java.util.HashMap;
import java.util.Map;

public class SpawnScoreboardTask extends BukkitRunnable {

    private final SectorManager sectorManager;
    private final Map<Player, Scoreboard> boards = new HashMap<>();

    public SpawnScoreboardTask(SectorManager sectorManager) {
        this.sectorManager = sectorManager;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Sector sector = sectorManager.getCurrentSector();
            if (sector == null) continue;

            if (sector.getType() == SectorType.SPAWN) {
                Scoreboard board = boards.computeIfAbsent(player, p -> {
                    Scoreboard b = Bukkit.getScoreboardManager().getNewScoreboard();
                    Objective obj = b.registerNewObjective("spawnSB", "dummy", ChatColor.GOLD + "SEKTOR INFO");
                    obj.setDisplaySlot(DisplaySlot.SIDEBAR);
                    return b;
                });
                Objective obj = board.getObjective("spawnSB");
                if (obj == null) {
                    obj = board.registerNewObjective("spawnSB", "dummy", ChatColor.GOLD + "SEKTOR INFO");
                    obj.setDisplaySlot(DisplaySlot.SIDEBAR);
                }
                String tpsComponent = sector.getTPSColored();

                setScore(obj, 4, ChatColor.GREEN + "SEKTOR: " + ChatColor.WHITE + sector.getName());
                setScore(obj, 3, ChatColor.YELLOW + "NICK: " + ChatColor.WHITE + player.getName());
                setScore(obj, 2, ChatColor.AQUA + "TPS: " + tpsComponent);
                setScore(obj, 1, ChatColor.RED + "ONLINE: " + ChatColor.WHITE + sector.getPlayerCount());
                player.setScoreboard(board);
            } else {
                if (boards.containsKey(player)) {
                    player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
                    boards.remove(player);
                }
            }
        }
    }

    private void setScore(Objective obj, int scoreValue, String line) {
        for (String s : obj.getScoreboard().getEntries()) {
            if (obj.getScore(s).getScore() == scoreValue) {
                obj.getScoreboard().resetScores(s);
            }
        }
        obj.getScore(line).setScore(scoreValue);
    }
}

