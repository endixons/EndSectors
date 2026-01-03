package pl.endixon.sectors.tools.backpack.render;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import pl.endixon.sectors.tools.inventory.api.builder.StackBuilder;

@RequiredArgsConstructor
public final class BackpackItemRenderer {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.legacySection();

    private static final String MAIN_GRADIENT = "<gradient:#00d2ff:#3a7bd5><bold>";
    private static final String ACCENT = "<#00d2ff>";
    private static final String TEXT = "<#a8a8a8>";
    private static final String ADMIN_GRADIENT = "<gradient:#ed213a:#93291e><bold>";
    private static final String ADMIN_ACCENT = "<#ff5f6d>";

    private static String hex(String text) {
        return SERIALIZER.serialize(MM.deserialize(text));
    }

    public static StackBuilder prepareInfoIcon(int page, int maxPages, double balance) {
        return new StackBuilder(new ItemStack(Material.BOOK))
                .name(hex(MAIN_GRADIENT + "STATUS SESJI"))
                .lore(hex(TEXT + "Strona: " + ACCENT + page + " / " + maxPages))
                .lore(hex(TEXT + "Portfel: <#00ff87>" + balance + "$"))
                .lore(" ")
                .lore(hex("<#555555><italic>Tryb: Transactional</italic>"));
    }

    public static StackBuilder prepareUpgradeButton(int nextPage, double cost) {
        return new StackBuilder(new ItemStack(Material.NETHER_BRICK))
                .name(hex("<#fbff00><bold>ODBLOKUJ STRONĘ " + nextPage + "</bold>"))
                .lore(hex(TEXT + "Koszt: <#00ff87>" + cost + "$"))
                .lore(" ")
                .lore(hex(ACCENT + "Kliknij, aby rozszerzyć miejsce!"))
                .glow(true);
    }

    public static StackBuilder prepareAdminInfo(String targetName, int page, int maxPages) {
        return new StackBuilder(new ItemStack(Material.OBSERVER))
                .name(hex(ADMIN_GRADIENT + "TRYB INSPEKCJI"))
                .lore(hex(TEXT + "Cel: " + ADMIN_ACCENT + targetName))
                .lore(hex(TEXT + "Strona: " + ADMIN_ACCENT + page + " / " + maxPages));
    }

    public static StackBuilder prepareSaveButton(boolean isAdmin) {
        String grad = isAdmin ? ADMIN_GRADIENT : "<gradient:#00ff87:#00aa00><bold>";
        return new StackBuilder(new ItemStack(isAdmin ? Material.NETHER_STAR : Material.LIME_DYE))
                .name(hex(grad + "ZAPISZ ZMIANY"))
                .lore(hex(TEXT + "Synchronizuj z bazą danych."))
                .glow(true);
    }
}