/*
 *
 * EndSectors – Non-Commercial License
 * (c) 2025 Endixon
 *
 * Permission is granted to use, copy, and
 * modify this software **only** for personal
 * or educational purposes.
 *
 * Commercial use, redistribution, claiming
 * this work as your own, or copying code
 * without explicit permission is strictly
 * prohibited.
 *
 * Visit https://github.com/Endixon/EndSectors
 * for more info.
 *
 */

package pl.endixon.sectors.common.nats;

import com.google.gson.Gson;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Nats;
import io.nats.client.Options;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import pl.endixon.sectors.common.Common;
import pl.endixon.sectors.common.nats.listener.NatsErrorListener;
import pl.endixon.sectors.common.packet.Packet;
import pl.endixon.sectors.common.packet.PacketListener;
import pl.endixon.sectors.common.util.LoggerUtil;


public final class NatsManager {

    private final Gson gson = new Gson();
    private final ExecutorService processingExecutor = Executors.newFixedThreadPool(4);
    private final Map<String, Dispatcher> dispatchers = new ConcurrentHashMap<>();
    private Connection connection;

    public void initialize(String url, String connectionName) {
        try {
            Options options = new Options.Builder()
                    .server(url)
                    .connectionName(connectionName)
                    .maxReconnects(-1)
                    .reconnectWait(Duration.ofSeconds(10))
                    .connectionTimeout(Duration.ofSeconds(10))
                    .errorListener(new NatsErrorListener())
                    .connectionListener((conn, type) -> {
                        LoggerUtil.info("NATS Connection Event: " + type);
                    })
                    .build();

            this.connection = Nats.connect(options);
            LoggerUtil.info("NATS connection established: " + connectionName);
        } catch (Exception exception) {
            LoggerUtil.error("NATS initialization failed: " + exception.getMessage());
        }
    }

    public <T extends Packet> void subscribe(String subject, PacketListener<T> listener, Class<T> packetType) {
        if (this.connection == null) {
            LoggerUtil.warn("Cannot subscribe, connection is null.");
            return;
        }
        Dispatcher dispatcher = this.connection.createDispatcher(msg -> {
            this.processingExecutor.submit(() -> {
                try {
                    String json = new String(msg.getData(), StandardCharsets.UTF_8);
                    T packet = this.gson.fromJson(json, packetType);
                    listener.handle(packet);

                    if(Common.getInstance().isAppBootstrap()) {
                        Common.getInstance().getFlowLogger().logIncoming(subject, packet);
                    }

                } catch (Exception exception) {
                    LoggerUtil.error("Error processing packet on subject " + subject + ": " + exception.getMessage());
                }
            });
        });

        dispatcher.subscribe(subject);
        dispatchers.put(subject, dispatcher);
    }

    public void publish(String subject, Packet packet) {
        if (this.connection == null) {
            LoggerUtil.error("NATS not initialized, cannot publish.");
            return;
        }

        try {
            byte[] data = this.gson.toJson(packet).getBytes(StandardCharsets.UTF_8);
            this.connection.publish(subject, data);


            if (Common.getInstance().isAppBootstrap()) {
                Common.getInstance().getFlowLogger().logOutgoing(subject, packet);
            }

        } catch (Exception exception) {
            LoggerUtil.error("NATS publish failed for subject " + subject + ": " + exception.getMessage());
        }
    }



    public void shutdown() {
        try {
            if (this.connection != null) {
                this.connection.drain(Duration.ofSeconds(5));
                this.connection.close();
            }

            this.processingExecutor.shutdown();
            if (!this.processingExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                this.processingExecutor.shutdownNow();
            }
        } catch (Exception exception) {
            LoggerUtil.error("Error during NATS shutdown: " + exception.getMessage());
        }
    }
}