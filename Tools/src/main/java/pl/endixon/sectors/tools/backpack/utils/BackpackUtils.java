package pl.endixon.sectors.tools.backpack.utils;

import lombok.experimental.UtilityClass;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import pl.endixon.sectors.tools.user.profile.player.PlayerBackpackProfile;
import pl.endixon.sectors.tools.utils.PlayerDataSerializerUtil;

@UtilityClass
public class BackpackUtils {

    private static final int STORAGE_SLOTS = 45;


    public static void updateBackpackFromInventory(Inventory inv, PlayerBackpackProfile backpack, int page) {
        final ItemStack[] content = new ItemStack[STORAGE_SLOTS];

        for (int i = 0; i < STORAGE_SLOTS; i++) {
            content[i] = inv.getItem(i);
        }

        final String base64 = PlayerDataSerializerUtil.serializeItemStacksToBase64(content);
        backpack.getPages().put(String.valueOf(page), base64);
    }
}