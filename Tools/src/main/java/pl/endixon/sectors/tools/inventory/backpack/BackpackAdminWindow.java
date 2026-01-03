package pl.endixon.sectors.tools.inventory.backpack;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
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

public class BackpackAdminWindow {

    private final Player admin;
    private final PlayerProfile targetProfile;
    private final PlayerBackpackProfile targetBackpack;
    private final int page;
    private final BackpackService service;

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final int STORAGE_SLOTS = 45;

    public BackpackAdminWindow(Player admin, PlayerProfile target, PlayerBackpackProfile backpack, int page, BackpackService service) {
        this.admin = admin;
        this.targetProfile = target;
        this.targetBackpack = backpack;
        this.page = page;
        this.service = service;
        this.open();
    }

    public void open() {
        final int totalPages = targetBackpack.getUnlockedPages();
        final String title = "<gradient:#ed213a:#93291e><bold>ADMINISTRATION:</bold></gradient> <#aaaaaa>" + targetProfile.getName() + " <#ff5f6d>[" + page + "]";
        final WindowUI window = new WindowUI(title, 6);

        final String base64 = targetBackpack.getPages().getOrDefault(String.valueOf(page), "");
        final ItemStack[] items = PlayerDataSerializerUtil.deserializeItemStacksFromBase64(base64);


        for (int i = 0; i < STORAGE_SLOTS; i++) {
            if (items.length > i && items[i] != null) {
                window.getInventory().setItem(i, items[i]);
            }
        }

        window.setInteractionAllowed(true);

        this.setupAdminNavigation(window, totalPages);
        admin.openInventory(window.getInventory());
    }

    private void setupAdminNavigation(WindowUI window, int totalPages) {
        final ItemStack filler = new StackBuilder(new ItemStack(Material.BLACK_STAINED_GLASS_PANE)).name(" ").build();
        for (int i = 45; i < 54; i++) {
            window.setSlot(i, filler, e -> e.setCancelled(true));
        }

        if (page > 1) {
            final ItemStack prev = new StackBuilder(new ItemStack(Material.ARROW)).name("<#ff5f6d>← Poprzednia").build();
            window.setSlot(45, prev, e -> {
                e.setCancelled(true);
                BackpackUtils.updateBackpackFromInventory(e.getInventory(), targetBackpack, page);
                new BackpackAdminWindow(admin, targetProfile, targetBackpack, page - 1, service);
            });
        }

        window.setSlot(49, BackpackItemRenderer.prepareAdminInfo(targetProfile.getName(), page, totalPages).build(), e -> e.setCancelled(true));

        if (page < totalPages) {
            final ItemStack next = new StackBuilder(new ItemStack(Material.ARROW)).name("<#ff5f6d>Następna →").build();
            window.setSlot(53, next, e -> {
                e.setCancelled(true);
                BackpackUtils.updateBackpackFromInventory(e.getInventory(), targetBackpack, page);
                new BackpackAdminWindow(admin, targetProfile, targetBackpack, page + 1, service);
            });
        }

        window.setSlot(48, BackpackItemRenderer.prepareSaveButton(true).build(), e -> {
            e.setCancelled(true);
            BackpackUtils.updateBackpackFromInventory(e.getInventory(), targetBackpack, page);
            service.saveBackpack(targetBackpack);
            admin.closeInventory();
            admin.sendMessage(MM.deserialize("<gradient:#ed213a:#93291e><bold>SYSTEM:</bold></gradient> <#aaaaaa>Zaktualizowano plecak gracza <#ff5f6d>" + targetProfile.getName()));
        });
    }
}