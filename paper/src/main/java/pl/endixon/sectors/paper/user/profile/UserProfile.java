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

package pl.endixon.sectors.paper.user.profile;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.eclipse.sisu.launch.Main;
import pl.endixon.sectors.common.sector.SectorType;
import pl.endixon.sectors.paper.PaperSector;
import pl.endixon.sectors.paper.sector.Sector;
import pl.endixon.sectors.paper.util.LoggerUtil;
import pl.endixon.sectors.paper.util.MessagesUtil;
import pl.endixon.sectors.paper.util.PlayerDataSerializerUtil;

@Getter
@Setter
public class UserProfile {

    private String name;
    private String sectorName;
    private boolean firstJoin;
    private long lastSectorTransfer;
    private long lastTransferTimestamp;
    private long transferOffsetUntil;
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
    private long dataVersion;

    private UserProfile() {
        this.sectorName = "unknown";
        this.firstJoin = true;
        this.lastSectorTransfer = 0L;
        this.lastTransferTimestamp = 0L;
        this.transferOffsetUntil = 0L;
        this.foodLevel = 20;
        this.experience = 0;
        this.experienceLevel = 0;
        this.fireTicks = 0;
        this.allowFlight = false;
        this.flying = false;
        this.x = 0;
        this.y = 0;
        this.z = 0;
        this.yaw = 0;
        this.pitch = 0;
        this.playerGameMode = "SURVIVAL";
        this.playerInventoryData = "";
        this.playerEnderChestData = "";
        this.playerEffectsData = "";
        this.dataVersion = 0L;
    }

    public UserProfile(@NonNull Player player) {
        this();
        this.name = player.getName();
        Sector current = PaperSector.getInstance().getSectorManager().getCurrentSector();
        this.sectorName = (current != null && current.getType() != SectorType.QUEUE)
                ? current.getName()
                : "unknown";
    }

    public UserProfile(@NonNull String name) {
        this();
        this.name = name;
    }

    public UserProfile(@NonNull Map<String, String> redisData) {
        this();
        this.name = redisData.getOrDefault("name", "unknown");
        this.sectorName = redisData.getOrDefault("sectorName", sectorName);
        this.firstJoin = Boolean.parseBoolean(redisData.getOrDefault("firstJoin", String.valueOf(firstJoin)));
        this.lastSectorTransfer = Long.parseLong(redisData.getOrDefault("lastSectorTransfer", String.valueOf(lastSectorTransfer)));
        this.lastTransferTimestamp = Long.parseLong(redisData.getOrDefault("lastTransferTimestamp", String.valueOf(lastTransferTimestamp)));
        this.transferOffsetUntil = Long.parseLong(redisData.getOrDefault("transferOffsetUntil", "0"));
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
        this.dataVersion = Long.parseLong(redisData.getOrDefault("dataVersion", "0"));
    }

    public void updateFromPlayer(@NonNull Player player, @NonNull Sector currentSector, boolean preserveCoordinates) {
        long previousLastSectorTransfer = this.lastSectorTransfer;
        long previousLastTransferTimestamp = this.lastTransferTimestamp;
        long previousTransferOffsetUntil = this.transferOffsetUntil;

        this.name = player.getName();
        if (!preserveCoordinates) {
            Location loc = player.getLocation();
            this.x = loc.getX();
            this.y = loc.getY();
            this.z = loc.getZ();
            this.yaw = loc.getYaw();
            this.pitch = loc.getPitch();
        }
        this.playerGameMode = player.getGameMode().name();
        this.foodLevel = player.getFoodLevel();
        this.experience = player.getTotalExperience();
        this.experienceLevel = player.getLevel();
        this.fireTicks = player.getFireTicks();
        this.allowFlight = player.getAllowFlight();
        this.flying = player.isFlying();
        this.playerInventoryData = PlayerDataSerializerUtil.serializeItemStacksToBase64(player.getInventory().getContents());
        this.playerEnderChestData = PlayerDataSerializerUtil.serializeItemStacksToBase64(player.getEnderChest().getContents());
        this.playerEffectsData = PlayerDataSerializerUtil.serializeEffects(player);
        this.sectorName = (currentSector.getType() != SectorType.QUEUE) ? currentSector.getName() : "unknown";
        this.lastSectorTransfer = previousLastSectorTransfer;
        this.lastTransferTimestamp = previousLastTransferTimestamp;
        this.transferOffsetUntil = previousTransferOffsetUntil;
    }

    public Map<String, String> toRedisMap() {
        Map<String, String> map = new HashMap<>();
        map.put("name", name);
        map.put("sectorName", sectorName);
        map.put("firstJoin", String.valueOf(firstJoin));
        map.put("lastSectorTransfer", String.valueOf(lastSectorTransfer));
        map.put("lastTransferTimestamp", String.valueOf(lastTransferTimestamp));
        map.put("transferOffsetUntil", String.valueOf(transferOffsetUntil));
        map.put("x", String.valueOf(x));
        map.put("y", String.valueOf(y));
        map.put("z", String.valueOf(z));
        map.put("yaw", String.valueOf(yaw));
        map.put("pitch", String.valueOf(pitch));
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
        map.put("dataVersion", String.valueOf(dataVersion));
        return map;
    }

    public void updateAndSave(@NonNull Player player, @NonNull Sector currentSector, boolean preserveCoordinates) {
        updateFromPlayer(player, currentSector, preserveCoordinates);
        this.dataVersion++;
        UserProfileCache.save(this);
        UserProfileCache.addToCache(this);
    }


    public void setLocationAndSave(Location loc) {
        this.x = loc.getX();
        this.y = loc.getY();
        this.z = loc.getZ();
        this.yaw = loc.getYaw();
        this.pitch = loc.getPitch();
        this.dataVersion++;
        UserProfileCache.save(this);
        UserProfileCache.addToCache(this);
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(name);
    }

    public void setLastSectorTransfer(boolean redirecting) {
        this.lastSectorTransfer = redirecting ? System.currentTimeMillis() : 0L;
        UserProfileCache.save(this);
        UserProfileCache.addToCache(this);
    }

    public void activateTransferOffset() {
        this.transferOffsetUntil = System.currentTimeMillis() + 5000L;
        UserProfileCache.save(this);
        UserProfileCache.addToCache(this);
    }


    public void applyPlayerData() {
        final Player player = this.getPlayer();
        if (player == null) return;

        final Sector current = PaperSector.getInstance().getSectorManager().getCurrentSector();
        if (current == null) return;

        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(PaperSector.getInstance(), this::applyPlayerData);
            return;
        }

        final SectorType type = current.getType();
        final Location spawn = new Location(player.getWorld(), 0.5, 70.0, 0.5);
        final Location TECHNICAL_STATION_LOCATION = new Location(player.getWorld(), 0.5, 75.0, 0.5);

        if (type == SectorType.QUEUE) {
            player.teleport(TECHNICAL_STATION_LOCATION);
            player.setGameMode(GameMode.ADVENTURE);

            for (Player online : Bukkit.getOnlinePlayers()) {
                if (online.equals(player)) continue;
                online.hidePlayer(PaperSector.getInstance(), player);
                player.hidePlayer(PaperSector.getInstance(), online);
            }
            return;
        }

        if (type == SectorType.AFK) {
            player.teleport(TECHNICAL_STATION_LOCATION);
            this.loadPlayerData(player);
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (online.equals(player)) continue;
                online.hidePlayer(PaperSector.getInstance(), player);
                player.hidePlayer(PaperSector.getInstance(), online);
            }
            return;
        }


        if (type == SectorType.SPAWN || type == SectorType.NETHER) {
            player.teleport(spawn);
            this.loadPlayerData(player);
            return;
        }
        this.loadPlayerData(player);
        this.teleportPlayerToStoredLocation(player);
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
            player.getInventory().setContents(PlayerDataSerializerUtil.deserializeItemStacksFromBase64(playerInventoryData));
        if (!playerEnderChestData.isEmpty())
            player.getEnderChest().setContents(PlayerDataSerializerUtil.deserializeItemStacksFromBase64(playerEnderChestData));
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
        player.addPotionEffects(PlayerDataSerializerUtil.deserializeEffects(playerEffectsData));
    }

    private void teleportPlayerToStoredLocation(@NonNull Player player) {
        long now = System.currentTimeMillis();
        Location targetLoc = new Location(player.getWorld(), x, y, z, yaw, pitch);

        if (now < transferOffsetUntil) {
            Vector direction = targetLoc.getDirection().setY(0).normalize();
            targetLoc = targetLoc.clone().add(direction.multiply(6.0));
        }
        player.teleport(targetLoc);
        startProtectionTask(player);
    }

    public void startProtectionTask(Player player) {
        int protectionSeconds = PaperSector.getInstance().getConfiguration().protectionSeconds;
        player.setInvulnerable(true);

        new BukkitRunnable() {
            int remainingTicks = protectionSeconds * 20;

            @Override
            public void run() {
                if (!player.isOnline() || remainingTicks <= 0) {
                    player.setInvulnerable(false);
                    this.cancel();
                    return;
                }
                if (remainingTicks % 20 == 0) {
                    int seconds = remainingTicks / 20;
                    player.sendActionBar(MessagesUtil.PROTECTION_ACTIONBAR.get("{SECONDS}", String.valueOf(seconds)
                    ));
                }

                remainingTicks--;
            }
        }.runTaskTimer(PaperSector.getInstance(), 0, 1);
    }
}

