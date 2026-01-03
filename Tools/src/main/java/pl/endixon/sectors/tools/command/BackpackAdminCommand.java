/*
 *
 * EndSectors  Non-Commercial License
 * (c) 2025 Endixon
 *
 * Permission is granted to use, copy, and
 * modify this software **only** for personal
 * or educational purposes.
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
import pl.endixon.sectors.tools.EndSectorsToolsPlugin;
import pl.endixon.sectors.tools.backpack.BackpackService;
import pl.endixon.sectors.tools.inventory.backpack.BackpackAdminWindow;
import pl.endixon.sectors.tools.user.profile.cache.ProfileBackpackCache;
import pl.endixon.sectors.tools.user.profile.cache.ProfileCache;
import pl.endixon.sectors.tools.user.profile.player.PlayerBackpackProfile;
import pl.endixon.sectors.tools.user.profile.player.PlayerProfile;
import pl.endixon.sectors.tools.utils.MessagesUtil;

import java.util.UUID;

@RequiredArgsConstructor
public class BackpackAdminCommand implements CommandExecutor {

    private final EndSectorsToolsPlugin plugin;
    private final BackpackService backpackService;
    private static final MiniMessage MM = MiniMessage.miniMessage();

    private static final String ADMIN_PREFIX = "<gradient:#ed213a:#93291e><bold>ADMIN:</bold></gradient> ";
    private static final String TEXT = "<#aaaaaa>";

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {

        if (!(sender instanceof Player admin)) {
            sender.sendMessage(MessagesUtil.CONSOLE_BLOCK.get());
            return true;
        }


        if (!admin.hasPermission("endsectors.backpack.admin")) {
            admin.sendMessage(MM.deserialize("<#ff4b2b>Brak uprawnień administracyjnych."));
            return true;
        }

        if (args.length < 1) {
            admin.sendMessage(MM.deserialize(ADMIN_PREFIX + TEXT + "Poprawne użycie: <#ff5f6d>/bpadmin <nick/uuid>"));
            return true;
        }

        final String targetName = args[0];

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            PlayerProfile profile = this.findProfile(targetName);

            if (profile == null) {
                admin.sendMessage(MM.deserialize(ADMIN_PREFIX + "<#ff4b2b>Nie znaleziono profilu gracza: " + targetName));
                return;
            }

            PlayerBackpackProfile backpack = this.findBackpack(profile.getUuid(), profile.getName());
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                new BackpackAdminWindow(admin, profile, backpack, 1, backpackService);
                admin.sendMessage(MM.deserialize(ADMIN_PREFIX + TEXT + "Otwierasz plecak gracza: <#ff5f6d>" + profile.getName()));
            });
        });

        return true;
    }

    private PlayerProfile findProfile(String target) {
        Player online = plugin.getServer().getPlayer(target);
        if (online != null) return ProfileCache.get(online.getUniqueId());
        try {
            return plugin.getRepository().find(UUID.fromString(target)).orElse(null);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private PlayerBackpackProfile findBackpack(UUID uuid, String name) {
        PlayerBackpackProfile cached = ProfileBackpackCache.get(uuid);
        if (cached != null) return cached;
        return plugin.getBackpackRepository().find(uuid).orElse(null);
    }
}