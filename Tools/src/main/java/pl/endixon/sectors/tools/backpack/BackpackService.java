package pl.endixon.sectors.tools.backpack;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachmentInfo;
import pl.endixon.sectors.tools.backpack.repository.BackpackRepository;
import pl.endixon.sectors.tools.backpack.type.BackpackUpgradeResult;
import pl.endixon.sectors.tools.user.profile.cache.ProfileBackpackCache;
import pl.endixon.sectors.tools.user.profile.player.PlayerBackpackProfile;
import pl.endixon.sectors.tools.user.profile.player.PlayerProfile;
import pl.endixon.sectors.tools.utils.PlayerDataSerializerUtil;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class BackpackService {

    private final BackpackRepository backpackRepository;
    private static final String PERM_PREFIX = "endsectors.backpack.pages.";
    private static final int ABSOLUTE_MAX = 18;
    private static final long WEEK_MS = 1000L * 60 * 60 * 24 * 7;
    private static final MiniMessage MM = MiniMessage.miniMessage();


    public PlayerBackpackProfile getOrCreateBackpack(Player player) {
        PlayerBackpackProfile backpack = ProfileBackpackCache.get(player.getUniqueId());
        if (backpack == null) {
            return this.backpackRepository.find(player.getUniqueId()).orElseGet(() ->
                    this.backpackRepository.create(player.getUniqueId(), player.getName())
            );
        }
        return backpack;
    }

    public boolean isPageActive(PlayerBackpackProfile backpack, int page) {
        if (page <= 1) return true;
        return backpack.getPageExpirations().getOrDefault(String.valueOf(page), 0L) > System.currentTimeMillis();
    }

    public BackpackUpgradeResult processSubscription(PlayerProfile profile, PlayerBackpackProfile backpack, int page, double cost) {
        if (profile.getBalance() < cost) return BackpackUpgradeResult.INSUFFICIENT_FUNDS;

        profile.setBalance(profile.getBalance() - cost);
        long current = backpack.getPageExpirations().getOrDefault(String.valueOf(page), System.currentTimeMillis());
        backpack.getPageExpirations().put(String.valueOf(page), Math.max(current, System.currentTimeMillis()) + WEEK_MS);

        this.backpackRepository.save(backpack);
        return BackpackUpgradeResult.SUCCESS;
    }


    public BackpackUpgradeResult processBulkSubscription(Player player, PlayerProfile profile, PlayerBackpackProfile backpack, double unitCost) {
        // 1. Pobieramy REALNĄ liczbę odblokowanych stron (zamiast zawodnego size())
        final int maxPages = this.getMaxPages(player, backpack);

        // 2. Szukamy stron, które faktycznie wygasły
        int expiredCount = 0;
        java.util.List<Integer> pagesToRenew = new java.util.ArrayList<>();

        // Iterujemy od 2, bo strona 1 jest zawsze aktywna/darmowa
        for (int i = 2; i <= maxPages; i++) {
            if (!this.isPageActive(backpack, i)) {
                expiredCount++;
                pagesToRenew.add(i);
            }
        }

        // Jeśli wszystko jest opłacone, nie ma co robić (Enterprise Safety)
        if (expiredCount == 0 || pagesToRenew.isEmpty()) {
            return BackpackUpgradeResult.FAILURE;
        }

        // 3. Obliczamy totalny koszt transakcji
        final double totalCost = expiredCount * unitCost;

        // 4. Walidacja portfela
        if (profile.getBalance() < totalCost) {
            return BackpackUpgradeResult.FAILURE;
        }

        // 5. TRANSAKCJA: Zabieramy środki
        profile.setBalance(profile.getBalance() - totalCost);

        // 6. AKTUALIZACJA: Przedłużamy ważność o 7 dni od teraz
        final long now = System.currentTimeMillis();
        final long sevenDaysInMs = 7L * 24 * 60 * 60 * 1000;

        for (int pageNum : pagesToRenew) {
            backpack.getPageExpirations().put(String.valueOf(pageNum), now + sevenDaysInMs);
        }

        // 7. ZAPIS: Synchronizacja z repozytorium
        this.backpackRepository.save(backpack);

        return BackpackUpgradeResult.SUCCESS;
    }


    public void handleDeathBreach(Player victim) {
        PlayerBackpackProfile backpack = this.getOrCreateBackpack(victim);

        if (backpack.getPages().isEmpty()) {
            return;
        }

        List<String> activePages = new ArrayList<>();
        for (Map.Entry<String, String> entry : backpack.getPages().entrySet()) {
            String base64 = entry.getValue();
            if (base64 != null && !base64.isEmpty()) {
                activePages.add(entry.getKey());
            }
        }

        if (activePages.isEmpty()) {
            return;
        }

        if (Math.random() > 0.05) {
            return;
        }

        Collections.shuffle(activePages);

        for (String key : activePages) {
            String base64Data = backpack.getPages().get(key);
            ItemStack[] items = PlayerDataSerializerUtil.deserializeItemStacksFromBase64(base64Data);

            for (int i = 0; i < items.length; i++) {
                ItemStack item = items[i];

                if (item == null || item.getType() == Material.AIR) {
                    continue;
                }

                ItemStack drop = item.clone();

                String itemName = (drop.hasItemMeta() && drop.getItemMeta().displayName() != null)
                        ? MM.serialize(drop.getItemMeta().displayName())
                        : drop.getType().name();

                items[i] = null;
                backpack.getPages().put(key, PlayerDataSerializerUtil.serializeItemStacksToBase64(items));

                this.backpackRepository.save(backpack);
                victim.getWorld().dropItemNaturally(victim.getLocation(), drop);
                this.sendBreachNotifications(victim, itemName);
                return;
            }
        }
    }


    private void sendBreachNotifications(Player victim, String itemName) {
        victim.showTitle(net.kyori.adventure.title.Title.title(
                MM.deserialize("<#ff4b2b><bold>BACKPACK BREACH!"),
                MM.deserialize("<#a8a8a8>Straciłeś: <white>" + itemName)
        ));
        victim.playSound(victim.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 1f, 0.5f);

        Player killer = victim.getKiller();
        if (killer != null) {
            killer.showTitle(net.kyori.adventure.title.Title.title(
                    MM.deserialize("<#00ff87><bold>CRITICAL DROP!"),
                    MM.deserialize("<#a8a8a8>Wypadł item z plecaka ofiary!")
            ));
            killer.playSound(killer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 0.8f);
            killer.sendMessage(MM.deserialize("<#00ff87><bold>[!]</bold> <#a8a8a8>Z plecaka <white>" + victim.getName() +
                    " <#a8a8a8>wypadło: <white>" + itemName));
        }
    }


    public int getMaxPages(Player player, PlayerBackpackProfile backpack) {
        if (player.hasPermission("endsectors.backpack.admin")) return ABSOLUTE_MAX;
        int total = backpack.getUnlockedPages();
        int rankBonus = 0;
        for (PermissionAttachmentInfo pai : player.getEffectivePermissions()) {
            if (pai.getPermission().toLowerCase().startsWith(PERM_PREFIX)) {
                try {
                    rankBonus = Math.max(rankBonus, Integer.parseInt(pai.getPermission().substring(PERM_PREFIX.length())));
                } catch (Exception ignored) {}
            }
        }
        return Math.min(total + rankBonus, ABSOLUTE_MAX);
    }

    public BackpackUpgradeResult processPageUpgrade(PlayerProfile profile, PlayerBackpackProfile backpack, double cost, int currentMax) {
        if (currentMax >= ABSOLUTE_MAX) return BackpackUpgradeResult.MAX_PAGES_REACHED;
        if (profile.getBalance() < cost) return BackpackUpgradeResult.INSUFFICIENT_FUNDS;

        try {
            profile.setBalance(profile.getBalance() - cost);
            int nextPage = backpack.getUnlockedPages() + 1;
            backpack.setUnlockedPages(nextPage);
            backpack.getPageExpirations().put(String.valueOf(nextPage), System.currentTimeMillis() + WEEK_MS);
            this.backpackRepository.save(backpack);
            return BackpackUpgradeResult.SUCCESS;
        } catch (Exception e) {
            return BackpackUpgradeResult.DATABASE_ERROR;
        }
    }

    public void saveBackpack(PlayerBackpackProfile backpack) {
        this.backpackRepository.save(backpack);
    }
    public void rollback(UUID uuid) {
        this.backpackRepository.refreshCache(uuid);
    }
}