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

package pl.endixon.sectors.proxy.user.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.ServerPing;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import pl.endixon.sectors.common.sector.SectorData;
import pl.endixon.sectors.proxy.VelocitySectorPlugin;
import pl.endixon.sectors.proxy.manager.SectorManager;
import pl.endixon.sectors.proxy.util.CpuUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProxyPingListener {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();


    private final ProxyServer proxyServer = VelocitySectorPlugin.getInstance().getServer();
    private final SectorManager sectorManager = VelocitySectorPlugin.getInstance().getSectorManager();

    @Subscribe
    public void onProxyPing(final ProxyPingEvent event) {
        final ServerPing ping = event.getPing();

        final int proxyOnline = this.proxyServer.getPlayerCount();
        int totalMax = 0;
        int activeSectors = 0;

        for (final SectorData sector : this.sectorManager.getSectorsData()) {
            if (sector.isOnline()) {
                totalMax += sector.getMaxPlayers();
                activeSectors++;
            }
        }

        final Component motd = MINI_MESSAGE.deserialize(
                "<bold><gradient:#2afcff:#00bfff>ENDSECTORS</gradient></bold> <gray>•</gray> " +
                        "<gradient:#ffe259:#ffa751>FRAMEWORK</gradient>\n" +
                        "<gray>Status systemu: </gray><gradient:#56ab2f:#a8e063>ONLINE</gradient>"
        );


        final List<ServerPing.SamplePlayer> hover = new ArrayList<>();

        final double cpuLoad = CpuUtil.getCpuLoad();
        final String formattedCpu = String.format("%.1f%%", cpuLoad);

        hover.add(sample("§b§lENDSECTORS FRAMEWORK"));
        hover.add(sample("§7Status systemu: §eTESTOWY"));
        hover.add(sample(""));
        hover.add(sample("§7Aktywne sektory: §a" + activeSectors));
        hover.add(sample("§7Gracze na proxy: §a" + proxyOnline));
        hover.add(sample(""));
        hover.add(sample("§e§l» KLIKNIJ ABY PRZETESTOWAĆ SYSTEM «"));

        String cpuColor;
        if (cpuLoad >= 70.0) {
            cpuColor = "§c";
        } else if (cpuLoad >= 40.0) {
            cpuColor = "§e";
        } else {
            cpuColor = "§a";
        }

        hover.add(sample("§7Obciążenie CPU: " + cpuColor + formattedCpu));

        final ServerPing.Builder builder = ping.asBuilder()
                .description(motd)
                .onlinePlayers(proxyOnline)
                .maximumPlayers(totalMax)
                .samplePlayers(hover.toArray(new ServerPing.SamplePlayer[0]));

        event.setPing(builder.build());
    }

    private static ServerPing.SamplePlayer sample(final String text) {
        return new ServerPing.SamplePlayer(text, UUID.randomUUID());
    }
}
