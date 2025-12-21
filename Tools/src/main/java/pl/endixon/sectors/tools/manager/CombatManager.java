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

package pl.endixon.sectors.tools.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.entity.Player;
import pl.endixon.sectors.tools.Main;
import pl.endixon.sectors.tools.task.CombatTask;
import pl.endixon.sectors.tools.user.validators.combat.CombatValidator;
import pl.endixon.sectors.tools.user.validators.combat.SelfHitValidator;
import pl.endixon.sectors.tools.user.validators.player.GameModeValidator;
import pl.endixon.sectors.tools.user.validators.player.PlayerTypeValidator;

public class CombatManager {

    private final Main plugin;
    private final Map<Player, Player> inCombat = new HashMap<>();
    private final List<CombatValidator> validators = new ArrayList<>();

    public CombatManager(Main plugin) {
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

        new CombatTask(plugin, this, attacker).start();
        new CombatTask(plugin, this, victim).start();
    }

    public void endCombat(Player player) {
        Player other = inCombat.remove(player);
        if (other != null)
        inCombat.remove(other);
    }

    public boolean isInCombat(Player player) {
        return inCombat.containsKey(player);
    }
}
