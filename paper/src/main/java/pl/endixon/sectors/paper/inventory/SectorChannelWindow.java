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

package pl.endixon.sectors.paper.inventory;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.endixon.sectors.common.sector.SectorType;
import pl.endixon.sectors.common.util.ChatUtil;
import pl.endixon.sectors.paper.inventory.api.WindowUI;
import pl.endixon.sectors.paper.sector.Sector;
import pl.endixon.sectors.paper.sector.SectorManager;
import pl.endixon.sectors.paper.sector.transfer.SectorTeleportService;
import pl.endixon.sectors.paper.user.UserRedis;
import pl.endixon.sectors.paper.util.Configuration;
import pl.endixon.sectors.paper.util.HeadFactory;
import pl.endixon.sectors.paper.inventory.api.builder.StackBuilder;

import java.util.ArrayList;
import java.util.List;

public class SectorChannelWindow {

    private final Player player;
    private final WindowUI window;

    public SectorChannelWindow(Player player,
                               SectorManager manager,
                               UserRedis userData,
                               SectorTeleportService teleportService) {
        this.player = player;
        this.window = new WindowUI("&7Lista Kanałów SPAWN", 1);

        List<Sector> spawnSectors = manager.getSectors().stream()
                .filter(s -> s.getType() == SectorType.SPAWN)
                .toList();

        for (int slot = 0; slot < spawnSectors.size(); slot++) {
            Sector sector = spawnSectors.get(slot);

            ItemStack head = HeadFactory.pickOnlineOfflineHead(sector.isOnline());
            ItemStack item = new StackBuilder(head)
                    .name("&7Sektor &a" + sector.getName())
                    .lores(buildLore(sector, manager))
                    .build();

            final int currentSlot = slot;
            window.setSlot(currentSlot, item, event ->
                    handleClick(sector, manager, userData, teleportService));
        }
    }

    private List<String> buildLore(Sector sector, SectorManager manager) {
        List<String> lore = new ArrayList<>();
        lore.add("");
        if (sector.isOnline()) {
            lore.add(ChatUtil.fixHexColors("&#9ca3afOnline: &#4ade80%d".formatted(sector.getPlayerCount())));
            lore.add(ChatUtil.fixHexColors("&#9ca3afTPS: %s".formatted(sector.getTPSColored())));
            lore.add(ChatUtil.fixHexColors("&#9ca3afOstatnia aktualizacja: &#4ade80%.1fs".formatted(sector.getLastInfoPacket())));
        } else {
            lore.add(ChatUtil.fixHexColors("&#ef4444Sektor jest offline"));
        }
        lore.add("");
        lore.add(ChatUtil.fixHexColors(manager.getCurrentSector().getName().equals(sector.getName())
                ? "&#facc15Znajdujesz się na tym kanale"
                : "&#facc15Kliknij, aby połączyć się z kanałem"));
        return lore;
    }


    private void handleClick(Sector sector, SectorManager manager, UserRedis userData, SectorTeleportService teleportService) {
        if (manager.getCurrentSector().getName().equals(sector.getName())) {
            player.sendMessage(ChatUtil.fixColors(Configuration.playerAlreadyConnectedMessage));
            return;
        }

        if (!sector.isOnline()) {
            player.sendMessage(ChatUtil.fixColors(Configuration.sectorIsOfflineMessage));
            return;
        }

        if (userData == null) {
            player.kick(Component.text(ChatUtil.fixColors(Configuration.playerDataNotFoundMessage)));
            return;
        }

        teleportService.teleportToSector(player, userData, sector, true);
    }

    public void open() {
        window.openFor(player);
    }
}
