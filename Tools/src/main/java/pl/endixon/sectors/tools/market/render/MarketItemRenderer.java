package pl.endixon.sectors.tools.market.render;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;
import pl.endixon.sectors.tools.inventory.api.builder.StackBuilder;
import pl.endixon.sectors.tools.market.utils.MarketItemUtil;
import pl.endixon.sectors.tools.user.profile.PlayerMarketProfile;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor
public final class MarketItemRenderer {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.legacySection();


    private static String hex(String text) {
        return SERIALIZER.serialize(MM.deserialize(text));
    }

    public static StackBuilder prepareBuyItem(@NotNull PlayerMarketProfile offer, @NotNull ItemStack originalItem) {
        StackBuilder builder = new StackBuilder(originalItem);

        builder.lore(" ");
        builder.lore(hex("<#555555>• <#aaaaaa>Sprzedawca: <#ffff55>" + offer.getSellerName()));
        builder.lore(hex("<#555555>• <#aaaaaa>Cena: <gradient:#55ff55:#00aa00><bold>" + offer.getPrice() + "$</bold></gradient>"));
        builder.lore(hex("<#555555>• <#aaaaaa>Wygasa za: <#ffaa00>" + MarketItemUtil.formatTimeLeft(offer.getCreatedAt())));
        builder.lore(" ");
        builder.lore(hex("<gradient:#ffff55:#ffaa00><bold>KLIKNIJ, ABY ZAKUPIĆ</bold></gradient>"));
        builder.lore(" ");

        return builder;
    }

    public static StackBuilder prepareManageItem(@NotNull PlayerMarketProfile offer, @NotNull ItemStack originalItem) {
        StackBuilder builder = new StackBuilder(originalItem);

        builder.lore(" ");
        builder.lore(hex("<#555555>• <#aaaaaa>Cena: <gradient:#55ff55:#00aa00>" + offer.getPrice() + "$</gradient>"));
        builder.lore(hex("<#555555>• <#aaaaaa>Wystawiono: <#ffffff>" + MarketItemUtil.formatElapsedTime(offer.getCreatedAt())));
        builder.lore(hex("<#555555>• <#aaaaaa>Pozostało: <#ffaa00>" + MarketItemUtil.formatTimeLeft(offer.getCreatedAt())));
        builder.lore(" ");
        builder.lore(hex("<gradient:#ff5555:#aa0000><bold>KLIKNIJ, ABY WYCOFAĆ</bold></gradient>"));
        builder.lore(" ");

        return builder;
    }

    public static StackBuilder prepareStorageItem(@NotNull PlayerMarketProfile offer, @NotNull ItemStack originalItem) {
        StackBuilder builder = new StackBuilder(originalItem);

        builder.lore(" ");
        builder.lore(hex("<#555555>• <#aaaaaa>Status: <bold><#ff5555>WYGASŁE</bold>"));
        builder.lore(hex("<#555555>• <#aaaaaa>Wystawiono: <#ffffff>" + MarketItemUtil.formatElapsedTime(offer.getCreatedAt())));
        builder.lore(" ");
        builder.lore(hex("<gradient:#ff5555:#aa0000><bold>KLIKNIJ, ABY ODEBRAĆ</bold></gradient>"));
        builder.lore(" ");

        return builder;
    }

    public static StackBuilder prepareClaimableItem(@NotNull PlayerMarketProfile offer, @NotNull ItemStack originalItem) {
        StackBuilder builder = new StackBuilder(originalItem);

        builder.lore(" ");
        builder.lore(hex("<#555555>• <#aaaaaa>Status: <bold><#55ffff>DO ODEBRANIA</bold>"));
        builder.lore(hex("<#555555>• <#aaaaaa>Powód: <#ffffff>Zakup / Brak miejsca"));
        builder.lore(hex("<#555555>• <#aaaaaa>Data: <#ffffff>" + MarketItemUtil.formatElapsedTime(offer.getCreatedAt())));
        builder.lore(" ");
        builder.lore(hex("<gradient:#55ffff:#00aaaa><bold>KLIKNIJ, ABY ODEBRAĆ</bold></gradient>"));
        builder.lore(" ");

        return builder;
    }

    public static StackBuilder prepareMyOffersIcon(int activeOffersCount) {
        String countColor = activeOffersCount > 0 ? "<#ffff55>" : "<#aaaaaa>";
        return new StackBuilder(new ItemStack(Material.BOOK))
                .name(hex("<gradient:#ffcc00:#ffaa00><bold>Twoje aktywne przedmioty</bold></gradient>"))
                .lore(hex("<#aaaaaa>Zarządzaj przedmiotami,"))
                .lore(hex("<#aaaaaa>które wystawiłeś na sprzedaż."))
                .lore(" ")
                .lore(hex("<#555555>• <#aaaaaa>Wystawione: " + countColor + activeOffersCount))
                .lore(" ")
                .lore(hex("<#ffff55>Kliknij, aby zarządzać!"));
    }

    public static StackBuilder prepareClaimableIcon(int claimableCount) {
        String countColor = claimableCount > 0 ? "<#55ffff>" : "<#aaaaaa>";
        String titleColor = claimableCount > 0 ? "<gradient:#55ffff:#00aaaa>" : "<#aaaaaa>";

        return new StackBuilder(new ItemStack(Material.ENDER_CHEST))
                .name(hex(titleColor + "<bold>Depozyt</bold> <#aaaaaa>(Zakupione przedmioty)"))
                .lore(hex("<#aaaaaa>Przedmioty, które kupiłeś,"))
                .lore(hex("<#aaaaaa>ale nie miałeś miejsca w ekwipunku."))
                .lore(" ")
                .lore(hex("<#555555>• <#aaaaaa>Do odebrania: " + countColor + claimableCount))
                .lore(" ")
                .lore(hex("<#55ffff>Kliknij, aby odebrać!"));
    }

    public static StackBuilder prepareStorageIcon(int expiredCount) {
        String countColor = expiredCount > 0 ? "<#ff5555>" : "<#aaaaaa>";
        String titleColor = expiredCount > 0 ? "<gradient:#ff5555:#aa0000>" : "<#aaaaaa>";

        return new StackBuilder(new ItemStack(Material.CHEST))
                .name(hex(titleColor + "<bold>Magazyn</bold> <#aaaaaa>(Wygasłe przedmioty)"))
                .lore(hex("<#aaaaaa>Twoje przedmioty, które wygasły"))
                .lore(hex("<#aaaaaa>i czekają na odbiór."))
                .lore(" ")
                .lore(hex("<#555555>• <#aaaaaa>Do odebrania: " + countColor + expiredCount))
                .lore(" ")
                .lore(hex("<#ff5555>Kliknij, aby odebrać!"));
    }
}