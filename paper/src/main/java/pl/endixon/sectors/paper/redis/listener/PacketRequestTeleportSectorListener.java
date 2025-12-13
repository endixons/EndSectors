package pl.endixon.sectors.paper.redis.listener;

import org.bson.Document;
import org.bukkit.Bukkit;
import pl.endixon.sectors.common.packet.PacketChannel;
import pl.endixon.sectors.common.packet.PacketListener;
import pl.endixon.sectors.common.packet.object.PacketRequestTeleportSector;
import pl.endixon.sectors.common.packet.object.PacketUserCheck;
import pl.endixon.sectors.paper.PaperSector;
import pl.endixon.sectors.paper.user.UserManager;
import pl.endixon.sectors.paper.user.UserMongo;

public class PacketRequestTeleportSectorListener implements PacketListener<PacketRequestTeleportSector> {


    @Override
    public void handle(PacketRequestTeleportSector packet) {

    }
}