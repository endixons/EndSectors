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

package pl.endixon.sectors.proxy.redis.listener;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import pl.endixon.sectors.common.packet.PacketListener;
import pl.endixon.sectors.common.packet.object.PacketBroadcastTitle;
import pl.endixon.sectors.proxy.VelocitySectorPlugin;

public class PacketBroadcastTitlePacketListener implements PacketListener<PacketBroadcastTitle> {

    @Override
    public void handle(PacketBroadcastTitle packet) {
        ProxyServer server = VelocitySectorPlugin.getInstance().getServerInstance();
        if (server == null)
            return;

        Component title = Component.text(packet.getTitle());
        Component subtitle = Component.text(packet.getSubTitle());

        Title.Times times = Title.Times.of(java.time.Duration.ofMillis(packet.getFadeIn()), java.time.Duration.ofMillis(packet.getStay()), java.time.Duration.ofMillis(packet.getFadeOut()));

        Title advTitle = Title.title(title, subtitle, times);

        for (Player player : server.getAllPlayers()) {
            player.showTitle(advTitle);
        }
    }
}
