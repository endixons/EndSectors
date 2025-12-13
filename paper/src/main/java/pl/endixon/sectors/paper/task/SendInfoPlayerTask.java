package pl.endixon.sectors.paper.task;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import pl.endixon.sectors.common.packet.PacketChannel;
import pl.endixon.sectors.common.sector.SectorType;
import pl.endixon.sectors.paper.PaperSector;
import pl.endixon.sectors.paper.redis.packet.PacketPlayerInfoRequest;
import pl.endixon.sectors.paper.sector.Sector;
import pl.endixon.sectors.paper.user.UserManager;
import pl.endixon.sectors.paper.user.UserMongo;

public class SendInfoPlayerTask extends BukkitRunnable {

    private final PaperSector paperSector;

    public SendInfoPlayerTask(PaperSector paperSector) {
        this.paperSector = paperSector;

    }

    @Override
    public void run() {
        Sector currentSector = PaperSector.getInstance().getSectorManager().getCurrentSector();
        if (currentSector == null || currentSector.getType() == SectorType.QUEUE) {
            cancel();
            return;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            UserMongo user = UserManager.getUsers().get(player.getName().toLowerCase());
            if (user == null) continue;

            paperSector.getRedisManager().publish(
                    PacketChannel.PACKET_PLAYER_INFO_REQUEST,
                    new PacketPlayerInfoRequest(user)
            );

        }
    }

}

