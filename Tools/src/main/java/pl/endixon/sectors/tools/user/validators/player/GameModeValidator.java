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

package pl.endixon.sectors.tools.user.validators.player;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import pl.endixon.sectors.tools.user.validators.combat.CombatValidator;

public class GameModeValidator implements CombatValidator {

    @Override
    public boolean validate(Player attacker, Player victim) {
        return isValid(attacker) && isValid(victim);
    }

    private boolean isValid(Player player) {
        return player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE;
    }
}
