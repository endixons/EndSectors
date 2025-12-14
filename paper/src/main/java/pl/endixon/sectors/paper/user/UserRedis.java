package pl.endixon.sectors.paper.user;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import pl.endixon.sectors.common.sector.SectorType;
import pl.endixon.sectors.paper.PaperSector;
import pl.endixon.sectors.paper.sector.Sector;
import pl.endixon.sectors.paper.util.Logger;
import pl.endixon.sectors.paper.util.PlayerDataSerializer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
@Getter
@Setter
public class UserRedis {

    private String name;
    private String sectorName;
    private boolean firstJoin;
    private long lastSectorTransfer;
    private long lastTransferTimestamp;
    private boolean teleportingToSector;
    private int foodLevel;
    private int experience;
    private int experienceLevel;
    private int fireTicks;
    private boolean allowFlight;
    private boolean flying;
    private double x, y, z;
    private float yaw, pitch;
    private String playerGameMode;
    private String playerInventoryData;
    private String playerEnderChestData;
    private String playerEffectsData;

    // Prywatny konstruktor bazowy z domyślnymi wartościami
    private UserRedis() {
        this.sectorName = "null";
        this.firstJoin = true;
        this.lastSectorTransfer = 0L;
        this.lastTransferTimestamp = 0L;
        this.teleportingToSector = false;
        this.foodLevel = 20;
        this.experience = 0;
        this.experienceLevel = 0;
        this.fireTicks = 0;
        this.allowFlight = false;
        this.flying = false;
        this.x = 0; this.y = 0; this.z = 0;
        this.yaw = 0; this.pitch = 0;
        this.playerGameMode = "SURVIVAL";
        this.playerInventoryData = "";
        this.playerEnderChestData = "";
        this.playerEffectsData = "";
    }

    public UserRedis(@NonNull Player player) {
        this();
        this.name = player.getName();
        Sector current = PaperSector.getInstance().getSectorManager().getCurrentSector();
        this.sectorName = (current != null && current.getType() != SectorType.QUEUE) ? current.getName() : "null";
    }

    public UserRedis(@NonNull String name) {
        this();
        this.name = name;
    }

    public UserRedis(@NonNull Map<String, String> redisData) {
        this();
        this.name = redisData.getOrDefault("name", "unknown");
        this.sectorName = redisData.getOrDefault("sectorName", sectorName);
        this.firstJoin = Boolean.parseBoolean(redisData.getOrDefault("firstJoin", String.valueOf(firstJoin)));
        this.lastSectorTransfer = Long.parseLong(redisData.getOrDefault("lastSectorTransfer", String.valueOf(lastSectorTransfer)));
        this.lastTransferTimestamp = Long.parseLong(redisData.getOrDefault("lastTransferTimestamp", String.valueOf(lastTransferTimestamp)));
        this.teleportingToSector = Boolean.parseBoolean(redisData.getOrDefault("teleportingToSector", String.valueOf(teleportingToSector)));
        this.x = Double.parseDouble(redisData.getOrDefault("x", String.valueOf(x)));
        this.y = Double.parseDouble(redisData.getOrDefault("y", String.valueOf(y)));
        this.z = Double.parseDouble(redisData.getOrDefault("z", String.valueOf(z)));
        this.yaw = Float.parseFloat(redisData.getOrDefault("yaw", String.valueOf(yaw)));
        this.pitch = Float.parseFloat(redisData.getOrDefault("pitch", String.valueOf(pitch)));
        this.playerGameMode = redisData.getOrDefault("playerGameMode", playerGameMode);
        this.playerInventoryData = redisData.getOrDefault("playerInventoryData", playerInventoryData);
        this.playerEnderChestData = redisData.getOrDefault("playerEnderChestData", playerEnderChestData);
        this.playerEffectsData = redisData.getOrDefault("playerEffectsData", playerEffectsData);
        this.foodLevel = Integer.parseInt(redisData.getOrDefault("foodLevel", String.valueOf(foodLevel)));
        this.experience = Integer.parseInt(redisData.getOrDefault("experience", String.valueOf(experience)));
        this.experienceLevel = Integer.parseInt(redisData.getOrDefault("experienceLevel", String.valueOf(experienceLevel)));
        this.fireTicks = Integer.parseInt(redisData.getOrDefault("fireTicks", String.valueOf(fireTicks)));
        this.allowFlight = Boolean.parseBoolean(redisData.getOrDefault("allowFlight", String.valueOf(allowFlight)));
        this.flying = Boolean.parseBoolean(redisData.getOrDefault("flying", String.valueOf(flying)));
    }


    public void updateFromPlayer(@NonNull Player player, @NonNull Sector currentSector) {
        this.name = player.getName();
        Location loc = player.getLocation();
        this.x = loc.getX(); this.y = loc.getY(); this.z = loc.getZ();
        this.yaw = loc.getYaw(); this.pitch = loc.getPitch();
        this.playerGameMode = player.getGameMode().name();
        this.foodLevel = player.getFoodLevel();
        this.experience = player.getTotalExperience();
        this.experienceLevel = player.getLevel();
        this.fireTicks = player.getFireTicks();
        this.allowFlight = player.getAllowFlight();
        this.flying = player.isFlying();
        this.playerInventoryData = PlayerDataSerializer.serializeItemStacksToBase64(player.getInventory().getContents());
        this.playerEnderChestData = PlayerDataSerializer.serializeItemStacksToBase64(player.getEnderChest().getContents());
        this.playerEffectsData = PlayerDataSerializer.serializeEffects(player);
        this.sectorName = (currentSector != null && currentSector.getType() != SectorType.QUEUE) ? currentSector.getName() : "null";
    }

    public void updateAndSave(@NonNull Player player, @NonNull Sector currentSector) {
        updateFromPlayer(player, currentSector);
        saveAsync();
    }

    public Map<String, String> toRedisMap() {
        Map<String, String> map = new HashMap<>();
        map.put("name", name);
        map.put("sectorName", sectorName);
        map.put("firstJoin", String.valueOf(firstJoin));
        map.put("lastSectorTransfer", String.valueOf(lastSectorTransfer));
        map.put("lastTransferTimestamp", String.valueOf(lastTransferTimestamp));
        map.put("teleportingToSector", String.valueOf(teleportingToSector));
        map.put("x", String.valueOf(x)); map.put("y", String.valueOf(y)); map.put("z", String.valueOf(z));
        map.put("yaw", String.valueOf(yaw)); map.put("pitch", String.valueOf(pitch));
        map.put("playerGameMode", playerGameMode);
        map.put("playerInventoryData", playerInventoryData);
        map.put("playerEnderChestData", playerEnderChestData);
        map.put("playerEffectsData", playerEffectsData);
        map.put("foodLevel", String.valueOf(foodLevel));
        map.put("experience", String.valueOf(experience));
        map.put("experienceLevel", String.valueOf(experienceLevel));
        map.put("fireTicks", String.valueOf(fireTicks));
        map.put("allowFlight", String.valueOf(allowFlight));
        map.put("flying", String.valueOf(flying));
        return map;
    }

    public void saveAsync() {
        CompletableFuture.runAsync(() -> RedisUserCache.save(this))
                .exceptionally(ex -> {
                    Logger.info(() -> "Failed to save user " + name + " to Redis: " + ex.getMessage());
                    return null;
                });
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(name);
    }

    public void applyPlayerData() {
        Player player = getPlayer();
        if (player == null) return;
        Sector current = PaperSector.getInstance().getSectorManager().getCurrentSector();
        if (current == null) return;

        switch (current.getType()) {
            case QUEUE -> handleQueueSector(player);
            case NETHER -> handleNetherSector(player);
            case SPAWN -> handleSpawnSector(player);
            default -> handleDefaultSector(player);
        }
    }

    public void setLastSectorTransfer(boolean redirecting) {
        this.lastSectorTransfer = redirecting ? System.currentTimeMillis() : 0L;
        saveAsync();
    }

    // --- Private helpers ---
    private void handleQueueSector(Player player) {
        Location loc = new Location(player.getWorld(), 0, 70, 0);
        if (player.teleport(loc)) {
            player.setGameMode(GameMode.ADVENTURE);
            Bukkit.getOnlinePlayers().forEach(online -> {
                if (!online.equals(player)) online.hidePlayer(PaperSector.getInstance(), player);
                if (!online.equals(player)) player.hidePlayer(PaperSector.getInstance(), online);
            });
        }
    }

    private void handleNetherSector(Player player) {
        Location loc = new Location(player.getWorld(), 0, 70, 0);
        if (player.teleport(loc)) loadPlayerData(player);
    }

    private void handleSpawnSector(Player player) {
        Location loc = new Location(player.getWorld(), 0, 70, 0);
        if (player.teleport(loc)) loadPlayerData(player);
    }

    private void handleDefaultSector(Player player) {
        teleportPlayerToStoredLocation(player);
        loadPlayerData(player);
    }

    private void loadPlayerData(@NonNull Player player) {
        player.setGameMode(GameMode.valueOf(playerGameMode));
        player.setFoodLevel(foodLevel);
        player.setTotalExperience(experience);
        player.setLevel(experienceLevel);
        player.setFireTicks(fireTicks);
        player.setAllowFlight(allowFlight);
        player.setFlying(flying);

        if (!playerInventoryData.isEmpty())
            player.getInventory().setContents(PlayerDataSerializer.deserializeItemStacksFromBase64(playerInventoryData));

        if (!playerEnderChestData.isEmpty())
            player.getEnderChest().setContents(PlayerDataSerializer.deserializeItemStacksFromBase64(playerEnderChestData));

        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
        PlayerDataSerializer.deserializeEffects(playerEffectsData).forEach(player::addPotionEffect);
    }

    private void teleportPlayerToStoredLocation(@NonNull Player player) {
        Location loc = new Location(player.getWorld(), x, y, z, yaw, pitch);
        Vector direction = loc.getDirection();
        direction.setY(0).normalize().multiply(4);
        loc.add(direction);
        player.teleport(loc);
    }
}
