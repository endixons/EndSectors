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
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import pl.endixon.sectors.common.Common;
import pl.endixon.sectors.common.packet.PacketChannel;
import pl.endixon.sectors.common.sector.SectorType;
import pl.endixon.sectors.paper.PaperSector;
import pl.endixon.sectors.paper.nats.packet.PacketPlayerInfoRequest;
import pl.endixon.sectors.paper.sector.Sector;
import pl.endixon.sectors.paper.user.profile.UserProfileRepository;

public class SendInfoPlayerTask extends BukkitRunnable {

    private final PaperSector paperSector;

    public SendInfoPlayerTask(PaperSector paperSector) {
        this.paperSector = paperSector;
    }

    @Override
    public void run() {
        Sector currentSector = paperSector.getSectorManager().getCurrentSector();
        if (currentSector == null || currentSector.getType() == SectorType.QUEUE) {
            cancel();
            return;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            UserProfileRepository.getUserAsync(player.getName()).thenAccept(optionalUser -> {
                optionalUser.ifPresent(user -> {
                    Common.getInstance().getNatsManager().publish(PacketChannel.PACKET_PLAYER_INFO_REQUEST.getSubject(), new PacketPlayerInfoRequest(user));
                });
            });
        }
    }
}
