package pl.endixon.sectors.paper.redis.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import pl.endixon.sectors.common.redis.RedisPacketListener;
import pl.endixon.sectors.paper.PaperSector;
import pl.endixon.sectors.paper.redis.packet.PacketPlayerInfoRequest;
import pl.endixon.sectors.paper.user.UserManager;

public class PacketPlayerInfoRequestPacketListener
        extends RedisPacketListener<PacketPlayerInfoRequest> {

    private final PaperSector paperSector;

    public PacketPlayerInfoRequestPacketListener(PaperSector paperSector) {
        super(PacketPlayerInfoRequest.class);
        this.paperSector = paperSector;
    }

    @Override
    public void handle(PacketPlayerInfoRequest dto) {
        UserManager.getUser(dto.getName()).thenAccept(user -> {
            if (user == null) return;
            user.setSectorName(dto.getSectorName());
            user.setFirstJoin(dto.isFirstJoin());
            user.setLastSectorTransfer(dto.getLastSectorTransfer() != 0);
            user.setLastTransferTimestamp(dto.getLastTransferTimestamp());
            user.setTeleportingToSector(dto.isTeleportingToSector());
            user.setFoodLevel(dto.getFoodLevel());
            user.setExperience(dto.getExperience());
            user.setExperienceLevel(dto.getExperienceLevel());
            user.setFireTicks(dto.getFireTicks());
            user.setAllowFlight(dto.isAllowFlight());
            user.setFlying(dto.isFlying());
            user.setPlayerGameMode(dto.getPlayerGameMode());
            user.setX(dto.getX());
            user.setY(dto.getY());
            user.setZ(dto.getZ());
            user.setYaw(dto.getYaw());
            user.setPitch(dto.getPitch());
            user.setPlayerInventoryData(dto.getPlayerInventoryData());
            user.setPlayerEnderChestData(dto.getPlayerEnderChestData());
            user.setPlayerEffectsData(dto.getPlayerEffectsData());
            Player player = Bukkit.getPlayer(user.getName());
            if (player != null) {
                Bukkit.getScheduler().runTask(paperSector, user::applyPlayerData);
            }
        });
    }
}
