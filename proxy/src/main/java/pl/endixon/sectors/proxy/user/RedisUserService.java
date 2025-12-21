package pl.endixon.sectors.proxy.user;

import java.util.Map;
import java.util.Optional;
import pl.endixon.sectors.proxy.VelocitySectorPlugin;

public class RedisUserService {

    private static final String PREFIX = "user:";
    private final VelocitySectorPlugin plugin;

    public RedisUserService(VelocitySectorPlugin plugin) {
        this.plugin = plugin;
    }

    public Optional<String> getSectorName(String playerName) {
        if (playerName == null || playerName.isEmpty())
            return Optional.empty();
        String key = buildKey(playerName);
        Map<String, String> data = plugin.getRedisService().hgetAll(key);
        if (data == null || data.isEmpty())
            return Optional.empty();
        return Optional.ofNullable(data.get("sectorName"));
    }

    public void setSectorName(String playerName, String sectorName) {
        if (playerName == null || playerName.isEmpty() || sectorName == null)
            return;
        String key = buildKey(playerName);
        plugin.getRedisService().hset(key, Map.of("sectorName", sectorName));
    }

    private String buildKey(String playerName) {
        return PREFIX + playerName.toLowerCase();
    }
}
