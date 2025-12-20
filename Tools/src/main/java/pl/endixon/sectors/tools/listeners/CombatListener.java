package pl.endixon.sectors.tools.listeners;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import pl.endixon.sectors.tools.Main;
import pl.endixon.sectors.tools.manager.CombatManager;
import pl.endixon.sectors.tools.task.CombatTask;
import pl.endixon.sectors.tools.utils.MessagesUtil;

public class CombatListener implements Listener {

    private final CombatManager combatManager;

    public CombatListener(CombatManager combatManager) {
        this.combatManager = combatManager;
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (victim.getGameMode() != GameMode.SURVIVAL && victim.getGameMode() != GameMode.ADVENTURE) return;
        if (attacker.getGameMode() != GameMode.SURVIVAL && attacker.getGameMode() != GameMode.ADVENTURE) return;
        if (combatManager.isInCombat(attacker)) combatManager.endCombat(attacker);
        if (combatManager.isInCombat(victim)) combatManager.endCombat(victim);
        this.combatManager.startCombat(attacker, victim);
        new CombatTask(Main.getInstance(), combatManager, attacker).start();
        new CombatTask(Main.getInstance(), combatManager, victim).start();
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        if (combatManager.isInCombat(player)) {
            player.sendMessage((MessagesUtil.COMBAT_NO_COMMAND.get()));
            event.setCancelled(true);
        }
    }
}
