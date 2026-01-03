package pl.endixon.sectors.tools.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.entity.Player;
import pl.endixon.sectors.tools.EndSectorsToolsPlugin;
import pl.endixon.sectors.tools.task.CombatTask;
import pl.endixon.sectors.tools.validators.combat.CombatValidator;
import pl.endixon.sectors.tools.validators.combat.SelfHitValidator;
import pl.endixon.sectors.tools.validators.player.GameModeValidator;
import pl.endixon.sectors.tools.validators.player.PlayerTypeValidator;

public class CombatManager {

    private final EndSectorsToolsPlugin plugin;
    private final Map<Player, Player> inCombat = new HashMap<>();
    private final Map<Player, CombatTask> activeTasks = new HashMap<>();
    private final List<CombatValidator> validators = new ArrayList<>();

    public CombatManager(EndSectorsToolsPlugin plugin) {
        this.plugin = plugin;
        validators.add(new PlayerTypeValidator());
        validators.add(new GameModeValidator());
        validators.add(new SelfHitValidator());
    }

    public boolean canStartCombat(Player attacker, Player victim) {
        for (CombatValidator validator : validators) {
            if (!validator.validate(attacker, victim)) {
                return false;
            }
        }
        return true;
    }

    public void startCombat(Player attacker, Player victim) {
        inCombat.put(attacker, victim);
        inCombat.put(victim, attacker);
        handlePlayerTask(attacker);
        handlePlayerTask(victim);
    }

    private void handlePlayerTask(Player player) {
        if (activeTasks.containsKey(player)) {
            activeTasks.get(player).resetTime();
        } else {
            CombatTask task = new CombatTask(plugin, this, player);
            activeTasks.put(player, task);
            task.start();
        }
    }

    public void endCombat(Player player) {
        inCombat.remove(player);
        CombatTask task = activeTasks.remove(player);
        if (task != null) {
            task.stop();
        }
    }

    public boolean isInCombat(Player player) {
        return inCombat.containsKey(player);
    }
}