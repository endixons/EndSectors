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

public class MarketStorageWindow {

    private final Player player;
    private final PlayerProfile profile;
    private final EndSectorsToolsPlugin plugin = EndSectorsToolsPlugin.getInstance();

    public MarketStorageWindow(Player player, PlayerProfile profile) {
        this.player = player;
        this.profile = profile;
        open();
    }

    public void open() {
        WindowUI window = new WindowUI("Magazyn (Wygasłe)", 3);
        List<PlayerMarketProfile> expiredOffers = new ArrayList<>(plugin.getMarketRepository().findExpiredBySeller(player.getUniqueId()));
        expiredOffers.sort((a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));

        if (expiredOffers.isEmpty()) {
            window.setSlot(13, new StackBuilder(new ItemStack(Material.BARRIER))
                    .name("§cPusto")
                    .lore("§7Nie masz żadnych przedmiotów do odebrania.")
                    .build(), null);
        }

        int slot = 0;
        for (PlayerMarketProfile offer : expiredOffers) {
            if (slot >= 26) break;

            ItemStack[] deserialized = PlayerDataSerializerUtil.deserializeItemStacksFromBase64(offer.getItemData());
            ItemStack originalItem = (deserialized.length > 0) ? deserialized[0] : new ItemStack(Material.BARRIER);
            StackBuilder builder = MarketItemRenderer.prepareStorageItem(offer, originalItem);

            window.setSlot(slot, builder.build(), event -> {


                if (!MarketItemUtil.hasSpace(player, originalItem)) {
                    player.sendMessage("§cMasz pełny ekwipunek!");
                    player.sendMessage("§7Zrób miejsce, aby odebrać ten przedmiot.");
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                    player.closeInventory();
                    return;
                }

                boolean success = plugin.getMarketService().claimStorageItem(offer.getId(), player.getUniqueId());

                if (success) {
                    ItemStack[] itemsToReturn = PlayerDataSerializerUtil.deserializeItemStacksFromBase64(offer.getItemData());
                    if (itemsToReturn.length > 0) {
                        player.getInventory().addItem(itemsToReturn[0]);

                    }
                    event.getClickedInventory().setItem(event.getSlot(), new ItemStack(Material.AIR));
                    player.sendMessage("§aOdebrano przedmiot z magazynu!");
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);

                    Bukkit.getScheduler().runTask(plugin, this::open);

                } else {
                    player.sendMessage("§cBłąd! Nie udało się odebrać przedmiotu.");
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                    player.closeInventory();
                }
            });
            slot++;
        }

        window.setSlot(26,
                new StackBuilder(new ItemStack(Material.ARROW)).name("§e« Wróć na Market").build(),
                event -> new MarketWindow(player, profile, "ALL", 0)
        );

        player.openInventory(window.getInventory());
    }

}