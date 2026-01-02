package pl.endixon.sectors.tools.task;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import pl.endixon.sectors.tools.EndSectorsToolsPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public class MarketBossBarTask extends BukkitRunnable {

    private final EndSectorsToolsPlugin plugin;

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final String DEPOSIT_TEMPLATE = "<gradient:#00c6ff:#0072ff><bold>DEPOZYT:</bold></gradient> <#aaaaaa>Odbierz przedmioty: <#00c6ff><amount> <#eebb00>/market";
    private static final String WAREHOUSE_TEMPLATE = "<gradient:#ff512f:#dd2476><bold>MAGAZYN:</bold></gradient> <#aaaaaa>Wygasłe przedmioty: <#ff512f><amount> <#eebb00>/market";

    private final Map<UUID, BossBar> activeBars = new HashMap<>();

    @Override
    public void run() {
        activeBars.keySet().removeIf(uuid -> Bukkit.getPlayer(uuid) == null);
        for (Player player : Bukkit.getOnlinePlayers()) {
            this.checkPlayer(player);
        }
    }

    private void checkPlayer(final Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            final UUID uuid = player.getUniqueId();
            final int claimable = plugin.getMarketRepository().findClaimableBySeller(uuid).size();
            final int expired = plugin.getMarketRepository().findExpiredBySeller(uuid).size();

            Bukkit.getScheduler().runTask(plugin, () -> {
                if (!player.isOnline()) {
                    return;
                }

                if (claimable == 0 && expired == 0) {
                    if (activeBars.containsKey(uuid)) {
                        player.hideBossBar(activeBars.remove(uuid));
                    }
                    return;
                }

                final Component title;
                final BossBar.Color color;
                final BossBar.Overlay overlay = BossBar.Overlay.NOTCHED_10;

                if (claimable > 0) {
                    title = MM.deserialize(
                            DEPOSIT_TEMPLATE,
                            Placeholder.unparsed("amount", String.valueOf(claimable))
                    );
                    color = BossBar.Color.BLUE;
                } else {
                    title = MM.deserialize(
                            WAREHOUSE_TEMPLATE,
                            Placeholder.unparsed("amount", String.valueOf(expired))
                    );
                    color = BossBar.Color.RED;
                }

                final BossBar bar = activeBars.get(uuid);

                if (bar == null) {
                    final BossBar newBar = BossBar.bossBar(title, 1.0f, color, overlay);
                    player.showBossBar(newBar);
                    activeBars.put(uuid, newBar);
                } else {
                    bar.name(title);
                    bar.color(color);
                    bar.overlay(overlay);
                    bar.progress(1.0f);
                }
            });
        });
    }
}