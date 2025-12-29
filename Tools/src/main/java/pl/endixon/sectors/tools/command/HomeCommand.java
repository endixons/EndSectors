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

package pl.endixon.sectors.tools.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import pl.endixon.sectors.paper.SectorsAPI;
import pl.endixon.sectors.paper.user.profile.UserProfile;
import pl.endixon.sectors.tools.inventory.HomeWindow;
import pl.endixon.sectors.tools.user.profile.PlayerProfile;
import pl.endixon.sectors.tools.user.profile.ProfileCache;
import pl.endixon.sectors.tools.utils.MessagesUtil;

public class HomeCommand implements CommandExecutor {

    private final SectorsAPI api;

    public HomeCommand(SectorsAPI api) {
        if (api == null) {
        throw new IllegalArgumentException("SectorsAPI cannot be null!");
        }
        this.api = api;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(MessagesUtil.CONSOLE_BLOCK.get());
            return true;
        }

       PlayerProfile profile = ProfileCache.get(player.getUniqueId());

        if (profile == null) {
            player.sendMessage(MessagesUtil.PLAYERDATANOT_FOUND_MESSAGE.get());
            return true;
        }

        UserProfile user = api.getUser(player).orElse(null);

        if (user == null) {
            player.sendMessage(MessagesUtil.PLAYERDATANOT_FOUND_MESSAGE.get());
            return true;
        }

        new HomeWindow(player, profile, api).open();
        return true;
    }
}
