package pl.endixon.sectors.tools.inventory.api;

import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import pl.endixon.sectors.tools.inventory.api.builder.WindowHolder;
import pl.endixon.sectors.tools.utils.ChatAdventureUtil;

public class WindowUI {

    private static final ChatAdventureUtil CHAT_UTIL = new ChatAdventureUtil();
    private final WindowHolder holder;

    public WindowUI(String title, int rows) {
        this.holder = new WindowHolder();
        this.holder.setInventory(Bukkit.createInventory(holder, Math.min(rows * 9, 54), CHAT_UTIL.toComponent(title)));
    }


    public void setInteractionAllowed(boolean allowed) {
        this.holder.setInteractionAllowed(allowed);
    }

    public void setSlot(int slot, ItemStack item, Consumer<InventoryClickEvent> onClick) {
        holder.assignAction(slot, onClick);
        holder.getInventory().setItem(slot, item);
    }

    public void handleClick(InventoryClickEvent event) {
        holder.processClick(event);
    }

    public void openFor(HumanEntity player) {
        player.openInventory(holder.getInventory());
    }

    public Inventory getInventory() {
        return holder.getInventory();
    }
}