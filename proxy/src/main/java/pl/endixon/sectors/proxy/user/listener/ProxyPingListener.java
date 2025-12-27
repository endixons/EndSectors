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
import pl.endixon.sectors.common.sector.SectorData;
import pl.endixon.sectors.proxy.VelocitySectorPlugin;
import pl.endixon.sectors.proxy.manager.SectorManager;
import pl.endixon.sectors.proxy.util.CpuUtil;
import pl.endixon.sectors.proxy.util.ProxyMessages;

import java.util.List;
import java.util.UUID;

public final class ProxyPingListener {

    private final VelocitySectorPlugin plugin = VelocitySectorPlugin.getInstance();
    private final ProxyServer proxyServer = plugin.getServer();
    private final SectorManager sectorManager = plugin.getSectorManager();

    @Subscribe
    public void onProxyPing(final ProxyPingEvent event) {
        if (this.plugin.getHeartbeatHook() == null || !this.plugin.getHeartbeatHook().isCommonReady()) {
            this.handleEmergencyPing(event);
            return;
        }

        int proxyOnline = this.proxyServer.getPlayerCount();
        int totalMax = 0;
        int activeSectors = 0;

        for (SectorData sector : this.sectorManager.getSectorsData()) {
            if (sector.isOnline()) {
                totalMax += sector.getMaxPlayers();
                activeSectors++;
            }
        }

        double cpuLoad = CpuUtil.getCpuLoad();
        String formattedCpu = String.format("%.1f%%", cpuLoad);
        String cpuColor = (cpuLoad >= 70.0) ? "§c" : (cpuLoad >= 40.0) ? "§e" : "§a";

        Component motd = ProxyMessages.PROXY_MOTD.getMotd(
                "{ACTIVE_SECTORS}", String.valueOf(activeSectors),
                "{ONLINE_PLAYERS}", String.valueOf(proxyOnline),
                "{CPU}", cpuColor + formattedCpu
        );

        List<String> hoverLines = ProxyMessages.PROXY_HOVER.getRawLines(
                "{ACTIVE_SECTORS}", String.valueOf(activeSectors),
                "{ONLINE_PLAYERS}", String.valueOf(proxyOnline),
                "{CPU}", cpuColor + formattedCpu
        );

        ServerPing.SamplePlayer[] hover = hoverLines.stream()
                .map(line -> new ServerPing.SamplePlayer(line, UUID.randomUUID()))
                .toArray(ServerPing.SamplePlayer[]::new);

        ServerPing.Builder builder = event.getPing().asBuilder()
                .description(motd)
                .onlinePlayers(proxyOnline)
                .maximumPlayers(totalMax)
                .samplePlayers(hover);

        event.setPing(builder.build());
    }

    private void handleEmergencyPing(final ProxyPingEvent event) {
        Component emergencyMotd = ProxyMessages.EMERGENCY_MOTD.getMotd();
        List<String> hoverLines = ProxyMessages.EMERGENCY_HOVER.getRawLines();

        ServerPing.SamplePlayer[] hover = hoverLines.stream()
                .map(line -> new ServerPing.SamplePlayer(line, UUID.randomUUID()))
                .toArray(ServerPing.SamplePlayer[]::new);

        ServerPing.Builder builder = event.getPing().asBuilder()
                .description(emergencyMotd)
                .onlinePlayers(0)
                .maximumPlayers(0)
                .samplePlayers(hover);
        event.setPing(builder.build());
    }
}
