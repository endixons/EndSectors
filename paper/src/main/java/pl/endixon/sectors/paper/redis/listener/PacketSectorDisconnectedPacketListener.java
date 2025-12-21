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

package pl.endixon.sectors.paper.redis.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import pl.endixon.sectors.common.packet.PacketListener;
import pl.endixon.sectors.common.packet.object.PacketSectorDisconnected;
import pl.endixon.sectors.common.util.ChatUtil;
import pl.endixon.sectors.paper.PaperSector;
import pl.endixon.sectors.paper.sector.Sector;
import pl.endixon.sectors.paper.util.LoggerUtil;

public class PacketSectorDisconnectedPacketListener implements PacketListener<PacketSectorDisconnected> {

    @Override
    public void handle(PacketSectorDisconnected packet) {
        String sectorName = packet.getSector();

        Sector disconnectedSector = PaperSector.getInstance().getSectorManager().getSector(sectorName);
        if (disconnectedSector == null)
            return;

        disconnectedSector.setOnline(false);

        String currentSectorName = PaperSector.getInstance().getSectorManager().getCurrentSectorName();

        if (!sectorName.equalsIgnoreCase(currentSectorName)) {
            String message = String.format("&cSektor &e%s &czostał zamknięty i jest niedostępny!", sectorName);

            LoggerUtil.info(message);

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.hasPermission("endsectors.messages"))
                    continue;
                player.sendMessage(ChatUtil.fixColors(message));
            }
        }
    }
}
