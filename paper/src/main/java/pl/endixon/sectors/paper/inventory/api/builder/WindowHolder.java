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

import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Setter
@Getter
public class WindowHolder implements InventoryHolder {

    private final Map<Integer, Consumer<InventoryClickEvent>> slotActions = new ConcurrentHashMap<>();

    private Inventory inventory;

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void assignAction(int slot, Consumer<InventoryClickEvent> action) {
        slotActions.put(slot, action != null ? action : event -> event.setCancelled(true));
    }

    public void processClick(InventoryClickEvent event) {
        slotActions.getOrDefault(event.getRawSlot(), e -> e.setCancelled(true)).accept(event);
    }
}
