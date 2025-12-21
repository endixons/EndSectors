/*
 *
 *  EndSectors  Non-Commercial License
 *  (c) 2025 Endixon
 *
 *  Permission is granted to use, copy, and
 *  modify this software **only** for personal
 *  or educational purposes.
 *
 *   Commercial use, redistribution, claiming
 *  this work as your own, or copying code
 *  without explicit permission is strictly
 *  prohibited.
 *
 *  Visit https://github.com/Endixon/EndSectors
 *  for more info.
 *
 */

package pl.endixon.sectors.tools.user.listeners;

import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import pl.endixon.sectors.tools.inventory.api.builder.WindowHolder;

@RequiredArgsConstructor
public class InventoryInternactListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Inventory clickedInventory = event.getClickedInventory();
        Inventory topInventory = event.getInventory();
        InventoryHolder holder = topInventory.getHolder();

        if (!isWindowInventory(clickedInventory) && !isWindowInventory(topInventory)) {
            return;
        }

        if (!(holder instanceof WindowHolder windowHolder)) {
            return;
        }

        event.setCancelled(true);
        windowHolder.processClick(event);
    }

    @EventHandler
    public void onInteract(InventoryInteractEvent event) {

        if (!isWindowInventory(event.getInventory())) {
            return;
        }
        event.setCancelled(true);
    }

    private boolean isWindowInventory(Inventory inventory) {

        if (inventory == null || inventory.getType() != InventoryType.CHEST) {
            return false;
        }

        InventoryHolder holder = inventory.getHolder();
        return holder instanceof WindowHolder && holder.getClass() == WindowHolder.class;
    }
}
