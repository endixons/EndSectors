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

public class SpawnCommand implements CommandExecutor {

    private static final int COUNTDOWN_TIME = 10;

    // Definiujemy standardowe czasy dla tytułów (Enterprise Standard: 0.5s fade-in, 2s stay, 0.5s fade-out)
    private static final Title.Times TITLE_TIMES = Title.Times.times(
            Duration.ofMillis(500),
            Duration.ofMillis(2000),
            Duration.ofMillis(500)
    );

    private final SectorsAPI api;

    public SpawnCommand(SectorsAPI api) {
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

        if (currentSector != null && currentSector.getType() == SectorType.SPAWN) {
            this.sendAlertTitle(player, MessagesUtil.SPAWN_ALREADY);
            return true;
        }

        Sector spawnSector = api.getSectorManager().getBalancedRandomSpawnSector();

        if (spawnSector == null) {
            this.sendAlertTitle(player, MessagesUtil.RANDOM_SECTORSPAWN_NOTFOUND);
            return true;
        }

        if (!spawnSector.isOnline()) {
            this.sendAlertTitle(player, MessagesUtil.SPAWN_OFFLINE);
            return true;
        }
        user.setTransferOffsetUntil(0);
        boolean isAdmin = player.hasPermission("endsectors.admin");
        int countdown = isAdmin ? 0 : COUNTDOWN_TIME;

        TeleportUtil.startTeleportCountdown(player, countdown, () -> {
            api.teleportPlayer(player, user, spawnSector, false, true);
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
        });

        return true;
    }


    private void sendAlertTitle(Player player, MessagesUtil subTitleMessage) {
        Title title = Title.title(MessagesUtil.SPAWN_TITLE.get(), subTitleMessage.get(), TITLE_TIMES);
        player.showTitle(title);
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
    }
}