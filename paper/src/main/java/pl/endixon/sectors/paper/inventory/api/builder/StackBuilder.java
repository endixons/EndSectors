/*
 *
 *  EndSectors  Non-Commercial License
 *  (c) 2025 Endixon
 *
 *  Permission is granted to use, copy, and
 *  modify this software **only** for personal
 *  or educational purposes.
 *
 *  Commercial use, redistribution, claiming
 *  this work as your own, or copying code
 *  without explicit permission is strictly
 *  prohibited.
 *
 *  Visit https://github.com/Endixon/EndSectors
 *  for more info.
 *
 */

package pl.endixon.sectors.paper.inventory.api.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pl.endixon.sectors.common.util.ChatUtil;

public class StackBuilder {

    private final ItemStack stack;

    public StackBuilder(ItemStack stack) {
        this.stack = stack;
    }

    public StackBuilder type(Material material) {
        stack.setType(material);
        return this;
    }

    public StackBuilder amount(int amount) {
        stack.setAmount(amount);
        return this;
    }

    public StackBuilder durability(short dur) {
        stack.setDurability(dur);
        return this;
    }

    public StackBuilder addUnsafe(Enchantment enchant, int level) {
        stack.addUnsafeEnchantment(enchant, level);
        return this;
    }

    public StackBuilder removeEnchant(Enchantment enchant) {
        stack.removeEnchantment(enchant);
        return this;
    }

    public StackBuilder addEnchant(Enchantment enchant, int level) {
        ItemMeta meta = stack.getItemMeta();
        meta.addEnchant(enchant, level, true);
        stack.setItemMeta(meta);
        return this;
    }

    public StackBuilder addAllEnchants(Map<Enchantment, Integer> enchants) {
        stack.addEnchantments(enchants);
        return this;
    }

    public StackBuilder name(String name) {
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(ChatUtil.fixColors(name));
        stack.setItemMeta(meta);
        return this;
    }

    public String getName() {
        ItemMeta meta = stack.getItemMeta();
        return meta != null ? meta.getDisplayName() : "";
    }

    public StackBuilder lore(String line) {
        return lore(line, -1);
    }

    public StackBuilder lore(String line, int index) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null)
            return this;

        List<String> lore = meta.getLore() != null ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        String coloredLine = ChatUtil.fixColors(line);

        if (index >= 0 && index < lore.size())
            lore.set(index, coloredLine);
        else
            lore.add(coloredLine);

        meta.setLore(lore);
        stack.setItemMeta(meta);
        return this;
    }

    public StackBuilder lores(List<String> lines) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null)
            return this;

        List<String> colored = lines.stream().map(ChatUtil::fixColors).collect(Collectors.toList());
        meta.setLore(colored);
        stack.setItemMeta(meta);
        return this;
    }

    public StackBuilder glow(boolean enable) {
        if (!enable)
            return this;

        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.addEnchant(Enchantment.VANISHING_CURSE, 1, false);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            stack.setItemMeta(meta);
        }
        return this;
    }

    public StackBuilder addFlag(ItemFlag flag) {
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.addItemFlags(flag);
            stack.setItemMeta(meta);
        }
        return this;
    }

    public ItemStack build() {
        return stack;
    }
}
