package pl.endixon.sectors.proxy.user;

import java.util.Map;
import java.util.Optional;
import pl.endixon.sectors.proxy.VelocitySectorPlugin;
import pl.endixon.sectors.proxy.util.LoggerUtil;


public class RedisUserService {

    private static final String KEY_PREFIX = "user:";
    private static final String FIELD_SECTOR = "sectorName";
    private static final String VALUE_UNKNOWN = "unknown";

    private final VelocitySectorPlugin plugin;

    public RedisUserService(VelocitySectorPlugin plugin) {
        this.plugin = plugin;
    }

    public Optional<String> getSectorName(String playerName) {
        if (playerName == null || playerName.isBlank()) {
            return Optional.empty();
        }

        final String key = buildKey(playerName);

        try {
            final Map<String, String> data = this.plugin.getRedisService().hgetAll(key);

            if (data == null || data.isEmpty()) {
                return Optional.empty();
            }

            final String sector = data.get(FIELD_SECTOR);

            if (sector == null || sector.isBlank() || sector.trim().equalsIgnoreCase(VALUE_UNKNOWN)) {
                return Optional.empty();
            }

            return Optional.of(sector.trim());
        } catch (Exception exception) {
            LoggerUtil.info("[CRITICAL] Failed to retrieve sector data for " + playerName + ": " + exception.getMessage());
            return Optional.empty();
        }
    }

    public void setSectorName(String playerName, String sectorName) {
        if (playerName == null || playerName.isBlank() || sectorName == null || sectorName.isBlank()) {
            return;
        }

        final String sanitizedSector = sectorName.trim();

        if (sanitizedSector.equalsIgnoreCase(VALUE_UNKNOWN)) {
            return;
        }

        final String key = buildKey(playerName);

        try {
            this.plugin.getRedisService().hset(key, Map.of(FIELD_SECTOR, sanitizedSector));
        } catch (Exception exception) {
            LoggerUtil.info("[CRITICAL] Failed to persist sector '" + sanitizedSector + "' for " + playerName + ": " + exception.getMessage());
        }
    }

    private String buildKey(String playerName) {
        return KEY_PREFIX + playerName.toLowerCase();
    }
}