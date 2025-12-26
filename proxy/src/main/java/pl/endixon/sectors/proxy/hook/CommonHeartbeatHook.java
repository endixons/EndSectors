package pl.endixon.sectors.proxy.hook;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import pl.endixon.sectors.common.Common;
import pl.endixon.sectors.common.packet.object.PacketHeartbeat;
import pl.endixon.sectors.common.util.LoggerUtil;

import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class CommonHeartbeatHook {

    private static final String SUBJECT_PING = "common.heartbeat.ping";
    private static final String SUBJECT_PONG = "common.heartbeat.pong";

    private final ProxyServer server;
    private final AtomicBoolean commonAlive = new AtomicBoolean(false);
    private final AtomicBoolean awaitingPong = new AtomicBoolean(false);
    private CountDownLatch latch;

    public CommonHeartbeatHook(ProxyServer server) {
        this.server = server;
        Common.getInstance().getNatsManager().subscribe(
                SUBJECT_PONG,
                this::handlePong,
                PacketHeartbeat.class
        );
    }

    private void handlePong(PacketHeartbeat packet) {
        if (packet == null) return;


        if (!packet.isStatus()) {
            this.handleConnectionLoss("Common emergency shutdown: " + packet.getMessage());
            return;
        }

        if (this.awaitingPong.get()) {
            if (!this.commonAlive.get()) {
                LoggerUtil.info("[COMMON HOOK] Connection to Common App established!");
            }

            this.commonAlive.set(true);
            this.awaitingPong.set(false);

            if (this.latch != null) {
                this.latch.countDown();
            }
        }
    }

    public void checkConnection() {
        try {
            this.awaitingPong.set(true);
            this.latch = new CountDownLatch(1);

            PacketHeartbeat ping = new PacketHeartbeat("ProxyCheck", true);
            Common.getInstance().getNatsManager().publish(SUBJECT_PING, ping);

            if (!this.commonAlive.get()) {
                boolean received = this.latch.await(2500, TimeUnit.MILLISECONDS);

                if (!received || !this.commonAlive.get()) {
                    this.handleConnectionLoss("Initial handshake failed! Timeout reached.");
                }
            }
        } catch (Exception exception) {
            this.handleConnectionLoss("NATS infrastructure failure: " + exception.getMessage());
        }
    }

    private void handleConnectionLoss(final String reason) {
        this.commonAlive.set(false);
        this.awaitingPong.set(false);

        final Collection<Player> onlinePlayers = this.server.getAllPlayers();

        LoggerUtil.error("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        LoggerUtil.error("   CRITICAL FAILURE: COMMON SYSTEM IS OFFLINE       ");
        LoggerUtil.error("   REASON: " + reason);
        LoggerUtil.error("   CURRENT LOAD: " + onlinePlayers.size() + " active sessions.");
        LoggerUtil.error("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

        if (onlinePlayers.isEmpty()) {
            LoggerUtil.warn("[LUCKY STRIKE] Infrastructure collapsed, but the proxy was empty. No UX impact reported.");
            return;
        }
        LoggerUtil.info("   [ACTION] Initiating mass disconnect sequence...");
        final Component disconnectMessage = Component.text("§cPołączenie z infrastrukturą zostało przerwane.\n§7Trwa próba przywrócenia usług...");

        for (final Player player : onlinePlayers) {
            player.disconnect(disconnectMessage);
        }
        LoggerUtil.info("   [SUCCESS] Cleaned up " + onlinePlayers.size() + " sessions.");
    }

    public boolean isCommonReady() {
        return this.commonAlive.get();
    }

    public void shutdown() {
        this.commonAlive.set(false);
        this.awaitingPong.set(false);
    }
}