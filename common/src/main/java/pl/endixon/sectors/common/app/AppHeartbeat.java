package pl.endixon.sectors.common.app;

import pl.endixon.sectors.common.Common;
import pl.endixon.sectors.common.nats.NatsManager;
import pl.endixon.sectors.common.packet.object.PacketHeartbeat;


public final class AppHeartbeat {

    private static final String SUBJECT_PING = "common.heartbeat.ping";
    private static final String SUBJECT_PONG = "common.heartbeat.pong";

    private final NatsManager natsManager;
    private boolean running = false;

    public AppHeartbeat(NatsManager natsManager) {
        this.natsManager = natsManager;
    }


    public void start() {
        this.running = true;
        this.natsManager.subscribe(SUBJECT_PING, this::handlePing, PacketHeartbeat.class);
        Common.getInstance().getLogger().info("AppHeartbeat service is now ACTIVE (Responder Mode).");
    }


    private void handlePing(PacketHeartbeat packet) {
        if (!this.running || packet == null) {
            return;
        }
        Common.getInstance().getLogger().info("[HEARTBEAT] Health check received from: " + packet.getMessage());
        PacketHeartbeat pong = new PacketHeartbeat("PONG: " + packet.getMessage(), true);
        this.natsManager.publish(SUBJECT_PONG, pong);
    }


    public void broadcastEmergencyStop(String reason) {
        PacketHeartbeat deathSignal = new PacketHeartbeat("CRITICAL_STOP: " + reason, false);
        this.natsManager.publish(SUBJECT_PONG, deathSignal);
        Common.getInstance().getLogger().error("Emergency stop signal broadcasted to NATS.");
    }

    public void stop() {
        if (!this.running) return;

        this.running = false;
        PacketHeartbeat shutdownSignal = new PacketHeartbeat("SHUTDOWN_CLEAN", false);
        this.natsManager.publish(SUBJECT_PONG, shutdownSignal);

        Common.getInstance().getLogger().info("AppHeartbeat service stopped gracefully.");
    }
}