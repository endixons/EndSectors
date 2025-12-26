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
import pl.endixon.sectors.proxy.util.LoggerUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ProxyPingListener {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final ServerPing.SamplePlayer[] EMPTY_SAMPLE = new ServerPing.SamplePlayer[0];

    private final VelocitySectorPlugin plugin = VelocitySectorPlugin.getInstance();
    private final ProxyServer proxyServer = plugin.getServer();
    private final SectorManager sectorManager = plugin.getSectorManager();

    @Subscribe
    public void onProxyPing(final ProxyPingEvent event) {
        if (this.plugin.getHeartbeatHook() == null || !this.plugin.getHeartbeatHook().isCommonReady()) {
            this.handleEmergencyPing(event);
            return;
        }

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
        final String cpuColor = (cpuLoad >= 70.0) ? "§c" : (cpuLoad >= 40.0) ? "§e" : "§a";

        hover.add(this.createSample("§b§lENDSECTORS FRAMEWORK"));
        hover.add(this.createSample("§7Status systemu: §eTESTOWY"));
        hover.add(this.createSample(""));
        hover.add(this.createSample("§7Aktywne sektory: §a" + activeSectors));
        hover.add(this.createSample("§7Gracze online: §a" + proxyOnline));
        hover.add(this.createSample(""));
        hover.add(this.createSample("§e§l» KLIKNIJ ABY PRZETESTOWAĆ SYSTEM «"));
        hover.add(this.createSample("§7Obciążenie CPU: " + cpuColor + formattedCpu));

        final ServerPing.Builder builder = event.getPing().asBuilder()
                .description(motd)
                .onlinePlayers(proxyOnline)
                .maximumPlayers(totalMax)
                .samplePlayers(hover.toArray(EMPTY_SAMPLE));

        event.setPing(builder.build());
    }

    private void handleEmergencyPing(final ProxyPingEvent event) {
        final Component emergencyMotd = MINI_MESSAGE.deserialize(
                "<bold><gradient:#ff4b2b:#ff416c>ENDSECTORS</gradient></bold> <gray>•</gray> " +
                        "<gradient:#ffe259:#ffa751>PRACE KONSERWACYJNE</gradient>\n" +
                        "<gray>Status: </gray><red>Trwa optymalizacja infrastruktury. Zapraszamy wkrótce!</red>"
        );

        final List<ServerPing.SamplePlayer> emergencyHover = new ArrayList<>();
        emergencyHover.add(this.createSample("§6§lDODATKOWE INFORMACJE"));
        emergencyHover.add(this.createSample("§7Aktualnie przeprowadzamy §eplanowane §7prace"));
        emergencyHover.add(this.createSample("§7nad wydajnością naszych systemów."));
        emergencyHover.add(this.createSample(""));
        emergencyHover.add(this.createSample("§fPrzewidywany czas powrotu: §aKilka minut"));
        emergencyHover.add(this.createSample("§eDziękujemy za cierpliwość!"));

        final ServerPing.Builder builder = event.getPing().asBuilder()
                .description(emergencyMotd)
                .onlinePlayers(0)
                .maximumPlayers(0)
                .samplePlayers(emergencyHover.toArray(EMPTY_SAMPLE));

        event.setPing(builder.build());
    }

    private ServerPing.SamplePlayer createSample(final String text) {
        return new ServerPing.SamplePlayer(text, UUID.randomUUID());
    }
}