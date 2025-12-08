package pl.endixon.sectors.paper.redis.packet;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import pl.endixon.sectors.common.packet.Packet;
import pl.endixon.sectors.paper.user.UserMongo;

@Getter
public class PacketPlayerInfoRequest extends Packet {

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
    private final double x, y, z;
    private final float yaw, pitch;
    private final String playerGameMode;
    private final String playerInventoryData;
    private final String playerEnderChestData;
    private final String playerEffectsData;

    @JsonCreator
    public PacketPlayerInfoRequest(@JsonProperty("user") UserMongo user) {
        this.name = user.getName();
        this.sectorName = user.getSectorName();
        this.firstJoin = user.isFirstJoin();
        this.lastSectorTransfer = user.getLastSectorTransfer();
        this.lastTransferTimestamp = user.getLastTransferTimestamp();
        this.teleportingToSector = user.isTeleportingToSector();
        this.foodLevel = user.getFoodLevel();
        this.experience = user.getExperience();
        this.experienceLevel = user.getExperienceLevel();
        this.fireTicks = user.getFireTicks();
        this.allowFlight = user.isAllowFlight();
        this.flying = user.isFlying();
        this.x = user.getX();
        this.y = user.getY();
        this.z = user.getZ();
        this.yaw = user.getYaw();
        this.pitch = user.getPitch();
        this.playerGameMode = user.getPlayerGameMode();
        this.playerInventoryData = user.getPlayerInventoryData();
        this.playerEnderChestData = user.getPlayerEnderChestData();
        this.playerEffectsData = user.getPlayerEffectsData();
    }
}
