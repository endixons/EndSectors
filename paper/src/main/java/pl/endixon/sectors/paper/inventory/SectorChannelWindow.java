package pl.endixon.sectors.paper.inventory;

import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.endixon.sectors.common.sector.SectorType;
import pl.endixon.sectors.paper.inventory.api.WindowUI;
import pl.endixon.sectors.paper.inventory.api.builder.StackBuilder;
import pl.endixon.sectors.paper.manager.SectorManager;
import pl.endixon.sectors.paper.sector.Sector;
import pl.endixon.sectors.paper.sector.SectorTeleport;
import pl.endixon.sectors.paper.user.profile.UserProfile;
import pl.endixon.sectors.paper.util.MessagesUtil;
import pl.endixon.sectors.paper.util.HeadFactory;

public class SectorChannelWindow {

    private final Player player;
    private final WindowUI window;

    public SectorChannelWindow(Player player, SectorManager manager, UserProfile userData, SectorTeleport teleportService) {
        this.player = player;
        this.window = new WindowUI(MessagesUtil.CHANNEL_GUI_TITLE.getText(), 1);

        List<Sector> spawnSectors = manager.getSectors().stream()
                .filter(s -> s.getType() == SectorType.SPAWN)
                .toList();

        for (int slot = 0; slot < spawnSectors.size(); slot++) {
            Sector sector = spawnSectors.get(slot);
            ItemStack head = HeadFactory.pickOnlineOfflineHead(sector.isOnline());

            String itemName = MessagesUtil.CHANNEL_ITEM_NAME.getText("{SECTOR}", sector.getName());

            ItemStack item = new StackBuilder(head)
                    .name(itemName)
                    .lores(this.buildLore(sector, manager))
                    .build();

            window.setSlot(slot, item, event -> this.handleClick(sector, manager, userData, teleportService));
        }
    }

    private List<String> buildLore(Sector sector, SectorManager manager) {
        if (!sector.isOnline()) {
            return List.of("", MessagesUtil.CHANNEL_OFFLINE.getText());
        }

        String status = manager.getCurrentSectorName().equals(sector.getName())
                ? MessagesUtil.CHANNEL_CURRENT.getRaw()
                : MessagesUtil.CHANNEL_CLICK_TO_CONNECT.getRaw();

        return MessagesUtil.CHANNEL_LORE_FORMAT.asLore(
                "{ONLINE}", String.valueOf(sector.getPlayerCount()),
                "{TPS}", sector.getTPSColored(),
                "{UPDATE}", String.format("%.1f", sector.getLastInfoPacket()),
                "{STATUS}", status
        );
    }

    private void handleClick(Sector sector, SectorManager manager, UserProfile userData, SectorTeleport teleportService) {
        if (manager.getCurrentSectorName().equals(sector.getName())) {
            player.sendMessage(MessagesUtil.playerAlreadyConnectedMessage.get());
            return;
        }

        if (!sector.isOnline()) {
            player.sendMessage(MessagesUtil.sectorIsOfflineMessage.get());
            return;
        }

        if (userData == null) {
            player.kick(MessagesUtil.playerDataNotFoundMessage.get());
            return;
        }

        teleportService.teleportToSector(player, userData, sector, true, true);
    }

    public void open() {
        window.openFor(player);
    }
}