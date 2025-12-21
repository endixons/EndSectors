/*
 *
 * EndSectors – Non-Commercial License
 * (c) 2025 Endixon
 *
 * Permission is granted to use, copy, and
 * modify this software **only** for personal
 * or educational purposes.
 *
 * Commercial use, redistribution, claiming
 * this work as your own, or copying code
 * without explicit permission is strictly
 * prohibited.
 *
 * Visit https://github.com/Endixon/EndSectors
 * for more info.
 *
 */

package pl.endixon.sectors.paper.inventory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.endixon.sectors.common.sector.SectorType;
import pl.endixon.sectors.common.util.ChatUtil;
import pl.endixon.sectors.paper.inventory.api.WindowUI;
import pl.endixon.sectors.paper.inventory.api.builder.StackBuilder;
import pl.endixon.sectors.paper.manager.SectorManager;
import pl.endixon.sectors.paper.sector.Sector;
import pl.endixon.sectors.paper.util.HeadFactory;
import pl.endixon.sectors.paper.util.TpsUtil;

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
            if (slot >= 54)
                break;

            ItemStack head = HeadFactory.pickOnlineOfflineHead(s.isOnline());
            ItemStack item = new StackBuilder(head).name("&6" + s.getName()).lores(buildLore(s)).build();

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
        lore.add(ChatUtil.fixHexColors("&#9ca3afStatus: %s".formatted(s.isOnline() ? "&#4ade80Online" : "&#ef4444Offline")));
        lore.add(ChatUtil.fixHexColors("&#9ca3afTPS: &#f59e0b%.1f".formatted(s.isOnline() ? TpsUtil.getTPS() : 0.0)));
        lore.add(ChatUtil.fixHexColors("&#9ca3afOnline: &#7dd3fc%d/%d".formatted(s.getPlayerCount(), s.getMaxPlayers())));
        int percentFull = s.getMaxPlayers() > 0 ? (int) ((double) s.getPlayerCount() / s.getMaxPlayers() * 100) : 0;
        lore.add(ChatUtil.fixHexColors("&#9ca3afZapełnienie: &#fbbf24%d%%".formatted(percentFull)));
        lore.add(ChatUtil.fixHexColors("&#9ca3afOstatnia aktualizacja: &#a78bfa%.1fs".formatted(s.getLastInfoPacket())));
        return lore;
    }

    public void open() {
        window.openFor(player);
    }
}
