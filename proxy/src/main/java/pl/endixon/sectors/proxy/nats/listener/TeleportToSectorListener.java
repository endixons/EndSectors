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

package pl.endixon.sectors.proxy.nats.listener;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import java.util.Optional;
import net.kyori.adventure.text.Component;
import pl.endixon.sectors.common.packet.PacketListener;
import pl.endixon.sectors.common.packet.object.PacketRequestTeleportSector;
import pl.endixon.sectors.proxy.VelocitySectorPlugin;
import pl.endixon.sectors.proxy.util.LoggerUtil;

public class TeleportToSectorListener implements PacketListener<PacketRequestTeleportSector> {

    @Override
    public void handle(PacketRequestTeleportSector packet) {
        String playerName = packet.getPlayerName();
        String sectorName = packet.getSector();

        LoggerUtil.info("Received teleport request for player '" + playerName + "' to sector '" + sectorName + "'");


        Optional<Player> playerOptional = VelocitySectorPlugin.getInstance().getServer().getPlayer(playerName);
        if (playerOptional.isEmpty()) {
            LoggerUtil.error("Teleport failed for player '" + playerName + "': player not found");
            return;
        }

        Optional<RegisteredServer> serverOptional = VelocitySectorPlugin.getInstance().getServer().getServer(sectorName);
        if (serverOptional.isEmpty()) {
            playerOptional.ifPresent(player -> player.disconnect(Component.text("Brak dostępnych serwerów.")));
            LoggerUtil.error("Teleport failed for player '" + playerName + "': target sector '" + sectorName + "' does not exist");
            return;
        }

        Player player = playerOptional.get();
        RegisteredServer server = serverOptional.get();

        player.createConnectionRequest(server).fireAndForget();
        LoggerUtil.info("Player '" + playerName + "' has been successfully teleported to sector '" + sectorName + "'");
    }
}
