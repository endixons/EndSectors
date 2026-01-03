package pl.endixon.sectors.tools.inventory.market;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.endixon.sectors.tools.EndSectorsToolsPlugin;
import pl.endixon.sectors.tools.inventory.api.WindowUI;
import pl.endixon.sectors.tools.inventory.api.builder.StackBuilder;
import pl.endixon.sectors.tools.market.render.MarketItemRenderer;
import pl.endixon.sectors.tools.user.profile.player.PlayerProfile;

public class MarketProfileWindow {

    private final Player player;
    private final PlayerProfile profile;
    private final EndSectorsToolsPlugin plugin = EndSectorsToolsPlugin.getInstance();

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.legacySection();

    public MarketProfileWindow(Player player, PlayerProfile profile) {
        this.player = player;
        this.profile = profile;
        open();
    }

    private String hex(String text) {
        return SERIALIZER.serialize(MM.deserialize(text));
    }

    public void open() {
        WindowUI window = new WindowUI(hex("Twoj profil"), 1);
        window.setInteractionAllowed(false);

        int myOffersCount = plugin.getMarketRepository().findBySeller(player.getUniqueId()).size();
        int claimableCount = plugin.getMarketRepository().findClaimableBySeller(player.getUniqueId()).size();
        int expiredCount = plugin.getMarketRepository().findExpiredBySeller(player.getUniqueId()).size();

        window.setSlot(2, MarketItemRenderer.prepareMyOffersIcon(myOffersCount).build(), event -> new MarketMyOffersWindow(player, profile));

        window.setSlot(4, MarketItemRenderer.prepareClaimableIcon(claimableCount).build(), event -> {
            if (claimableCount > 0) {
                new MarketClaimableWindow(player, profile);
            } else {
                player.sendMessage(hex("<#ff5555>Depozyt jest pusty."));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            }
        });

        window.setSlot(6, MarketItemRenderer.prepareStorageIcon(expiredCount).build(), event -> {
            if (expiredCount > 0) {
                new MarketStorageWindow(player, profile);
            } else {
                player.sendMessage(hex("<#ff5555>Magazyn jest pusty."));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            }
        });

        window.setSlot(8, new StackBuilder(new ItemStack(Material.ARROW))
                        .name(hex("<gradient:#ffcc00:#ffaa00>« Wróć na Market</gradient>"))
                        .build(),
                event -> new MarketWindow(player, profile, "ALL", 0));

        player.openInventory(window.getInventory());
    }
}