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

import java.time.Duration;
import net.kyori.adventure.title.Title;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import pl.endixon.sectors.common.sector.SectorType;
import pl.endixon.sectors.paper.SectorsAPI;
import pl.endixon.sectors.paper.sector.Sector;
import pl.endixon.sectors.paper.user.profile.UserProfile;
import pl.endixon.sectors.tools.utils.MessagesUtil;
import pl.endixon.sectors.tools.utils.TeleportUtil;

public class AfkCommand implements CommandExecutor {

    private static final int COUNTDOWN_TIME = 10;


    private static final Title.Times TITLE_TIMES = Title.Times.times(
            Duration.ofMillis(500),
            Duration.ofMillis(2000),
            Duration.ofMillis(500)
    );

    private final SectorsAPI api;

    public AfkCommand(SectorsAPI api) {
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

        UserProfile user = api.getUser(player).orElse(null);
        if (user == null) {
            player.sendMessage(MessagesUtil.PLAYERDATANOT_FOUND_MESSAGE.get());
            return true;
        }

        Sector currentSector = api.getSectorManager().getCurrentSector();

        if (currentSector != null && currentSector.getType() == SectorType.AFK) {
            this.sendAlertTitle(player, MessagesUtil.AFK_ALREADY);
            return true;
        }

        Sector afkSector = api.getSectorManager().getBalancedRandomAfkSector();

        if (afkSector == null) {
            this.sendAlertTitle(player, MessagesUtil.RANDOM_SECTORAFK_NOTFOUND);
            return true;
        }

        if (!afkSector.isOnline()) {
            this.sendAlertTitle(player, MessagesUtil.AFK_OFFLINE);
            return true;
        }
        user.setTransferOffsetUntil(0);
        boolean isAdmin = player.hasPermission("endsectors.admin");
        int countdown = isAdmin ? 0 : COUNTDOWN_TIME;

        TeleportUtil.startTeleportCountdown(player, countdown, () -> {
            api.teleportPlayer(player, user, afkSector, false, true);
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
        });

        return true;
    }


    private void sendAlertTitle(Player player, MessagesUtil subTitleMessage) {
        Title title = Title.title(MessagesUtil.AFK_TITLE.get(), subTitleMessage.get(), TITLE_TIMES);
        player.showTitle(title);
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
    }
}