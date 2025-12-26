package pl.endixon.sectors.paper.hook;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import pl.endixon.sectors.common.Common;
import pl.endixon.sectors.common.packet.object.PacketHeartbeat;
import pl.endixon.sectors.common.util.LoggerUtil;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class CommonHeartbeatHook {

    private static final String SUBJECT_PING = "common.heartbeat.ping";
    private static final String SUBJECT_PONG = "common.heartbeat.pong";
    private final JavaPlugin plugin;

    private final AtomicBoolean commonAlive = new AtomicBoolean(false);
    private final AtomicBoolean awaitingPong = new AtomicBoolean(false);
    private CountDownLatch latch;

    public CommonHeartbeatHook(final JavaPlugin plugin) {
        this.plugin = plugin;
        this.initializeNatsSubscription();
    }

    private void initializeNatsSubscription() {
        Common.getInstance().getNatsManager().subscribe(
                SUBJECT_PONG,
                this::handlePong,
                PacketHeartbeat.class
        );
    }

    private void handlePong(final PacketHeartbeat packet) {
        if (packet == null) {
            return;
        }
        if (!packet.isStatus()) {
            this.handleConnectionLoss("Common emergency signal: " + packet.getMessage());
            return;
        }

        if (this.awaitingPong.get()) {
            if (!this.commonAlive.get()) {
                LoggerUtil.info("[COMMON HOOK] Connection to Common App established! Systems nominal.");
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
            final PacketHeartbeat ping = new PacketHeartbeat("SectorCheck", true);
            Common.getInstance().getNatsManager().publish(SUBJECT_PING, ping);

            if (!this.commonAlive.get()) {
                final boolean received = this.latch.await(2500, TimeUnit.MILLISECONDS);

                if (!received || !this.commonAlive.get()) {
                    this.handleConnectionLoss("Initial handshake failed! Timeout reached.");
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            this.handleConnectionLoss("Thread interrupted during handshake.");
        } catch (Exception exception) {
            this.handleConnectionLoss("NATS infrastructure failure: " + exception.getMessage());
        }
    }

    private void handleConnectionLoss(final String reason) {
        this.commonAlive.set(false);
        this.awaitingPong.set(false);
        LoggerUtil.error("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        LoggerUtil.error("   CRITICAL FAILURE: COMMON SYSTEM IS OFFLINE       ");
        LoggerUtil.error("   REASON: " + reason);
        LoggerUtil.error("   ACTION: INITIATING EMERGENCY SERVER SHUTDOWN     ");
        LoggerUtil.error("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        Bukkit.getScheduler().runTask(this.plugin, () -> {
            LoggerUtil.warn("Saving data and stopping the server...");
            Bukkit.shutdown();
        });
    }

    public void shutdown() {
        this.commonAlive.set(false);
        this.awaitingPong.set(false);
    }
}