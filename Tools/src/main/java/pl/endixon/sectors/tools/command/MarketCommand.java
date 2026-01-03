package pl.endixon.sectors.tools.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import pl.endixon.sectors.tools.inventory.market.MarketWindow;
import pl.endixon.sectors.tools.user.profile.player.PlayerProfile;
import pl.endixon.sectors.tools.user.profile.cache.ProfileCache;
import pl.endixon.sectors.tools.utils.MessagesUtil;

public class MarketCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(MessagesUtil.CONSOLE_BLOCK.get());
            return true;
        }

        final PlayerProfile profile = ProfileCache.get(player.getUniqueId());
        if (profile == null) {
            player.sendMessage(MessagesUtil.PLAYERDATANOT_FOUND_MESSAGE.get());
            return true;
        }

        new MarketWindow(player, profile, "ALL", 0);
        return true;
    }
}