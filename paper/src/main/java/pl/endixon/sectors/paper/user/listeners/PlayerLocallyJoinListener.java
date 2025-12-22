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

package pl.endixon.sectors.paper.user.listeners;

import java.time.Duration;
import lombok.AllArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import pl.endixon.sectors.common.sector.SectorType;
import pl.endixon.sectors.paper.PaperSector;
import pl.endixon.sectors.paper.sector.Sector;
import pl.endixon.sectors.paper.user.profile.UserProfile;
import pl.endixon.sectors.paper.user.profile.UserProfileRepository;
import pl.endixon.sectors.paper.util.ChatAdventureUtil;
import pl.endixon.sectors.paper.util.ConfigurationUtil;

@AllArgsConstructor
public class PlayerLocallyJoinListener implements Listener {

    private final PaperSector paperSector;
    private final ChatAdventureUtil CHAT = new ChatAdventureUtil();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        event.joinMessage(Component.empty());
        player.setCollidable(false);

        UserProfile user = UserProfileRepository.getUser(player).orElseGet(() -> new UserProfile(player));

        Sector currentSector = paperSector.getSectorManager().getCurrentSector();

        Bukkit.getScheduler().runTask(paperSector, () -> {
            if (currentSector == null)
                return;

            if (user.isFirstJoin() && currentSector.getType() != SectorType.QUEUE && currentSector.getType() != SectorType.NETHER && currentSector.getType() != SectorType.END) {
                sendSectorTitle(player, currentSector);
                user.setFirstJoin(false);
                user.setLastSectorTransfer(false);
                user.updateFromPlayer(player, currentSector,false);
                paperSector.getSectorManager().randomLocation(player, user);
            } else {
                sendSectorTitle(player, currentSector);
                user.applyPlayerData();
                user.setLastSectorTransfer(false);
            }
        });
    }

    private void sendSectorTitle(Player player, Sector sector) {
        Component title = CHAT.toComponent("");
        Component subtitle = CHAT.toComponent(ConfigurationUtil.SECTOR_CONNECTED_MESSAGE.replace("{SECTOR}", sector.getName())
        );
        Title.Times times = Title.Times.times(
                Duration.ofMillis(500),
                Duration.ofMillis(2000),
                Duration.ofMillis(500)
        );

        player.showTitle(Title.title(title, subtitle, times));
    }

}
