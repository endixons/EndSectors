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

package pl.endixon.sectors.tools.task;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import pl.endixon.sectors.common.util.ChatUtil;
import pl.endixon.sectors.tools.manager.CombatManager;

public class CombatTask implements Runnable {

    private static final Map<Player, CombatTask> activeTasks = new HashMap<>();
    private final JavaPlugin plugin;
    private final CombatManager combatManager;
    private final Player player;
    private int timeLeft = 30;
    private BossBar bossBar;
    private int taskId;

    public CombatTask(JavaPlugin plugin, CombatManager combatManager, Player player) {
        this.plugin = plugin;
        this.combatManager = combatManager;
        this.player = player;

        if (activeTasks.containsKey(player)) {
            CombatTask existing = activeTasks.get(player);
            existing.resetTime();
            return;
        }
        activeTasks.put(player, this);
        createBossBar();
    }

    private void createBossBar() {
        bossBar = Bukkit.createBossBar(ChatUtil.fixHexColors("&#ff5555Jesteś podczas walki, &#f5c542pozostało " + timeLeft + "s"), BarColor.RED, BarStyle.SEGMENTED_10);
        bossBar.addPlayer(player);
        bossBar.setProgress(1.0);
    }

    private void resetTime() {
        this.timeLeft = 30;
    }

    @Override
    public void run() {
        if (!combatManager.isInCombat(player) || timeLeft <= 0) {
            if (bossBar != null)
                bossBar.removeAll();
            combatManager.endCombat(player);
            activeTasks.remove(player);
            Bukkit.getScheduler().cancelTask(taskId);
            return;
        }

        if (bossBar != null) {
            bossBar.setProgress(timeLeft / 30.0);
            bossBar.setTitle(ChatUtil.fixHexColors("&#ff5555Jesteś podczas walki, &#f5c542pozostało " + timeLeft + "s"));
        }
        timeLeft--;
    }

    public void start() {
        if (activeTasks.get(player) != this) {
            return;
        }

        taskId = Bukkit.getScheduler().runTaskTimer(plugin, this, 0L, 20L).getTaskId();
    }
}
