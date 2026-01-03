package pl.endixon.sectors.tools.inventory.backpack;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.endixon.sectors.tools.backpack.BackpackService;
import pl.endixon.sectors.tools.backpack.render.BackpackItemRenderer;
import pl.endixon.sectors.tools.backpack.utils.BackpackUtils;
import pl.endixon.sectors.tools.inventory.api.WindowUI;
import pl.endixon.sectors.tools.inventory.api.builder.StackBuilder;
import pl.endixon.sectors.tools.user.profile.player.PlayerBackpackProfile;
import pl.endixon.sectors.tools.user.profile.player.PlayerProfile;
import pl.endixon.sectors.tools.utils.PlayerDataSerializerUtil;

import java.util.Map;

public class BackpackAdminWindow {

    private final Player admin;
    private final PlayerProfile targetProfile;
    private final PlayerBackpackProfile targetBackpack;
    private final int page;
    private final BackpackService service;

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final int STORAGE_SLOTS = 45;
    private static final long WEEK_MS = 1000L * 60 * 60 * 24 * 7;

    public BackpackAdminWindow(Player admin, PlayerProfile target, PlayerBackpackProfile backpack, int page, BackpackService service) {
        this.admin = admin;
        this.targetProfile = target;
        this.targetBackpack = backpack;
        this.page = page;
        this.service = service;
        this.open();
    }

    public void open() {
        final int totalPages = this.targetBackpack.getUnlockedPages();
        final String title = "<gradient:#ed213a:#93291e><bold>ADMIN:</bold></gradient> <#aaaaaa>" + this.targetProfile.getName() + " <#ff5f6d>[" + this.page + "]";
        final WindowUI window = new WindowUI(title, 6);

        final String base64 = this.targetBackpack.getPages().getOrDefault(String.valueOf(this.page), "");
        final ItemStack[] items = PlayerDataSerializerUtil.deserializeItemStacksFromBase64(base64);

        for (int i = 0; i < STORAGE_SLOTS; i++) {
            if (items.length > i && items[i] != null) {
                window.getInventory().setItem(i, items[i]);
            }
        }

        window.setInteractionAllowed(true);
        this.setupAdminNavigation(window, totalPages);
        this.admin.openInventory(window.getInventory());
    }

    private void setupAdminNavigation(WindowUI window, int totalPages) {
        final ItemStack filler = new StackBuilder(new ItemStack(Material.GRAY_STAINED_GLASS_PANE)).name(" ").build();
        for (int i = 45; i < 54; i++) {
            window.setSlot(i, filler, e -> e.setCancelled(true));
        }

        if (this.page > 1) {
            window.setSlot(45, new StackBuilder(new ItemStack(Material.ARROW)).name("<#ff5f6d>← Poprzednia").build(), e -> {
                BackpackUtils.updateBackpackFromInventory(e.getInventory(), this.targetBackpack, this.page);
                new BackpackAdminWindow(this.admin, this.targetProfile, this.targetBackpack, this.page - 1, this.service);
            });
        }

        // Slot 46: GLOBALNE ZARZĄDZANIE CZASEM
        window.setSlot(46, BackpackItemRenderer.prepareAdminGlobalRenew().build(), e -> {
            String action = "";
            for (int i = 2; i <= totalPages; i++) {
                String pStr = String.valueOf(i);
                long current = this.targetBackpack.getPageExpirations().getOrDefault(pStr, System.currentTimeMillis());

                if (e.isLeftClick()) {
                    this.targetBackpack.getPageExpirations().put(pStr, Math.max(System.currentTimeMillis(), current) + WEEK_MS);
                    action = "odnowiono o 7 dni";
                } else if (e.isRightClick()) {
                    this.targetBackpack.getPageExpirations().put(pStr, current - WEEK_MS);
                    action = "skrócono o 7 dni";
                } else if (e.getClick().name().contains("MIDDLE")) {
                    this.targetBackpack.getPageExpirations().put(pStr, 0L);
                    action = "ustawiono jako wygasłe";
                }
            }
            this.admin.sendMessage(MM.deserialize("<#ed213a>[!] <#a8a8a8>Globalnie " + action + " wszystkie strony."));
            this.admin.playSound(this.admin.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.0f);
            new BackpackAdminWindow(this.admin, this.targetProfile, this.targetBackpack, this.page, this.service);
        });

        if (totalPages > 1) {
            window.setSlot(47, BackpackItemRenderer.prepareAdminRemovePage().build(), e -> {
                this.targetBackpack.getPages().remove(String.valueOf(totalPages));
                this.targetBackpack.getPageExpirations().remove(String.valueOf(totalPages));
                this.targetBackpack.setUnlockedPages(totalPages - 1);
                this.service.saveBackpack(this.targetBackpack);
                this.admin.sendMessage(MM.deserialize("<#ed213a>[!] <#a8a8a8>Usunięto stronę <#ff5f6d>" + totalPages));
                new BackpackAdminWindow(this.admin, this.targetProfile, this.targetBackpack, Math.min(this.page, totalPages - 1), this.service);
            });
        }

        window.setSlot(48, BackpackItemRenderer.prepareSaveButton(true).build(), e -> {
            BackpackUtils.updateBackpackFromInventory(e.getInventory(), this.targetBackpack, this.page);
            this.service.saveBackpack(this.targetBackpack);
            this.admin.closeInventory();
            this.admin.sendMessage(MM.deserialize("<#ed213a>[!] <#a8a8a8>Zsynchronizowano plecak gracza <#ff5f6d>" + this.targetProfile.getName()));
        });

        // Slot 49: INFO / ZARZĄDZANIE CZASEM TEJ STRONY
        if (this.page == 1) {
            window.setSlot(49, new StackBuilder(new ItemStack(Material.ENCHANTED_BOOK))
                    .name("<#00d2ff><bold>STRONA STARTOWA")
                    .lore("<#a8a8a8>Właściciel: <#ff5f6d>" + this.targetProfile.getName())
                    .lore("<#a8a8a8>Status: <#00ff87>DOŻYWOTNIA")
                    .build(), e -> e.setCancelled(true));
        } else {
            window.setSlot(49, BackpackItemRenderer.prepareAdminInfo(this.targetProfile.getName(), this.page, totalPages).build(), e -> {
                String pStr = String.valueOf(this.page);
                long current = this.targetBackpack.getPageExpirations().getOrDefault(pStr, System.currentTimeMillis());

                if (e.isLeftClick()) {
                    this.targetBackpack.getPageExpirations().put(pStr, Math.max(System.currentTimeMillis(), current) + WEEK_MS);
                    this.admin.sendMessage(MM.deserialize("<#ed213a>[!] <#a8a8a8>Dodano tydzień do strony <#ff5f6d>" + this.page));
                } else if (e.isRightClick()) {
                    this.targetBackpack.getPageExpirations().put(pStr, current - WEEK_MS);
                    this.admin.sendMessage(MM.deserialize("<#ed213a>[!] <#a8a8a8>Odjęto tydzień od strony <#ff5f6d>" + this.page));
                } else if (e.getClick().name().contains("MIDDLE")) {
                    this.targetBackpack.getPageExpirations().put(pStr, 0L);
                    this.admin.sendMessage(MM.deserialize("<#ed213a>[!] <#ff0000>Zresetowano ważność strony " + this.page));
                }

                this.admin.playSound(this.admin.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 1.0f, 1.0f);
                new BackpackAdminWindow(this.admin, this.targetProfile, this.targetBackpack, this.page, this.service);
            });
        }

        window.setSlot(50, new StackBuilder(new ItemStack(Material.TNT_MINECART)).name("<#ff0000><bold>CZYŚĆ STRONĘ " + this.page).build(), e -> {
            this.targetBackpack.getPages().put(String.valueOf(this.page), "");
            this.admin.sendMessage(MM.deserialize("<#ed213a>[!] <#a8a8a8>Wyczyszczono stronę <#ff5f6d>" + this.page));
            new BackpackAdminWindow(this.admin, this.targetProfile, this.targetBackpack, this.page, this.service);
        });

        window.setSlot(51, BackpackItemRenderer.prepareAdminWipeAll().build(), e -> {
            for (Map.Entry<String, String> entry : this.targetBackpack.getPages().entrySet()) {
                entry.setValue("");
            }
            this.admin.sendMessage(MM.deserialize("<#ff0000>[!!!] <#a8a8a8>Zrobiono totalny WIPE plecaka!"));
            this.admin.playSound(this.admin.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
            new BackpackAdminWindow(this.admin, this.targetProfile, this.targetBackpack, this.page, this.service);
        });

        if (this.page < totalPages) {
            window.setSlot(53, new StackBuilder(new ItemStack(Material.ARROW)).name("<#ff5f6d>Następna →").build(), e -> {
                BackpackUtils.updateBackpackFromInventory(e.getInventory(), this.targetBackpack, this.page);
                new BackpackAdminWindow(this.admin, this.targetProfile, this.targetBackpack, this.page + 1, this.service);
            });
        } else if (totalPages < 18) {
            window.setSlot(53, new StackBuilder(new ItemStack(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE)).name("<#ff5f6d><bold>FORCE UNLOCK").build(), e -> {
                this.targetBackpack.setUnlockedPages(totalPages + 1);
                this.targetBackpack.getPageExpirations().put(String.valueOf(totalPages + 1), System.currentTimeMillis() + WEEK_MS);
                this.service.saveBackpack(this.targetBackpack);
                this.admin.sendMessage(MM.deserialize("<#ed213a>[!] <#a8a8a8>Odblokowano stronę <#ff5f6d>" + (totalPages + 1)));
                new BackpackAdminWindow(this.admin, this.targetProfile, this.targetBackpack, this.page + 1, this.service);
            });
        }
    }
}