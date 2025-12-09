package pl.endixon.sectors.paper.inventory;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.endixon.sectors.common.sector.SectorType;
import pl.endixon.sectors.paper.inventory.api.WindowUI;
import pl.endixon.sectors.paper.inventory.api.builder.StackBuilder;
import pl.endixon.sectors.paper.sector.Sector;
import pl.endixon.sectors.paper.sector.SectorManager;
import pl.endixon.sectors.paper.util.HeadFactory;
import pl.endixon.sectors.paper.util.TpsUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class SectorShowWindow {

    private final Player player;
    private final WindowUI window;

    public SectorShowWindow(Player player, SectorManager manager) {
        this.player = player;
        this.window = new WindowUI("&7Lista Sektorów", 6);

        List<Sector> sectors = manager.getSectors().stream()
                .sorted(Comparator
                        .comparingInt((Sector s) -> getOrder(s.getType()))
                        .thenComparing(Sector::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();

        int slot = 0;
        for (Sector s : sectors) {
            if (slot >= 54) break;

            ItemStack head = HeadFactory.pickOnlineOfflineHead(s.isOnline());
            ItemStack item = new StackBuilder(head)
                    .name("&6" + s.getName())
                    .lores(buildLore(s))
                    .build();

            window.setSlot(slot++, item, null);
        }
    }

    private int getOrder(SectorType type) {
        return switch (type) {
            case SPAWN -> 0;
            case SECTOR -> 1;
            case NETHER -> 2;
            case END -> 3;
            case QUEUE -> 4;
        };
    }

    private List<String> buildLore(Sector s) {
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(String.format("&7Status: %s", s.isOnline() ? "&aOnline" : "&cOffline"));
        lore.add(String.format("&7TPS: &6%.1f", s.isOnline() ? TpsUtil.getTPS() : 0.0));
        lore.add(String.format("&7Online: &6%d", s.getPlayerCount()));
        lore.add(String.format("&7Ostatnia aktualizacja: &6%.1fs", s.getLastInfoPacket()));
        return lore;
    }

    public void open() {
        window.openFor(player);
    }
}
