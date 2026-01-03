package pl.endixon.sectors.tools.inventory.backpack;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
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
    private static final int STORAGE_SLOTS = 45;
    private static final double PAGE_COST = 50000.0;

    public BackpackWindow(Player player, PlayerProfile profile, PlayerBackpackProfile backpack, int page, BackpackService service) {
        this.player = player;
        this.profile = profile;
        this.backpack = backpack;
        this.page = page;
        this.service = service;
        this.open();
    }

    public void open() {
        final int maxAllowed = this.service.getMaxPages(player, backpack);
        final int currentPage = Math.min(page, maxAllowed);

        final WindowUI window = new WindowUI("<gradient:#00d2ff:#3a7bd5><bold>BACKPACK</bold></gradient> <#a8a8a8>P:" + currentPage, 6);

        final String base64 = backpack.getPages().getOrDefault(String.valueOf(currentPage), "");
        final ItemStack[] items = PlayerDataSerializerUtil.deserializeItemStacksFromBase64(base64);

        for (int i = 0; i < STORAGE_SLOTS; i++) {
            if (items.length > i && items[i] != null) {
                window.getInventory().setItem(i, items[i]);
            }
        }

        window.setInteractionAllowed(true);

        this.setupNavigation(window, maxAllowed);
        player.openInventory(window.getInventory());
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 1.1f);
    }

    private void setupNavigation(WindowUI window, int maxPages) {

        final ItemStack filler = new StackBuilder(new ItemStack(Material.BLACK_STAINED_GLASS_PANE)).name(" ").build();
        for (int i = 45; i < 54; i++) {
            window.setSlot(i, filler, e -> e.setCancelled(true));
        }

        if (page > 1) {
            final ItemStack prev = new StackBuilder(new ItemStack(Material.ARROW)).name("§b← Poprzednia").build();
            window.setSlot(45, prev, e -> {
                e.setCancelled(true);
                BackpackUtils.updateBackpackFromInventory(e.getInventory(), backpack, page);
                new BackpackWindow(player, profile, backpack, page - 1, service);
            });
        }

        window.setSlot(49, BackpackItemRenderer.prepareInfoIcon(page, maxPages, profile.getBalance()).build(), e -> e.setCancelled(true));

        window.setSlot(48, BackpackItemRenderer.prepareSaveButton(false).build(), e -> {
            e.setCancelled(true);
            BackpackUtils.updateBackpackFromInventory(e.getInventory(), backpack, page);
            service.saveBackpack(backpack);
            player.sendMessage(MM.deserialize("<#00ff87><bold>[!]</bold> <#a8a8a8>Pomyślnie zsynchronizowano dane plecaka."));
            player.closeInventory();
        });

        if (page < maxPages) {
            final ItemStack next = new StackBuilder(new ItemStack(Material.ARROW)).name("§bNastępna →").build();
            window.setSlot(53, next, e -> {
                e.setCancelled(true);
                BackpackUtils.updateBackpackFromInventory(e.getInventory(), backpack, page);
                new BackpackWindow(player, profile, backpack, page + 1, service);
            });
        } else if (maxPages < 18) {
            window.setSlot(53, BackpackItemRenderer.prepareUpgradeButton(maxPages + 1, PAGE_COST).build(), e -> {
                e.setCancelled(true);

                final BackpackUpgradeResult result = service.processPageUpgrade(profile, backpack, PAGE_COST, maxPages);

                switch (result) {
                    case SUCCESS -> {
                        player.sendMessage(MM.deserialize("<#00ff87><bold>[!]</bold> <#a8a8a8>Odblokowano stały slot na stronę <#00ff87>" + (maxPages + 1)));
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
                        new BackpackWindow(player, profile, backpack, page + 1, service);
                    }
                    case INSUFFICIENT_FUNDS -> {
                        player.sendMessage(MM.deserialize("<#ff4b2b><bold>[!]</bold> <#a8a8a8>Brak środków! Potrzebujesz <#ff4b2b>" + PAGE_COST + "$"));
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    }
                    case MAX_PAGES_REACHED -> {
                        player.sendMessage(MM.deserialize("<#ffaa00><bold>[!]</bold> <#a8a8a8>Osiągnięto już maksymalny limit stron (18)."));
                    }
                    case DATABASE_ERROR -> {
                        player.sendMessage(MM.deserialize("<#ff4b2b><bold>[!]</bold> <#a8a8a8>Błąd krytyczny podczas zapisu do bazy danych!"));
                    }
                }
            });
        }

        final ItemStack cancel = new StackBuilder(new ItemStack(Material.RED_DYE)).name("<#ff4b2b><bold>COFNIJ ZMIANY</bold>").build();
        window.setSlot(50, cancel, e -> {
            e.setCancelled(true);
            service.rollback(player.getUniqueId());
            player.closeInventory();
            player.sendMessage(MM.deserialize("<#ff4b2b><bold>[!]</bold> <#a8a8a8>Anulowano zmiany w plecaku."));
        });
    }
}