package pl.endixon.sectors.paper.redis.listener;

import org.bson.Document;
import org.bukkit.Bukkit;
import pl.endixon.sectors.common.packet.PacketChannel;
import pl.endixon.sectors.common.packet.PacketListener;
import pl.endixon.sectors.common.packet.object.PacketUserCheck;
import pl.endixon.sectors.paper.PaperSector;
import pl.endixon.sectors.paper.user.UserManager;
import pl.endixon.sectors.paper.user.UserMongo;

public class PacketUserCheckListener implements PacketListener<PacketUserCheck> {


    @Override
    public void handle(PacketUserCheck packet) {
        String username = packet.getUsername();

        UserMongo cached = UserManager.getUsers().get(username.toLowerCase());
        if (cached != null) {
            PacketUserCheck response = new PacketUserCheck(username, true, cached.getSectorName());
            PaperSector.getInstance().getRedisManager().publish(PacketChannel.USER_CHECK_RESPONSE, response);
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(PaperSector.getInstance(), () -> {
            Document doc = PaperSector.getInstance().getMongoManager()
                    .getUsersCollection()
                    .find(new Document("Name", username))
                    .first();

            boolean exists = doc != null;
            String sector = exists ? doc.getString("sectorName") : null;
            PacketUserCheck response = new PacketUserCheck(username, exists, sector);
            PaperSector.getInstance().getRedisManager().publish(PacketChannel.USER_CHECK_RESPONSE, response);
        });
    }
}