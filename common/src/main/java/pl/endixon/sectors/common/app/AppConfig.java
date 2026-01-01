package pl.endixon.sectors.common.app;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppConfig {
    private String natsUrl = "nats://user:password@127.0.0.1:4222";
    private String natsClientName = "common-app";
    private String redisHost = "127.0.0.1";
    private int redisPort = 6379;
    private String redisPassword = "password";
    private boolean flowLoggerEnabled = true;
    private long resourceMonitorInterval = 300000;
}