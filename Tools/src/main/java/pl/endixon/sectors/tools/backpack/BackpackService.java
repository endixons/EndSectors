package pl.endixon.sectors.tools.backpack;

import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
import pl.endixon.sectors.tools.backpack.repository.BackpackRepository;
import pl.endixon.sectors.tools.backpack.type.BackpackUpgradeResult;
import pl.endixon.sectors.tools.user.profile.cache.ProfileBackpackCache;
import pl.endixon.sectors.tools.user.profile.player.PlayerBackpackProfile;
import pl.endixon.sectors.tools.user.profile.player.PlayerProfile;

import java.util.UUID;

@RequiredArgsConstructor
public class BackpackService {

    private final BackpackRepository backpackRepository;

    private static final String PERM_PREFIX = "endsectors.backpack.pages.";
    private static final int ABSOLUTE_MAX = 18;

    public PlayerBackpackProfile getOrCreateBackpack(Player player) {
        PlayerBackpackProfile backpack = ProfileBackpackCache.get(player.getUniqueId());
        if (backpack == null) {
            return this.backpackRepository.find(player.getUniqueId()).orElseGet(() ->
                    this.backpackRepository.create(player.getUniqueId(), player.getName())
            );
        }
        return backpack;
    }


    public int getMaxPages(Player player, PlayerBackpackProfile backpack) {
        if (player.hasPermission("endsectors.backpack.admin")) return ABSOLUTE_MAX;

        int total = backpack.getUnlockedPages();
        int rankBonus = 0;

        for (PermissionAttachmentInfo pai : player.getEffectivePermissions()) {
            final String perm = pai.getPermission().toLowerCase();
            if (perm.startsWith(PERM_PREFIX)) {
                try {
                    int val = Integer.parseInt(perm.substring(PERM_PREFIX.length()));
                    if (val > rankBonus) rankBonus = val;
                } catch (NumberFormatException ignored) {}
            }
        }
        return Math.min(total + rankBonus, ABSOLUTE_MAX);
    }

    public BackpackUpgradeResult processPageUpgrade(PlayerProfile profile, PlayerBackpackProfile backpack, double cost, int currentMax) {
        if (currentMax >= ABSOLUTE_MAX) return BackpackUpgradeResult.MAX_PAGES_REACHED;
        if (profile.getBalance() < cost) return BackpackUpgradeResult.INSUFFICIENT_FUNDS;

        try {
            profile.setBalance(profile.getBalance() - cost);
            backpack.setUnlockedPages(backpack.getUnlockedPages() + 1);
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