package pl.endixon.sectors.paper.redis.listener;

import pl.endixon.sectors.common.packet.PacketChannel;
import pl.endixon.sectors.common.packet.PacketListener;
import pl.endixon.sectors.common.packet.object.PacketUserCheck;
import pl.endixon.sectors.paper.PaperSector;
import pl.endixon.sectors.paper.user.UserManager;
import pl.endixon.sectors.paper.user.UserRedis;

public class PacketUserCheckListener implements PacketListener<PacketUserCheck> {

    @Override
    public void handle(PacketUserCheck packet) {
        String username = packet.getUsername().toLowerCase();

        UserManager.getUserAsync(username).thenAccept(optionalUser -> {
            boolean exists = optionalUser.isPresent();
            String sector = optionalUser.map(UserRedis::getSectorName).orElse(null);
            PacketUserCheck response = new PacketUserCheck(username, exists, sector);
            PaperSector.getInstance().getRedisManager()
                    .publish(PacketChannel.USER_CHECK_RESPONSE, response);
        });
    }
}
