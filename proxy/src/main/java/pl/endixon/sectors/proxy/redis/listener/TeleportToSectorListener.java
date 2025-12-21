package pl.endixon.sectors.proxy.redis.listener;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import java.util.Optional;
import net.kyori.adventure.text.Component;
import pl.endixon.sectors.common.packet.PacketListener;
import pl.endixon.sectors.common.packet.object.PacketRequestTeleportSector;
import pl.endixon.sectors.proxy.VelocitySectorPlugin;
import pl.endixon.sectors.proxy.util.Logger;

public class TeleportToSectorListener implements PacketListener<PacketRequestTeleportSector> {

    @Override
    public void handle(PacketRequestTeleportSector packet) {
        String playerName = packet.getPlayerName();
        String sectorName = packet.getSector();

        Logger.info("Otrzymano request teleportu gracza " + playerName + " do sektora " + sectorName);

        Optional<Player> playerOptional = VelocitySectorPlugin.getInstance().getServer().getPlayer(playerName);
        if (playerOptional.isEmpty()) {
            Logger.info("Nie udało się teleportować gracza " + playerName + " (gracz nie istnieje)");
            return;
        }

        Optional<RegisteredServer> serverOptional = VelocitySectorPlugin.getInstance().getServer().getServer(sectorName);
        if (serverOptional.isEmpty()) {
            playerOptional.ifPresent(player -> player.disconnect(Component.text("Brak dostępnych serwerów.")));
            Logger.info("Nie udało się teleportować gracza " + playerName + " (serwer " + sectorName + " nie istnieje)");
            return;
        }

        Player player = playerOptional.get();
        RegisteredServer server = serverOptional.get();

        player.createConnectionRequest(server).fireAndForget();
        Logger.info("Gracz " + playerName + " teleportowany natychmiast do " + sectorName);
    }
}
