package pl.endixon.sectors.paper.task;

import org.bukkit.scheduler.BukkitRunnable;
import pl.endixon.sectors.common.packet.PacketChannel;
import pl.endixon.sectors.paper.PaperSector;
import pl.endixon.sectors.paper.redis.packet.PacketPlayerInfoRequest;
import pl.endixon.sectors.paper.user.UserMongo;

public class SendInfoPlayerTask extends BukkitRunnable {

    private final PaperSector paperSector;
    private final UserMongo user;

    public SendInfoPlayerTask(PaperSector paperSector, UserMongo user) {
        this.paperSector = paperSector;
        this.user = user;
    }

    @Override
    public void run() {
        paperSector.getRedisManager().publish(
                PacketChannel.SECTORS,
                new PacketPlayerInfoRequest(user)
        );
    }
}
