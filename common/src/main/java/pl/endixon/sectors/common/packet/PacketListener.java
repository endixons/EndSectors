package pl.endixon.sectors.common.packet;

public interface PacketListener<T> {

    void handle(T packet);
}
