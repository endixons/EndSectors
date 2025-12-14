package pl.endixon.sectors.paper.redis.packet;

import lombok.Getter;
import pl.endixon.sectors.common.packet.Packet;
import pl.endixon.sectors.paper.user.UserRedis;

@Getter
public class PacketPlayerInfoRequest implements Packet {

    private final String name;
    private final String sectorName;
    private final boolean firstJoin;
    private final long lastSectorTransfer;
    private final long lastTransferTimestamp;
    private final boolean teleportingToSector;
    private final int foodLevel;
    private final int experience;
    private final int experienceLevel;
    private final int fireTicks;
    private final boolean allowFlight;
    private final boolean flying;
    private final String playerGameMode;
    private final String playerInventoryData;
    private final String playerEnderChestData;
    private final String playerEffectsData;

    public PacketPlayerInfoRequest(UserRedis user) {
        this(
                user.getName(),
                user.getSectorName(),
                user.isFirstJoin(),
                user.getLastSectorTransfer(),
                user.getLastTransferTimestamp(),
                user.isTeleportingToSector(),
                user.getFoodLevel(),
                user.getExperience(),
                user.getExperienceLevel(),
                user.getFireTicks(),
                user.isAllowFlight(),
                user.isFlying(),
                user.getPlayerGameMode(),
                user.getPlayerInventoryData(),
                user.getPlayerEnderChestData(),
                user.getPlayerEffectsData()
        );
    }

    public PacketPlayerInfoRequest(
            String name,
            String sectorName,
            boolean firstJoin,
            long lastSectorTransfer,
            long lastTransferTimestamp,
            boolean teleportingToSector,
            int foodLevel,
            int experience,
            int experienceLevel,
            int fireTicks,
            boolean allowFlight,
            boolean flying,
            String playerGameMode,
            String playerInventoryData,
            String playerEnderChestData,
            String playerEffectsData
    ) {
        this.name = name;
        this.sectorName = sectorName;
        this.firstJoin = firstJoin;
        this.lastSectorTransfer = lastSectorTransfer;
        this.lastTransferTimestamp = lastTransferTimestamp;
        this.teleportingToSector = teleportingToSector;
        this.foodLevel = foodLevel;
        this.experience = experience;
        this.experienceLevel = experienceLevel;
        this.fireTicks = fireTicks;
        this.allowFlight = allowFlight;
        this.flying = flying;
        this.playerGameMode = playerGameMode;
        this.playerInventoryData = playerInventoryData;
        this.playerEnderChestData = playerEnderChestData;
        this.playerEffectsData = playerEffectsData;
    }

    public String getSectorName() {
        return sectorName;
    }

    public boolean isFirstJoin() {
        return firstJoin;
    }

    public long getLastSectorTransfer() {
        return lastSectorTransfer;
    }

    public long getLastTransferTimestamp() {
        return lastTransferTimestamp;
    }

    public boolean isTeleportingToSector() {
        return teleportingToSector;
    }

    public int getFoodLevel() {
        return foodLevel;
    }

    public int getExperience() {
        return experience;
    }

    public int getExperienceLevel() {
        return experienceLevel;
    }

    public int getFireTicks() {
        return fireTicks;
    }

    public boolean isAllowFlight() {
        return allowFlight;
    }

    public boolean isFlying() {
        return flying;
    }

    public String getPlayerGameMode() {
        return playerGameMode;
    }

    public String getPlayerInventoryData() {
        return playerInventoryData;
    }

    public String getPlayerEnderChestData() {
        return playerEnderChestData;
    }

    public String getPlayerEffectsData() {
        return playerEffectsData;
    }
}
