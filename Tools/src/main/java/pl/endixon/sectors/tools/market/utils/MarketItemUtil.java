package pl.endixon.sectors.tools.market.utils;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import pl.endixon.sectors.tools.inventory.api.builder.StackBuilder;
import pl.endixon.sectors.tools.utils.PlayerDataSerializerUtil;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public final class MarketItemUtil {

    private static final ItemFlag[] HIDE_FLAGS = {
            ItemFlag.HIDE_ATTRIBUTES,
            ItemFlag.HIDE_ENCHANTS,
            ItemFlag.HIDE_ADDITIONAL_TOOLTIP,
            ItemFlag.HIDE_UNBREAKABLE,
            ItemFlag.HIDE_DESTROYS,
            ItemFlag.HIDE_PLACED_ON,
            ItemFlag.HIDE_DYE
    };

    private static final long EXPIRATION_MILLIS = TimeUnit.HOURS.toMillis(24);

    public static String resolveItemName(@NotNull ItemStack item) {
        final ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.hasDisplayName() && meta.displayName() != null) {
            return LegacyComponentSerializer.legacySection().serialize(meta.displayName());
        }
        return formatMaterialName(item.getType());
    }

    public static String formatMaterialName(@NotNull Material material) {
        StringBuilder name = new StringBuilder();
        for (String part : material.name().split("_")) {
            name.append(part.charAt(0)).append(part.substring(1).toLowerCase()).append(" ");
        }
        return name.toString().trim();
    }


    public static boolean hasSpace(Player player, ItemStack itemToCheck) {
        int amountNeeded = itemToCheck.getAmount();
        for (ItemStack storageItem : player.getInventory().getStorageContents()) {

            if (storageItem == null || storageItem.getType() == Material.AIR) {
                return true;
            }

            if (storageItem.isSimilar(itemToCheck)) {
                int spaceInStack = storageItem.getMaxStackSize() - storageItem.getAmount();
                if (spaceInStack > 0) {
                    amountNeeded -= spaceInStack;
                }
            }

            if (amountNeeded <= 0) {
                return true;
            }
        }
        return false;
    }

    public static Map<Integer, ItemStack> giveItemToPlayer(@NotNull Player player, @NotNull String itemData) {
        ItemStack[] items = PlayerDataSerializerUtil.deserializeItemStacksFromBase64(itemData);

        if (items.length > 0) {
            return player.getInventory().addItem(items[0]);
        }
        return Map.of();
    }


    public static String determineCategory(@NotNull Material m) {
        final String name = m.name();
        if (name.contains("SWORD") || name.contains("BOW") || name.contains("AXE") || name.contains("TRIDENT"))
            return "WEAPONS";
        if (name.contains("PICKAXE") || name.contains("SHOVEL") || name.contains("HOE") || name.contains("ROD"))
            return "TOOLS";
        if (name.contains("HELMET") || name.contains("CHESTPLATE") || name.contains("LEGGINGS") || name.contains("BOOTS") || name.contains("ELYTRA"))
            return "ARMOR";
        if (m.isBlock()) return "BLOCKS";
        if (name.contains("POTION") || name.contains("APPLE") || name.contains("STEAK") || name.contains("COOKED"))
            return "FOOD";
        return "OTHER";
    }

    public static String formatElapsedTime(long timestamp) {
        long diff = System.currentTimeMillis() - timestamp;
        if (diff < 0) return "przed chwilą";

        long seconds = diff / 1000;
        if (seconds < 60) return seconds + " s temu";
        long minutes = seconds / 60;
        if (minutes < 60) return minutes + " min temu";
        long hours = minutes / 60;
        if (hours < 24) return hours + " h temu";
        long days = hours / 24;
        return days + " dni temu";
    }


    public static String formatTimeLeft(long createdAt) {
        long endTimestamp = createdAt + EXPIRATION_MILLIS;
        long diff = endTimestamp - System.currentTimeMillis();

        if (diff <= 0) return "Wygasa...";

        long totalSeconds = diff / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        if (hours > 0) {
            return hours + "h " + minutes + "m";
        }
        if (minutes > 0) {
            return minutes + "m " + seconds + "s";
        }
        return seconds + "s";
    }

    public static void applyFlags(StackBuilder builder) {
        for (ItemFlag flag : HIDE_FLAGS) {
            builder.addFlag(flag);
        }
    }


}