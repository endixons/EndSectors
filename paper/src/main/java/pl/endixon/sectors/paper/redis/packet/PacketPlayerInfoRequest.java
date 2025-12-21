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

package pl.endixon.sectors.paper.redis.packet;

import lombok.Getter;
import pl.endixon.sectors.common.packet.Packet;
import pl.endixon.sectors.paper.user.profile.UserProfile;

@Getter
public class PacketPlayerInfoRequest implements Packet {

    private final String name;
    private final String sectorName;
    private final boolean firstJoin;
    private final long lastSectorTransfer;
    private final long lastTransferTimestamp;
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

    public PacketPlayerInfoRequest(UserProfile user) {
        this(user.getName(), user.getSectorName(), user.isFirstJoin(), user.getLastSectorTransfer(), user.getLastTransferTimestamp(), user.getFoodLevel(), user.getExperience(), user.getExperienceLevel(), user.getFireTicks(), user.isAllowFlight(), user.isFlying(), user.getPlayerGameMode(), user.getPlayerInventoryData(), user.getPlayerEnderChestData(), user.getPlayerEffectsData());
    }

    public PacketPlayerInfoRequest(String name, String sectorName, boolean firstJoin, long lastSectorTransfer, long lastTransferTimestamp, int foodLevel, int experience, int experienceLevel, int fireTicks, boolean allowFlight, boolean flying, String playerGameMode, String playerInventoryData, String playerEnderChestData, String playerEffectsData) {
        this.name = name;
        this.sectorName = sectorName;
        this.firstJoin = firstJoin;
        this.lastSectorTransfer = lastSectorTransfer;
        this.lastTransferTimestamp = lastTransferTimestamp;
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
}
