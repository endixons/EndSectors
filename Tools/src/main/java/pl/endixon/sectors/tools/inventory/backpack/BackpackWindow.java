package pl.endixon.sectors.tools.inventory.backpack;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.inventory.ClickType;
import pl.endixon.sectors.tools.backpack.BackpackService;
import pl.endixon.sectors.tools.backpack.type.BackpackUpgradeResult;
import pl.endixon.sectors.tools.backpack.utils.BackpackUtils;
import pl.endixon.sectors.tools.inventory.api.WindowUI;
import pl.endixon.sectors.tools.inventory.api.builder.StackBuilder;
import pl.endixon.sectors.tools.backpack.render.BackpackItemRenderer;
import pl.endixon.sectors.tools.user.profile.player.PlayerBackpackProfile;
import pl.endixon.sectors.tools.user.profile.player.PlayerProfile;
import pl.endixon.sectors.tools.utils.PlayerDataSerializerUtil;

public class BackpackWindow {

    private final Player player;
    private final PlayerProfile profile;
    private final PlayerBackpackProfile backpack;
    private final int page;
    private final BackpackService service;
    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final double PAGE_COST = 50000.0;
    private static final double RENEW_COST = 25000.0;

    public BackpackWindow(Player player, PlayerProfile profile, PlayerBackpackProfile backpack, int page, BackpackService service) {
        this.player = player;
        this.profile = profile;
        this.backpack = backpack;
        this.page = page;
        this.service = service;
        this.open();
    }

    public void open() {
        final int unlocked = this.service.getMaxPages(this.player, this.backpack);
        final int currentPage = Math.min(this.page, unlocked + 1);

        boolean isActive = (currentPage == 1) || (currentPage <= unlocked && this.service.isPageActive(this.backpack, currentPage));
        if (this.player.hasPermission("endsectors.backpack.admin")) isActive = true;

        final WindowUI window = new WindowUI("<gradient:#00d2ff:#3a7bd5><bold>BACKPACK</bold></gradient> <#a8a8a8>P:" + currentPage, 6);

        if (isActive && currentPage <= unlocked) {
            final String base64 = this.backpack.getPages().getOrDefault(String.valueOf(currentPage), "");
            final ItemStack[] items = PlayerDataSerializerUtil.deserializeItemStacksFromBase64(base64);
            for (int i = 0; i < 45; i++) {
                if (items.length > i && items[i] != null) window.getInventory().setItem(i, items[i]);
            }
            window.setInteractionAllowed(true);
        } else {
            window.setInteractionAllowed(false);
        }

        this.setupNavigation(window, unlocked, isActive, currentPage);
        this.player.openInventory(window.getInventory());
    }

    private void setupNavigation(WindowUI window, int maxPages, boolean isActive, int currentPage) {
        final ItemStack filler = new StackBuilder(new ItemStack(Material.BLACK_STAINED_GLASS_PANE)).name(" ").build();
        for (int i = 45; i < 54; i++) window.setSlot(i, filler, e -> e.setCancelled(true));

        if (currentPage > 1) {
            window.setSlot(45, new StackBuilder(new ItemStack(Material.ARROW)).name("<#00d2ff>← Poprzednia").build(), e -> {
                if (isActive) BackpackUtils.updateBackpackFromInventory(e.getInventory(), this.backpack, currentPage);
                new BackpackWindow(this.player, this.profile, this.backpack, currentPage - 1, this.service);
            });
        }

        int tempExpiredCount = 0;

        for (int i = 2; i <= maxPages; i++) {
            if (!this.service.isPageActive(this.backpack, i)) tempExpiredCount++;
        }

        final int expiredCount = tempExpiredCount;
        final double bulkCost = expiredCount * RENEW_COST;
        final long exp = this.backpack.getPageExpirations().getOrDefault(String.valueOf(currentPage), 0L);
        final boolean needsRenew = (currentPage != 1) && !isActive && currentPage <= maxPages && !this.player.hasPermission("endsectors.backpack.admin");

        window.setSlot(48, BackpackItemRenderer.prepareActionSaveExit(isActive).build(), e -> {

            if (isActive && e.isLeftClick()) {
                BackpackUtils.updateBackpackFromInventory(e.getInventory(), this.backpack, currentPage);
                this.service.saveBackpack(this.backpack);
                this.player.sendMessage(MM.deserialize("<#00ff87>[!] Zmiany w plecaku zostały pomyślnie zapisane."));
            }
            this.player.closeInventory();
            this.player.playSound(this.player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 0.8f);
        });



        window.setSlot(49, BackpackItemRenderer.prepareInfoIcon(currentPage, maxPages, this.profile.getBalance(), exp, expiredCount, bulkCost).build(), e -> {
            boolean isBulkClick = e.getClick() == ClickType.MIDDLE || e.isShiftClick();

            if (expiredCount > 0 && isBulkClick) {
                if (this.service.processBulkSubscription(this.player, this.profile, this.backpack, RENEW_COST) == BackpackUpgradeResult.SUCCESS) {
                    this.player.playSound(this.player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
                    this.player.sendMessage(MM.deserialize("<#00ff87>[!] Pomyślnie opłacono wszystkie zaległe strony (" + expiredCount + ")!"));
                    this.open();
                } else {
                    this.player.playSound(this.player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    this.player.sendMessage(MM.deserialize("<#ff4b2b>[!] Błąd płatności! Wymagane: <#ffff55>" +
                            String.format("%.2f", bulkCost) + "$ <#ff4b2b>| Posiadasz: <#ffff55>" +
                            String.format("%.2f", this.profile.getBalance()) + "$"));
                }
            }
        });


        window.setSlot(50, BackpackItemRenderer.prepareBreachWarning(needsRenew, RENEW_COST, this.profile.getBalance(), expiredCount).build(), e -> {

            if (needsRenew && e.isLeftClick()) {
                if (this.service.processSubscription(this.profile, this.backpack, currentPage, RENEW_COST) == BackpackUpgradeResult.SUCCESS) {
                    this.player.playSound(this.player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
                    this.player.sendMessage(MM.deserialize("<#00ff87>[!] Strona została opłacona na 7 dni!"));
                    this.open();
                } else {
                    this.player.playSound(this.player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    this.player.sendMessage(MM.deserialize("<#ff4b2b>[!] Błąd płatności! Wymagane: <#ffff55>" +
                            String.format("%.2f", RENEW_COST) + "$ <#ff4b2b>| Posiadasz: <#ffff55>" +
                            String.format("%.2f", this.profile.getBalance()) + "$"));
                }
            }
        });

        if (currentPage < maxPages) {
            window.setSlot(53, new StackBuilder(new ItemStack(Material.ARROW)).name("<#00d2ff>Następna →").build(), e -> {
                if (isActive) BackpackUtils.updateBackpackFromInventory(e.getInventory(), this.backpack, currentPage);
                new BackpackWindow(this.player, this.profile, this.backpack, currentPage + 1, this.service);
            });

        } else if (currentPage == maxPages && maxPages < 18) {
            window.setSlot(53, BackpackItemRenderer.prepareUpgradeButton(maxPages + 1, PAGE_COST, this.profile.getBalance()).build(), e -> {

                if (this.service.processPageUpgrade(this.profile, this.backpack, PAGE_COST, maxPages) == BackpackUpgradeResult.SUCCESS) {
                    this.player.playSound(this.player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
                    this.player.sendMessage(MM.deserialize("<#00ff87>[!] Gratulacje! Odblokowano nową stronę plecaka: <#00d2ff>" + (maxPages + 1)));
                    new BackpackWindow(this.player, this.profile, this.backpack, maxPages + 1, this.service);

                } else {

                    this.player.playSound(this.player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    this.player.sendMessage(MM.deserialize("<#ff4b2b>[!] Brak środków na nową stronę! Wymagane: <#ffff55>" +
                            String.format("%.2f", PAGE_COST) + "$ <#ff4b2b>| Posiadasz: <#ffff55>" +
                            String.format("%.2f", this.profile.getBalance()) + "$"));
                }
            });
        }
    }
}