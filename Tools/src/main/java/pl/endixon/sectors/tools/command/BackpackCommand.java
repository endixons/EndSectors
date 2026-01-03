/*
 *
 * EndSectors – Non-Commercial License
 * (c) 2025 Endixon
 *
 * Permission is granted to use, copy, and
 * modify this software **only** for personal
 * or educational purposes.
 *
 * Commercial use, redistribution, claiming
 * this work as your own, or copying code
 * without explicit permission is strictly
 * prohibited.
 *
 * Visit https://github.com/Endixon/EndSectors
 * for more info.
 *
 */

package pl.endixon.sectors.tools.command;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import pl.endixon.sectors.tools.backpack.BackpackService;
import pl.endixon.sectors.tools.inventory.backpack.BackpackWindow;
import pl.endixon.sectors.tools.manager.CombatManager;
import pl.endixon.sectors.tools.user.profile.player.PlayerBackpackProfile;
import pl.endixon.sectors.tools.user.profile.player.PlayerProfile;
import pl.endixon.sectors.tools.user.profile.cache.ProfileCache;
import pl.endixon.sectors.tools.utils.MessagesUtil;

@RequiredArgsConstructor
public class BackpackCommand implements CommandExecutor {

    private final CombatManager combatManager;
    private final BackpackService backpackService;
    private static final MiniMessage MM = MiniMessage.miniMessage();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(MessagesUtil.CONSOLE_BLOCK.get());
            return true;
        }

        final PlayerProfile profile = ProfileCache.get(player.getUniqueId());
        if (profile == null) {
            player.sendMessage(MessagesUtil.PLAYERDATANOT_FOUND_MESSAGE.get());
            return true;
        }

        if (this.combatManager.isInCombat(player)) {
            final String combatError = "<#ff4b2b><bold>[!]</bold> <#a8a8a8>Dostęp do plecaka jest <#ff4b2b>zablokowany <#a8a8a8>podczas walki!";
            player.sendMessage(MM.deserialize(combatError));
            return true;
        }

        final PlayerBackpackProfile backpack = this.backpackService.getOrCreateBackpack(player);
        new BackpackWindow(player, profile, backpack, 1, this.backpackService);
        return true;
    }
}