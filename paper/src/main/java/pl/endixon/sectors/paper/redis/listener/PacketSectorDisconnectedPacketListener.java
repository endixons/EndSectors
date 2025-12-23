package pl.endixon.sectors.paper.redis.listener;

import org.bukkit.Bukkit;
import pl.endixon.sectors.common.packet.PacketListener;
import pl.endixon.sectors.common.packet.object.PacketSectorDisconnected;
import pl.endixon.sectors.paper.PaperSector;
import pl.endixon.sectors.paper.sector.Sector;
import pl.endixon.sectors.paper.util.ChatAdventureUtil;
import pl.endixon.sectors.paper.util.MessagesUtil;
import pl.endixon.sectors.paper.util.LoggerUtil;

public class PacketSectorDisconnectedPacketListener implements PacketListener<PacketSectorDisconnected> {


    @Override
    public void handle(PacketSectorDisconnected packet) {
        final String sectorName = packet.getSector();
        final Sector disconnectedSector = PaperSector.getInstance().getSectorManager().getSector(sectorName);

        if (disconnectedSector == null) {
            return;
        }

        disconnectedSector.setOnline(false);

        final String currentSectorName = PaperSector.getInstance().getSectorManager().getCurrentSectorName();

        if (!sectorName.equalsIgnoreCase(currentSectorName)) {
            LoggerUtil.info("CRITICAL: Sector " + sectorName + " has lost connection to the cluster.");
            Bukkit.broadcast(MessagesUtil.SECTOR_STOPPED_NOTIFICATION.get("{SECTOR}", sectorName), "endsectors.messages");
        }
    }
    }