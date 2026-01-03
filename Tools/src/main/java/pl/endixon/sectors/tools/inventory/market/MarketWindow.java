package pl.endixon.sectors.tools.inventory.market;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import pl.endixon.sectors.tools.EndSectorsToolsPlugin;
import pl.endixon.sectors.tools.inventory.api.WindowUI;
import pl.endixon.sectors.tools.inventory.api.builder.StackBuilder;
import pl.endixon.sectors.tools.market.render.MarketItemRenderer;
import pl.endixon.sectors.tools.market.type.PurchaseResult;
import pl.endixon.sectors.tools.market.utils.MarketItemUtil;
import pl.endixon.sectors.tools.user.profile.player.PlayerProfile;
import pl.endixon.sectors.tools.user.profile.player.PlayerMarketProfile;
import pl.endixon.sectors.tools.user.profile.cache.ProfileMarketCache;
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
    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.legacySection();

    public MarketWindow(Player player, PlayerProfile profile, String category, int page) {
        this.player = player;
        this.profile = profile;
        this.category = category;
        this.page = page;
        open();
    }

    private String hex(String text) {
        return SERIALIZER.serialize(MM.deserialize(text));
    }

    public void open() {
        WindowUI window = new WindowUI("Market: " + category + " (" + (page + 1) + ")", 6);
        window.setInteractionAllowed(false);

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
                        player.sendMessage(hex("<#55ff55>Zakupiono pomyślnie: <white>" + offer.getItemName()));
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);
                        player.closeInventory();
                    }
                    case NOT_ENOUGH_MONEY -> {
                        player.sendMessage(hex("<#ff5555>Nie masz wystarczających środków!"));
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                    }
                    case ALREADY_SOLD -> {
                        player.sendMessage(hex("<#ff5555>Przedmiot został już sprzedany!"));
                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1f, 1f);
                        open();
                    }
                    case NOT_FOUND -> {
                        player.sendMessage(hex("<#ff5555>Przedmiot nie istnieje."));
                        open();
                    }
                    case SELF_PURCHASE -> {
                        player.sendMessage(hex("<#ff5555>Nie możesz kupić własnego przedmiotu!"));
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 0.5f);
                    }
                }
            });
        }


        if (page > 0) {
            window.setSlot(45, new StackBuilder(new ItemStack(Material.ARROW)).name(hex("<gradient:#ffcc00:#ffaa00>« Poprzednia strona</gradient>")).build(),
                    event -> new MarketWindow(player, profile, category, page - 1));
        }

        window.setSlot(46, new StackBuilder(new ItemStack(Material.GRASS_BLOCK)).name(hex("<gradient:#55ff55:#00aa00>Kategoria: Bloki</gradient>")).build(),
                event -> new MarketWindow(player, profile, "BLOCKS", 0));

        window.setSlot(47, new StackBuilder(new ItemStack(Material.DIAMOND_SWORD)).name(hex("<gradient:#55ffff:#00aaaa>Kategoria: Ekwipunek</gradient>")).addFlag(ItemFlag.HIDE_ATTRIBUTES).build(),
                event -> new MarketWindow(player, profile, "WEAPONS", 0));

        window.setSlot(48, new StackBuilder(new ItemStack(Material.HEAVY_CORE)).name(hex("<gradient:#ffaa00:#ffff55>Kategoria: Inne</gradient>")).build(),
                event -> new MarketWindow(player, profile, "OTHER", 0));

        window.setSlot(49, new StackBuilder(new ItemStack(Material.BARRIER)).name(hex("<#ff5555>Zamknij")).build(),
                event -> player.closeInventory());

         window.setSlot(50, new StackBuilder(new ItemStack(Material.NETHER_STAR)).name(hex("<white>Wszystko (Reset)")).build(),
                    event -> new MarketWindow(player, profile, "ALL", 0));

        window.setSlot(51,
                MarketItemRenderer.prepareProfileIcon(player.getName(), myOffersCount, claimableCount, expiredCount)
                        .skullOwner(player.getName())
                        .build(),
                event -> new MarketProfileWindow(player, profile)
        );

        window.setSlot(52,
                MarketItemRenderer.prepareInformationIcon().build(),
                event -> player.closeInventory()
        );

        if (offers.size() > end) {
            window.setSlot(53, new StackBuilder(new ItemStack(Material.ARROW)).name(hex("<gradient:#ffcc00:#ffaa00>Następna strona »</gradient>")).build() ,
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

            this.player.sendMessage(hex("<#ff5555>Ekwipunek pełny! <#ffff55>Przedmiot trafił do Depozytu."));
            this.player.playSound(this.player.getLocation(), Sound.ENTITY_IRON_GOLEM_REPAIR, 1f, 1f);
        }
    }
}