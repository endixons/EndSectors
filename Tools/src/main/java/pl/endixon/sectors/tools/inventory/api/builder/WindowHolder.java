package pl.endixon.sectors.tools.inventory.api.builder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

@Setter
@Getter
public class WindowHolder implements InventoryHolder {

    private final Map<Integer, Consumer<InventoryClickEvent>> slotActions = new ConcurrentHashMap<>();
    private Inventory inventory;
    private boolean interactionAllowed = false;

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void assignAction(int slot, Consumer<InventoryClickEvent> action) {
        if (action == null) {
            slotActions.put(slot, event -> event.setCancelled(true));
            return;
        }
        slotActions.put(slot, event -> {
            event.setCancelled(true);
            action.accept(event);
        });
    }

    public void processClick(InventoryClickEvent event) {
        int slot = event.getRawSlot();
        if (slot < 0) return;

        if (slot < inventory.getSize()) {
            Consumer<InventoryClickEvent> action = slotActions.get(slot);
            if (action != null) {
                action.accept(event);
                return;
            }
            if (!interactionAllowed) {
                event.setCancelled(true);
            }
            return;
        }
        if (!interactionAllowed) {
            event.setCancelled(true);
        }
    }
}