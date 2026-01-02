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
import pl.endixon.sectors.tools.market.utils.MarketItemUtil;
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
        WindowUI window = new WindowUI("Market: " + category + " (" + (page + 1) + ")", 6);

        int expiredCount = plugin.getMarketRepository().findExpiredBySeller(player.getUniqueId()).size();
        int claimableCount = plugin.getMarketRepository().findClaimableBySeller(player.getUniqueId()).size();
        int myOffersCount = plugin.getMarketRepository().findBySeller(player.getUniqueId()).size();


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
                        this.givePurchasedItem(offer);
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
                        player.sendMessage("§cPrzedmiot nie istnieje.");
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
            window.setSlot(45, new StackBuilder(new ItemStack(Material.ARROW)).name("§c« Poprzednia strona").build(),
                    event -> new MarketWindow(player, profile, category, page - 1));
        }

        window.setSlot(46, new StackBuilder(new ItemStack(Material.GRASS_BLOCK)).name("§aKategoria: Bloki").build(),
                event -> new MarketWindow(player, profile, "BLOCKS", 0));

        window.setSlot(47, new StackBuilder(new ItemStack(Material.DIAMOND_SWORD)).name("§bKategoria: Ekwipunek").build(),
                event -> new MarketWindow(player, profile, "WEAPONS", 0));

        window.setSlot(48, new StackBuilder(new ItemStack(Material.HEAVY_CORE)).name("§6Kategoria: Inne").build(),
                event -> new MarketWindow(player, profile, "OTHER", 0));

        if (category.equals("ALL")) {
            window.setSlot(49, new StackBuilder(new ItemStack(Material.BARRIER)).name("§cZamknij").build(),
                    event -> player.closeInventory());
        } else {
            window.setSlot(49, new StackBuilder(new ItemStack(Material.NETHER_STAR)).name("§fWszystko (Reset)").build(),
                    event -> new MarketWindow(player, profile, "ALL", 0));
        }

        window.setSlot(50, MarketItemRenderer.prepareMyOffersIcon(myOffersCount).build(),
                event -> new MarketMyOffersWindow(player, profile));

        window.setSlot(51, MarketItemRenderer.prepareClaimableIcon(claimableCount).build(), event -> {
            if (claimableCount > 0) {
                new MarketClaimableWindow(player, profile);
            } else {
                player.sendMessage("§aDepozyt jest pusty.");
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            }
        });

        window.setSlot(52, MarketItemRenderer.prepareStorageIcon(expiredCount).build(), event -> {
            if (expiredCount > 0) {
                new MarketStorageWindow(player, profile);
            } else {
                player.sendMessage("§aMagazyn wygasłych przedmiotow jest pusty.");
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            }
        });

        if (offers.size() > end) {
            window.setSlot(53, new StackBuilder(new ItemStack(Material.ARROW)).name("§aNastępna strona »").build(),
                    event -> new MarketWindow(player, profile, category, page + 1));
        }

        player.openInventory(window.getInventory());
    }


    private void givePurchasedItem(PlayerMarketProfile offer) {
        Map<Integer, ItemStack> leftOver = MarketItemUtil.giveItemToPlayer(this.player, offer.getItemData());
        if (!leftOver.isEmpty()) {
            leftOver.values().forEach(item -> {
                this.plugin.getMarketRepository().sendToStorage(
                        this.player.getUniqueId(),
                        this.player.getName(),
                        item,
                        offer.getCategory()
                );
            });

            this.player.sendMessage("§cEkwipunek pełny! §ePrzedmiot trafił do depozytu");
            this.player.playSound(this.player.getLocation(), Sound.ENTITY_IRON_GOLEM_REPAIR, 1f, 1f);
        }
    }
}