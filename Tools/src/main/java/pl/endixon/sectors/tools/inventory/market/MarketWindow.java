package pl.endixon.sectors.tools.inventory.market;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.endixon.sectors.tools.EndSectorsToolsPlugin;
import pl.endixon.sectors.tools.inventory.api.WindowUI;
import pl.endixon.sectors.tools.inventory.api.builder.StackBuilder;
import pl.endixon.sectors.tools.market.render.MarketItemRenderer;
import pl.endixon.sectors.tools.market.type.PurchaseResult;
import pl.endixon.sectors.tools.user.profile.PlayerProfile;
import pl.endixon.sectors.tools.user.profile.PlayerMarketProfile;
import pl.endixon.sectors.tools.user.profile.ProfileMarketCache;
import pl.endixon.sectors.tools.utils.PlayerDataSerializerUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MarketWindow {

    private final Player player;
    private final PlayerProfile profile;
    private final EndSectorsToolsPlugin plugin = EndSectorsToolsPlugin.getInstance();
    private final String category;
    private final int page;

    public MarketWindow(Player player, PlayerProfile profile, String category, int page) {
        this.player = player;
        this.profile = profile;
        this.category = category;
        this.page = page;
        open();
    }

    public void open() {
        WindowUI window = new WindowUI("Rynek: " + category + " (" + (page + 1) + ")", 6);

        int expiredCount = plugin.getMarketRepository().findExpiredBySeller(player.getUniqueId()).size();
        int myOffers = plugin.getMarketRepository().findBySeller(player.getUniqueId()).size();

        List<PlayerMarketProfile> offers;
        if (category.equalsIgnoreCase("ALL")) {
            offers = new ArrayList<>(ProfileMarketCache.getValues());
        } else {
            offers = new ArrayList<>(plugin.getMarketRepository().findByCategory(category));
        }
        offers.sort((a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));

        int start = page * 45;
        int end = Math.min(start + 45, offers.size());

        for (int i = start; i < end; i++) {
            PlayerMarketProfile offer = offers.get(i);
            int slot = i - start;

            ItemStack[] deserialized = PlayerDataSerializerUtil.deserializeItemStacksFromBase64(offer.getItemData());
            ItemStack originalItem = (deserialized.length > 0) ? deserialized[0] : new ItemStack(Material.BARRIER);
            StackBuilder builder = MarketItemRenderer.prepareBuyItem(offer, originalItem);

            window.setSlot(slot, builder.build(), event -> {
                PurchaseResult result = plugin.getMarketService().processPurchase(
                        offer.getId(),
                        profile,
                        plugin.getRepository()
                );

                switch (result) {
                    case SUCCESS -> {
                        ItemStack[] boughtItems = PlayerDataSerializerUtil.deserializeItemStacksFromBase64(offer.getItemData());
                        if (boughtItems.length > 0) {
                            Map<Integer, ItemStack> leftOver = player.getInventory().addItem(boughtItems[0]);
                            if (!leftOver.isEmpty()) {
                                leftOver.values().forEach(item -> player.getWorld().dropItemNaturally(player.getLocation(), item));
                                player.sendMessage("§7Ekwipunek pełny! Przedmiot wyrzucono pod nogi.");
                            }
                        }
                        player.sendMessage("§aZakupiono pomyślnie: §f" + offer.getItemName());
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);
                        player.closeInventory();
                    }
                    case NOT_ENOUGH_MONEY -> {
                        player.sendMessage("§cNie masz wystarczających środków!");
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                    }
                    case ALREADY_SOLD -> {
                        player.sendMessage("§cPrzedmiot został już sprzedany!");
                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1f, 1f);
                        open();
                    }
                    case NOT_FOUND -> {
                        player.sendMessage("§cOferta nie istnieje.");
                        open();
                    }
                    case SELF_PURCHASE -> {
                        player.sendMessage("§cNie możesz kupić własnego przedmiotu!");
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 0.5f);
                    }
                }
            });
        }

        if (page > 0) {
            window.setSlot(45, new StackBuilder(new ItemStack(Material.ARROW)).name("§c« Poprzednia strona").build(), event -> new MarketWindow(player, profile, category, page - 1));
        }

        window.setSlot(46, new StackBuilder(new ItemStack(Material.GRASS_BLOCK)).name("§aBloki").build(), event -> new MarketWindow(player, profile, "BLOCKS", 0));
        window.setSlot(47, new StackBuilder(new ItemStack(Material.DIAMOND_SWORD)).name("§bEkwipunek").build(), event -> new MarketWindow(player, profile, "WEAPONS", 0));
        window.setSlot(48, new StackBuilder(new ItemStack(Material.NETHER_STAR)).name("§fWszystko").build(), event -> new MarketWindow(player, profile, "ALL", 0));
        window.setSlot(49, MarketItemRenderer.prepareMyOffersIcon(myOffers).build(), event -> new MarketMyOffersWindow(player, profile));
        window.setSlot(50, new StackBuilder(new ItemStack(Material.CHEST)).name("§6Inne").build(), event -> new MarketWindow(player, profile, "OTHER", 0));
        window.setSlot(51, new StackBuilder(new ItemStack(Material.BARRIER)).name("§cZamknij").build(), event -> player.closeInventory());

        window.setSlot(52, MarketItemRenderer.prepareStorageIcon(expiredCount).build(), event -> {
            if (expiredCount > 0) {
                new MarketStorageWindow(player, profile);
            } else {
                player.sendMessage("§aNie masz żadnych przedmiotów do odebrania.");
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            }
        });

        if (offers.size() > end) {
            window.setSlot(53, new StackBuilder(new ItemStack(Material.ARROW)).name("§aNastępna strona »").build(), event -> new MarketWindow(player, profile, category, page + 1));
        }

        player.openInventory(window.getInventory());
    }
}