/*
 *
 * EndSectors – Non-Commercial License
 * (c) 2025 Endixon
 *
 */

package pl.endixon.sectors.common.nats;

import com.google.gson.Gson;
import io.nats.client.*;
import java.time.Duration;
import pl.endixon.sectors.common.packet.Packet;
import pl.endixon.sectors.common.packet.PacketListener;
import pl.endixon.sectors.common.util.Logger;

public final class NatsManager {

    private Connection connection;
    private final Gson gson = new Gson();

    public void initialize(String url, String connectionName) {
        try {
            Options options = new Options.Builder()
                    .server(url)
                    .connectionName(connectionName)
                    .maxReconnects(-1)
                    .reconnectWait(Duration.ofSeconds(2))
                    .connectionTimeout(Duration.ofSeconds(5))
                    .build();

            this.connection = Nats.connect(options);
        } catch (Exception e) {
            Logger.info("NATS initialization failed: " + e.getMessage());
        }
    }

    public <T extends Packet> void subscribe(String subject, PacketListener<T> listener, Class<T> type) {
        if (this.connection == null) {
            return;
        }

        Dispatcher dispatcher = this.connection.createDispatcher(msg -> {
            try {
                T packet = gson.fromJson(new String(msg.getData()), type);
                listener.handle(packet);
            } catch (Exception e) {
                Logger.info("NATS listener error on subject " + subject + ": " + e.getMessage());
            }
        });

        dispatcher.subscribe(subject);
    }

    public void publish(String subject, Packet packet) {
        if (this.connection == null) {
            Logger.info("NATS not initialized, cannot publish.");
            return;
        }

        try {
            this.connection.publish(subject, gson.toJson(packet).getBytes());
        } catch (Exception e) {
            Logger.info("NATS publish failed for subject " + subject + ": " + e.getMessage());
        }
    }

    public void shutdown() {
        try {
            if (this.connection != null) {
                this.connection.close();
            }
        } catch (InterruptedException e) {
            Logger.info("Error while closing NATS connection: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}
