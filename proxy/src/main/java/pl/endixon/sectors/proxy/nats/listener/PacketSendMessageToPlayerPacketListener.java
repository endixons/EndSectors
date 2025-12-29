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

import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import pl.endixon.sectors.common.packet.PacketListener;
import pl.endixon.sectors.common.packet.object.PacketSendMessageToPlayer;
import pl.endixon.sectors.proxy.VelocitySectorPlugin;

public class PacketSendMessageToPlayerPacketListener implements PacketListener<PacketSendMessageToPlayer> {

    @Override
    public void handle(PacketSendMessageToPlayer packet) {
        final ProxyServer server = VelocitySectorPlugin.getInstance().getServer();
        if (server == null) {
            return;
        }

        final String playerName = packet.getPlayerName();
        if (playerName == null) {
            return;
        }
        server.getPlayer(playerName).ifPresent(player -> player.sendMessage(Component.text(packet.getMessage())));
    }
}