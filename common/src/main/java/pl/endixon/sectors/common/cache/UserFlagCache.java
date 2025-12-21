package pl.endixon.sectors.common.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserFlagCache {

    private final Map<String, Boolean> existsFlags = new ConcurrentHashMap<>();
    private final Map<String, Boolean> requestedFlags = new ConcurrentHashMap<>();
    private final Map<String, String> lastSectorMap = new ConcurrentHashMap<>();

    private static final UserFlagCache INSTANCE = new UserFlagCache();

    private UserFlagCache() {
    }

    public static UserFlagCache getInstance() {
        return INSTANCE;
    }

    private String key(String username) {
        return username;
    }

    public void setExists(String username, boolean exists) {
        existsFlags.put(key(username), exists);
    }

    public boolean exists(String username) {
        return existsFlags.getOrDefault(key(username), false);
    }

    public void setRequested(String username, boolean requested) {
        requestedFlags.put(key(username), requested);
    }

    public boolean isRequested(String username) {
        return requestedFlags.getOrDefault(key(username), false);
    }

    public void setLastSector(String username, String sector) {
        lastSectorMap.put(key(username), sector);
    }

    public String getLastSector(String username) {
        return lastSectorMap.get(key(username));
    }

    public void remove(String username) {
        String k = key(username);
        existsFlags.remove(k);
        requestedFlags.remove(k);
        lastSectorMap.remove(k);
    }

    public void clear() {
        existsFlags.clear();
        requestedFlags.clear();
        lastSectorMap.clear();
    }
}
