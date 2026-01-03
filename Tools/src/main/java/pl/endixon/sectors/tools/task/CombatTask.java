package pl.endixon.sectors.tools.task;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import pl.endixon.sectors.tools.manager.CombatManager;

public class CombatTask implements Runnable {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();
    private static final String TITLE_FORMAT = "<#ff5555>Jesteś podczas walki, <#f5c542>pozostało %ds";

    private final JavaPlugin plugin;
    private final CombatManager combatManager;
    private final Player player;
    private int timeLeft = 30;
    private BossBar bossBar;
    private int taskId = -1;

    public CombatTask(JavaPlugin plugin, CombatManager combatManager, Player player) {
        this.plugin = plugin;
        this.combatManager = combatManager;
        this.player = player;
        createBossBar();
    }

    private void createBossBar() {
        this.bossBar = Bukkit.createBossBar(render(30), BarColor.RED, BarStyle.SEGMENTED_10);
        this.bossBar.addPlayer(player);
        this.bossBar.setProgress(1.0);
    }

    public void resetTime() {
        this.timeLeft = 30;
        if (this.bossBar != null) {
            this.bossBar.setProgress(1.0);
            this.bossBar.setTitle(render(30));
        }
    }

    @Override
    public void run() {
        if (!player.isOnline() || timeLeft <= 0) {
            stop();
            combatManager.endCombat(player);
            return;
        }

        if (bossBar != null) {
            double progress = Math.max(0.0, Math.min(1.0, (double) timeLeft / 30.0));
            bossBar.setProgress(progress);
            bossBar.setTitle(render(timeLeft));
        }
        timeLeft--;
    }

    private String render(int seconds) {
        return LEGACY.serialize(MM.deserialize(String.format(TITLE_FORMAT, seconds)));
    }

    public void start() {
        this.taskId = Bukkit.getScheduler().runTaskTimer(plugin, this, 0L, 20L).getTaskId();
    }

    public void stop() {
        if (bossBar != null) {
            bossBar.removeAll();
            bossBar = null;
        }
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }
}