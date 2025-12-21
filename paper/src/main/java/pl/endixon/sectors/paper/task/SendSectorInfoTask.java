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

package pl.endixon.sectors.paper.task;

import org.bukkit.Bukkit;
import pl.endixon.sectors.common.packet.PacketChannel;
import pl.endixon.sectors.paper.PaperSector;
import pl.endixon.sectors.paper.redis.packet.PacketSectorInfo;
import pl.endixon.sectors.paper.util.TpsUtil;

public class SendSectorInfoTask implements Runnable {

    private final PaperSector paperSector;

    public SendSectorInfoTask(PaperSector paperSector) {
        this.paperSector = paperSector;
    }

    @Override
    public void run() {
        int online = Bukkit.getOnlinePlayers().size();
        String sector = PaperSector.getInstance().getSectorManager().getCurrentSectorName();
        int max = Bukkit.getMaxPlayers();
        float tps = (float) TpsUtil.getTPS();
        PacketSectorInfo info = new PacketSectorInfo(sector, tps, online, max);
        paperSector.getRedisManager().publish(PacketChannel.PACKET_SECTOR_INFO, info);
        paperSector.getRedisManager().publish(PacketChannel.PACKET_SECTOR_INFO_QUEUE, info);
    }
}
