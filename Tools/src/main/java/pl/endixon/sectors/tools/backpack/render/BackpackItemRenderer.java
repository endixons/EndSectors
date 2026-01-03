package pl.endixon.sectors.tools.backpack.render;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import pl.endixon.sectors.tools.inventory.api.builder.StackBuilder;

import java.text.SimpleDateFormat;
import java.util.Date;

@RequiredArgsConstructor
public final class BackpackItemRenderer {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.legacySection();
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm");

    private static final String MAIN_GRADIENT = "<gradient:#00d2ff:#3a7bd5><bold>";
    private static final String ACCENT = "<#00d2ff>";
    private static final String TEXT = "<#a8a8a8>";
    private static final String SEPARATOR = "<#555555>";
    private static final String ADMIN_GRADIENT = "<gradient:#ed213a:#93291e><bold>";
    private static final String ADMIN_ACCENT = "<#ff5f6d>";

    private static String hex(String text) {
        return SERIALIZER.serialize(MM.deserialize(text));
    }

    private static String getFinanceStatus(double balance, double cost, boolean showPercent) {
        if (!showPercent) {
            return "<#00ff87>" + String.format("%.2f", balance) + "$";
        }
        double percent = (cost <= 0) ? 100.0 : Math.min((balance / cost) * 100.0, 100.0);
        String color = percent >= 100 ? "<#00ff87>" : (percent > 50 ? "<#fbff00>" : "<#ff4b2b>");
        return "<#00ff87>" + String.format("%.2f", balance) + "$ " + TEXT + "(" + color + String.format("%.1f", percent) + "%" + TEXT + ")";
    }

    public static StackBuilder prepareInfoIcon(int page, int maxPages, double balance, long expiry, int expiredCount, double bulkCost) {
        boolean isExpired = System.currentTimeMillis() > expiry && page != 1;
        String expiryDate = page == 1 ? "Stały dostęp" : DATE_FORMAT.format(new Date(expiry));

        StackBuilder builder = new StackBuilder(new ItemStack(Material.BOOK))
                .name(hex(MAIN_GRADIENT + "INFORMACJE O STRONIE"))
                .lore(hex(TEXT + "Strona: " + ACCENT + page + " " + SEPARATOR + "/ " + maxPages))
                .lore(hex(TEXT + "Ważność: " + (isExpired ? "<#ff4b2b>WYGASŁA" : "<#00ff87>" + expiryDate)))
                .lore(hex(TEXT + "Status portfela: <#00ff87>" + String.format("%.2f", balance) + "$"));

        if (expiredCount > 0) {
            builder.lore(" ")
                    .lore(hex("<#fbff00><bold>OPŁAĆ WSZYSTKIE STRONY"))
                    .lore(hex(TEXT + "Masz " + ADMIN_ACCENT + expiredCount + TEXT + " wygasłych stron."))
                    .lore(hex(TEXT + "Koszt całkowity: <#00ff87>" + String.format("%.2f", bulkCost) + "$"))
                    .lore(" ")
                    .lore(hex(TEXT + "Status portfela: " + getFinanceStatus(balance, bulkCost,true)))
                    .lore(hex(ACCENT + "ŚRODKOWY KLIK » <#ffffff>Opłać wszystko"))
                    .glow(true);
        }

        builder.lore(" ")
                .lore(hex(SEPARATOR + "<italic>Tryb synchronizacji: Real-time</italic>"));

        return builder;
    }

    public static StackBuilder prepareActionSaveExit(boolean isActive) {
        if (isActive) {
            return new StackBuilder(new ItemStack(Material.LIME_DYE))
                    .name(hex("<#00ff87><bold>ZARZĄDZANIE ZMIANAMI"))
                    .lore(hex("<#00ff87>LPM » <#ffffff>Zapisz i wyjdź"))
                    .lore(hex("<#ff5f6d>PPM » <#ffffff>Anuluj i wyjdź"))
                    .glow(true);
        } else {
            return new StackBuilder(new ItemStack(Material.RED_DYE))
                    .name(hex("<#ff4b2b><bold>ZAMKNIJ PLECAK"))
                    .lore(hex(TEXT + "Strona jest zablokowana."))
                    .lore(hex("<#ff5f6d>Kliknij » <#ffffff>Wyjdź"));
        }
    }

    public static StackBuilder prepareBreachWarning(boolean needsRenew, double cost, double balance, int expiredCount) {
        final StackBuilder builder = new StackBuilder(new ItemStack(needsRenew ? Material.BARRIER : Material.WITHER_SKELETON_SKULL))
                .name(hex(needsRenew ? "<#ff4b2b><bold>STRONA ZABLOKOWANA" : "<#ff4b2b><bold>RYZYKO UTRATY PRZEDMIOTÓW"));

        if (needsRenew) {
            builder.lore(hex("<#ff4b2b>Ważność tej strony wygasła!"))
                    .lore(hex(TEXT + "Dostęp do przedmiotów został ograniczony."))
                    .lore(" ")
                    .lore(hex("<#00ff87>LPM » <#ffffff>Opłać tę stronę: <#ffff55>" + String.format("%.2f", cost) + "$"))
                    .lore(hex(TEXT + "Status portfela: " + getFinanceStatus(balance, cost,true)))
                    .lore(" ")
                    .lore(hex("<#fbff00>LUB » <#ffffff>Opłać wszystkie strony (" + expiredCount + ")"))
                    .lore(hex(TEXT + "klikając <#00d2ff>ŚRODKOWYM <#a8a8a8>w ikonę <#00d2ff>KSIĄŻKI<#a8a8a8>!"))
                    .lore(hex(SEPARATOR + "  "));
        }

        builder.lore(hex("<#ff4b2b>!!! WAŻNE !!!"))
                .lore(hex(TEXT + "Podczas śmierci istnieje 5% szansy,"))
                .lore(hex(TEXT + "że " + ACCENT + "LOSOWY " + TEXT + "przedmiot wypadnie z Twojego"))
                .lore(hex(TEXT + "plecaka - " + ADMIN_ACCENT + "NAWET Z ZABLOKOWANYCH STRON" + TEXT + "!"))
                .lore(" ")
                .lore(hex(SEPARATOR + "<italic>Dbaj o regularne opłaty!</italic>"));

        if (needsRenew) builder.glow(true);
        return builder;
    }

    public static StackBuilder prepareUpgradeButton(int nextPage, double cost, double balance) {
        return new StackBuilder(new ItemStack(Material.NETHER_BRICK))
                .name(hex("<#fbff00><bold>ODBLOKUJ STRONĘ " + nextPage))
                .lore(hex(TEXT + "Kliknij, aby dokonać zakupu."))
                .lore(" ")
                .lore(hex(TEXT + "Koszt: <#00ff87>" + String.format("%.2f", cost) + "$"))
                .lore(hex(TEXT + "Status portfela: " + getFinanceStatus(balance, cost,true)))
                .lore(" ")
                .lore(hex(TEXT + "Otrzymasz: " + ACCENT + "Nowa strone + 7 dni ważności"))
                .glow(true);
    }

    public static StackBuilder prepareAdminGlobalRenew() {
        return new StackBuilder(new ItemStack(Material.ENCHANTED_BOOK))
                .name(hex("<#ff5f6d><bold>GLOBALNE ODNOWIENIE"))
                .lore(hex(TEXT + "Ustawia ważność " + ACCENT + "WSZYSTKICH " + TEXT + "stron na 7 dni."))
                .glow(true);
    }

    public static StackBuilder prepareAdminWipeAll() {
        return new StackBuilder(new ItemStack(Material.LAVA_BUCKET))
                .name(hex("<#ff0000><bold>WIPE CAŁEGO PLECAKA"))
                .lore(hex(TEXT + "Czyści zawartość każdej strony."))
                .lore(hex("<#ff4b2b>Operacja nieodwracalna!"));
    }

    public static StackBuilder prepareAdminRemovePage() {
        return new StackBuilder(new ItemStack(Material.BARRIER))
                .name(hex("<#ff4b2b><bold>USUŃ OSTATNIĄ STRONĘ"))
                .lore(hex(TEXT + "Zmniejsza liczbę stron o 1."));
    }

    public static StackBuilder prepareAdminInfo(String targetName, int page, int maxPages) {
        return new StackBuilder(new ItemStack(Material.OBSERVER))
                .name(hex(ADMIN_GRADIENT + "ZARZĄDZANIE STRONĄ"))
                .lore(hex(TEXT + "Właściciel: " + ADMIN_ACCENT + targetName))
                .lore(hex(TEXT + "Strona: " + ADMIN_ACCENT + page + " / " + maxPages))
                .lore(" ")
                .lore(hex("<#00ff87>LPM » <#a8a8a8>Dodaj 7 dni"))
                .lore(hex("<#ff5f6d>PPM » <#a8a8a8>Odejmij 7 dni"))
                .lore(hex("<#ff0000>SCROLL » <#a8a8a8>Resetuj czas"));
    }

    public static StackBuilder prepareSaveButton(boolean isAdmin) {
        String grad = isAdmin ? ADMIN_GRADIENT : "<gradient:#00ff87:#00aa00><bold>";
        return new StackBuilder(new ItemStack(isAdmin ? Material.NETHER_STAR : Material.LIME_DYE))
                .name(hex(grad + "ZAPISZ ZMIANY"))
                .lore(hex(isAdmin ? TEXT + "Wymuś synchronizację." : TEXT + "Zaktualizuj dane."))
                .glow(true);
    }
}