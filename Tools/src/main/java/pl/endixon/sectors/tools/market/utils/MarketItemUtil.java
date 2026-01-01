package pl.endixon.sectors.tools.market.utils;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public final class MarketItemUtil {

    private MarketItemUtil() {}


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


    public static String determineCategory(@NotNull Material m) {
        final String name = m.name();
        if (name.contains("SWORD") || name.contains("BOW") || name.contains("AXE") || name.contains("TRIDENT")) return "WEAPONS";
        if (name.contains("PICKAXE") || name.contains("SHOVEL") || name.contains("HOE") || name.contains("ROD")) return "TOOLS";
        if (name.contains("HELMET") || name.contains("CHESTPLATE") || name.contains("LEGGINGS") || name.contains("BOOTS") || name.contains("ELYTRA")) return "ARMOR";
        if (m.isBlock()) return "BLOCKS";
        if (name.contains("POTION") || name.contains("APPLE") || name.contains("STEAK") || name.contains("COOKED")) return "FOOD";
        return "OTHER";
    }


    public static String formatElapsedTime(long timestamp) {
        long diff = System.currentTimeMillis() - timestamp;

        if (diff < 0) {
            return "przed chwilą";
        }

        long seconds = diff / 1000;

        if (seconds < 60) {
            return seconds + " s temu";
        }

        long minutes = seconds / 60;

        if (minutes < 60) {
            return minutes + " min temu";
        }

        long hours = minutes / 60;

        if (hours < 24) {
            return hours + " h temu";
        }
        long days = hours / 24;
        return days + " dni temu";
    }

}