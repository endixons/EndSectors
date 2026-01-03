package pl.endixon.sectors.tools.market.listener;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import pl.endixon.sectors.tools.EndSectorsToolsPlugin;

import java.time.Duration;

@RequiredArgsConstructor
public class ProfileMarketJoinListener implements Listener {

    private final EndSectorsToolsPlugin plugin;
    private static final MiniMessage MM = MiniMessage.miniMessage();

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {

            int expiredCount = plugin.getMarketRepository().findExpiredBySeller(player.getUniqueId()).size();

            if (expiredCount > 0) {

                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (!player.isOnline()) return;

                    Component mainTitle = MM.deserialize("<bold><gradient:#ffaa00:#ffff55>MARKET</gradient></bold>");
                    Component subTitle = MM.deserialize("<gray>Posiadasz Wygasłe przedmioty do odebrania: <#ff3333>" + expiredCount);
                    Title.Times times = Title.Times.times(
                            Duration.ofMillis(500),
                            Duration.ofMillis(3500),
                            Duration.ofMillis(1000)
                    );


                    Title title = Title.title(mainTitle, subTitle, times);
                    player.showTitle(title);
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.5f);
                });
            }
        });
    }
}