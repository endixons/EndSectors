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

import java.time.Duration;
import net.kyori.adventure.title.Title;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import pl.endixon.sectors.paper.SectorsAPI;
import pl.endixon.sectors.paper.event.SectorChangeEvent;
import pl.endixon.sectors.paper.sector.Sector;
import pl.endixon.sectors.paper.user.profile.UserProfile;
import pl.endixon.sectors.tools.utils.MessagesUtil;
import pl.endixon.sectors.tools.utils.TeleportUtil;

public class RandomTPCommand implements CommandExecutor {

    private static final int COUNTDOWN_TIME = 10;
    private final SectorsAPI api;

    public RandomTPCommand(SectorsAPI api) {
        if (api == null) {
            throw new IllegalArgumentException("SectorsAPI cannot be null!");
        }
        this.api = api;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(MessagesUtil.CONSOLE_BLOCK.get());
            return true;
        }

        player.showTitle(
                Title.title(
                        MessagesUtil.RANDOM_TITLE.get(),
                        MessagesUtil.RANDOM_START.get(),
                        Title.Times.times(
                                Duration.ofMillis(0),
                                Duration.ofMillis(9999),
                                Duration.ofMillis(0)
                        )
                )
        );

        UserProfile user = api.getUser(player).orElse(null);

        if (user == null) {
            player.sendMessage(MessagesUtil.PLAYERDATANOT_FOUND_MESSAGE.get());
            return true;
        }

        boolean isAdmin = player.hasPermission("endsectors.admin");
        int countdown = isAdmin ? 0 : COUNTDOWN_TIME;
        user.setTransferOffsetUntil(0);

        TeleportUtil.startTeleportCountdown(player, countdown, () -> {
            api.getRandomLocation(player, user);

            Sector randomSector = api.getSectorManager().getSector(user.getSectorName());

            if (randomSector == null) {
                player.sendMessage(MessagesUtil.RANDOM_SECTOR_NOTFOUND.get());
                return;
            }

            SectorChangeEvent event = new SectorChangeEvent(player, randomSector);
            api.getPaperSector().getServer().getPluginManager().callEvent(event);

            if (!event.isCancelled()) {
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
            }
        });
        return true;
    }
}
