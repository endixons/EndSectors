package pl.endixon.sectors.tools.inventory.market;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.endixon.sectors.tools.EndSectorsToolsPlugin;
import pl.endixon.sectors.tools.inventory.api.WindowUI;
import pl.endixon.sectors.tools.inventory.api.builder.StackBuilder;
import pl.endixon.sectors.tools.market.render.MarketItemRenderer;
import pl.endixon.sectors.tools.market.utils.MarketItemUtil;
import pl.endixon.sectors.tools.user.profile.PlayerMarketProfile;
import pl.endixon.sectors.tools.user.profile.PlayerProfile;
import pl.endixon.sectors.tools.utils.PlayerDataSerializerUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MarketMyOffersWindow {

    private final Player player;
    private final PlayerProfile profile;
    private final EndSectorsToolsPlugin plugin = EndSectorsToolsPlugin.getInstance();

    public MarketMyOffersWindow(Player player, PlayerProfile profile) {
        this.player = player;
        this.profile = profile;
        open();
    }

    public void open() {
        WindowUI window = new WindowUI("Market", 3);
        List<PlayerMarketProfile> myOffers = new ArrayList<>(plugin.getMarketRepository().findBySeller(player.getUniqueId()));
        myOffers.sort((a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));

        int slot = 0;
        for (PlayerMarketProfile offer : myOffers) {
            if (slot >= 18) break;

            ItemStack[] deserialized = PlayerDataSerializerUtil.deserializeItemStacksFromBase64(offer.getItemData());
            ItemStack originalItem = (deserialized.length > 0) ? deserialized[0] : new ItemStack(Material.BARRIER);

            StackBuilder builder = MarketItemRenderer.prepareManageItem(offer, originalItem);

            window.setSlot(slot, builder.build(), event -> {

                if (!MarketItemUtil.hasSpace(player, originalItem)) {
                    player.sendMessage("§cMasz pełny ekwipunek!");
                    player.sendMessage("§7Zrób miejsce, aby wycofać tę ofertę.");
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                    player.closeInventory();
                    return;
                }

                boolean success = plugin.getMarketService().cancelOffer(offer.getId(), player.getUniqueId());

                if (success) {
                    MarketItemUtil.giveItemToPlayer(player, offer.getItemData());
                    event.getClickedInventory().setItem(event.getSlot(), new ItemStack(Material.AIR));
                    player.sendMessage("§aOferta została pomyślnie wycofana.");
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.5f);

                    Bukkit.getScheduler().runTask(plugin, this::open);

                } else {
                    player.sendMessage("§cNie udało się wycofać oferty (może została sprzedana?).");
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                    player.closeInventory();
                }
            });
            slot++;
        }
        window.setSlot(22,
                new StackBuilder(new ItemStack(Material.ARROW)).name("§e« Wróć do przeglądania").build(),
                event -> new MarketWindow(player, profile, "ALL", 0)
        );

        player.openInventory(window.getInventory());
    }

}