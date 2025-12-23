package pl.endixon.sectors.paper.user.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import lombok.AllArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import pl.endixon.sectors.common.packet.PacketChannel;
import pl.endixon.sectors.common.packet.object.PacketSectorChatBroadcast;
import pl.endixon.sectors.common.sector.SectorType;
import pl.endixon.sectors.paper.PaperSector;
import pl.endixon.sectors.paper.manager.SectorManager;
import pl.endixon.sectors.paper.sector.Sector;

@AllArgsConstructor
public class PlayerChatListener implements Listener {

    private final PaperSector paperSector;

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        SectorManager sectorManager = this.paperSector.getSectorManager();
        Sector currentSector = sectorManager.getCurrentSector();
        Player player = event.getPlayer();

        if (currentSector != null && currentSector.getType() == SectorType.QUEUE) {
            event.setCancelled(true);
            return;
        }

        event.setCancelled(true);

        Component messageComponent = event.message();
        String plainMessage = PlainTextComponentSerializer.plainText().serialize(messageComponent);

        PacketSectorChatBroadcast packet = new PacketSectorChatBroadcast(player.getName(), plainMessage);
        paperSector.getRedisManager().publish(PacketChannel.PACKET_SECTOR_CHAT_BROADCAST, packet);
    }
}