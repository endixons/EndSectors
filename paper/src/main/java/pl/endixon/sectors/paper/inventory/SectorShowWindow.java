package pl.endixon.sectors.paper.inventory;

import java.util.Comparator;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.endixon.sectors.common.sector.SectorType;
import pl.endixon.sectors.paper.inventory.api.WindowUI;
import pl.endixon.sectors.paper.inventory.api.builder.StackBuilder;
import pl.endixon.sectors.paper.manager.SectorManager;
import pl.endixon.sectors.paper.sector.Sector;
import pl.endixon.sectors.paper.util.HeadFactory;
import pl.endixon.sectors.paper.util.MessagesUtil;

public class SectorShowWindow {

    private final Player player;
    private final WindowUI window;

    public SectorShowWindow(Player player, SectorManager manager) {
        this.player = player;
        this.window = new WindowUI(MessagesUtil.SHOW_GUI_TITLE.getText(), 6);

        List<Sector> sectors = manager.getSectors().stream()
                .sorted(Comparator
                        .comparingInt((Sector s) -> this.getOrder(s.getType()))
                        .thenComparing(Sector::getName, String.CASE_INSENSITIVE_ORDER))
                .limit(54)
                .toList();

        for (int slot = 0; slot < sectors.size(); slot++) {
            Sector s = sectors.get(slot);
            ItemStack head = HeadFactory.pickOnlineOfflineHead(s.isOnline());

            String itemName = MessagesUtil.SHOW_ITEM_NAME.getText("{SECTOR}", s.getName());

            ItemStack item = new StackBuilder(head)
                    .name(itemName)
                    .lores(this.buildLore(s))
                    .build();
            window.setSlot(slot, item, null);
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
        final int percentFull = s.getMaxPlayers() > 0
                ? (int) ((double) s.getPlayerCount() / s.getMaxPlayers() * 100)
                : 0;

        final String status = s.isOnline()
                ? MessagesUtil.SHOW_STATUS_ONLINE.getRaw()
                : MessagesUtil.SHOW_STATUS_OFFLINE.getRaw();

        return MessagesUtil.SHOW_LORE_FORMAT.asLore(
                "{STATUS}", status,
                "{TPS}", s.getTPSColored(),
                "{COUNT}", String.valueOf(s.getPlayerCount()),
                "{MAX}", String.valueOf(s.getMaxPlayers()),
                "{PERCENT}", String.valueOf(percentFull),
                "{UPDATE}", String.format("%.1f", s.getLastInfoPacket())
        );
    }

    public void open() {
        window.openFor(player);
    }
}