package pl.endixon.sectors.paper.redis.listener;

import net.kyori.adventure.text.Component;
import pl.endixon.sectors.common.packet.PacketListener;
import pl.endixon.sectors.common.packet.object.PacketSectorChatBroadcast;
import pl.endixon.sectors.paper.PaperSector;
import pl.endixon.sectors.paper.util.ChatAdventureUtil;

public class PacketSectorChatBroadcastPacketListener implements PacketListener<PacketSectorChatBroadcast> {


    private static final ChatAdventureUtil CHAT_HELPER = new ChatAdventureUtil();

    @Override
    public void handle(PacketSectorChatBroadcast packet) {
        String formattedMessage = "<gray>" + packet.getSenderName() + ": <white>" + packet.getMessage();
        Component componentMessage = CHAT_HELPER.toComponent(formattedMessage);
        PaperSector.getInstance().getServer().sendMessage(componentMessage);
    }
}