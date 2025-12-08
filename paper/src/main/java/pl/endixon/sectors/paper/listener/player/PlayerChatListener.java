package pl.endixon.sectors.paper.listener.player;

import lombok.AllArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import io.papermc.paper.event.player.AsyncChatEvent;
import pl.endixon.sectors.common.packet.object.PacketSectorChatBroadcast;
import pl.endixon.sectors.common.sector.SectorType;
import pl.endixon.sectors.paper.PaperSector;
import pl.endixon.sectors.paper.sector.Sector;
import pl.endixon.sectors.paper.sector.SectorManager;

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

        Component message = event.message();

        String serializedMessage = LegacyComponentSerializer.builder()
                .character('&')
                .hexColors()
                .build()
                .serialize(message);

        currentSector.sendPacketSectors(
                new PacketSectorChatBroadcast(player.getName(), serializedMessage)
        );
    }
}
