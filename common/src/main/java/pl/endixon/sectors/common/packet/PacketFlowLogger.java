package pl.endixon.sectors.common.packet;

import pl.endixon.sectors.common.Common;
import pl.endixon.sectors.common.packet.object.PacketHeartbeat;

public final class PacketFlowLogger {

    private boolean enabled = false;

    public void enable() {
        this.enabled = true;
        Common.getInstance().getLogger().info("PacketFlowLogger enabled (Selective IN/OUT logging).");
    }

    public <T extends Packet> void logIncoming(String subject, T packet) {
        if (!this.enabled || this.isHeartbeat(packet)) {
            return;
        }

        Common.getInstance().getLogger().info(String.format("[NATS IN]  %s -> %s", subject, packet.getClass().getSimpleName()));
    }

    public void logOutgoing(String subject, Packet packet) {
        if (!this.enabled || this.isHeartbeat(packet)) {
            return;
        }

        Common.getInstance().getLogger().info(String.format("[NATS OUT] %s -> %s", subject, packet.getClass().getSimpleName()));
    }

    private boolean isHeartbeat(Packet packet) {
        return packet instanceof PacketHeartbeat;
    }
}