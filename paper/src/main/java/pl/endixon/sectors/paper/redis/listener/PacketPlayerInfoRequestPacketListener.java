package pl.endixon.sectors.paper.redis.listener;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import pl.endixon.sectors.common.redis.RedisPacketListener;
import pl.endixon.sectors.paper.PaperSector;
import pl.endixon.sectors.paper.redis.packet.PacketPlayerInfoRequest;
import pl.endixon.sectors.paper.sector.Sector;
import pl.endixon.sectors.paper.user.UserManager;
import pl.endixon.sectors.paper.user.UserMongo;
import pl.endixon.sectors.paper.util.PlayerDataSerializer;

public class PacketPlayerInfoRequestPacketListener
        extends RedisPacketListener<PacketPlayerInfoRequest> {

    private final PaperSector paperSector;

    public PacketPlayerInfoRequestPacketListener(PaperSector paperSector) {
        super(PacketPlayerInfoRequest.class);
        this.paperSector = paperSector;
    }

    @Override
    public void handle(PacketPlayerInfoRequest dto) {
        UserMongo user = UserManager.getUsers().get(dto.getName().toLowerCase());
        if (user == null) return;

        Player player = user.getPlayer();
        if (player != null) {
            Bukkit.getScheduler().runTask(paperSector, () -> {
                player.setGameMode(GameMode.valueOf(dto.getPlayerGameMode()));
                player.setFoodLevel(dto.getFoodLevel());
                player.setTotalExperience(dto.getExperience());
                player.setLevel(dto.getExperienceLevel());
                player.setFireTicks(dto.getFireTicks());
                player.setAllowFlight(dto.isAllowFlight());
                player.setFlying(dto.isFlying());
                player.getInventory().setContents(PlayerDataSerializer.deserializeItemStacksFromBase64(dto.getPlayerInventoryData()));
                player.getEnderChest().setContents(PlayerDataSerializer.deserializeItemStacksFromBase64(dto.getPlayerEnderChestData()));
                player.getActivePotionEffects().forEach(e -> player.removePotionEffect(e.getType()));
                PlayerDataSerializer.deserializeEffects(dto.getPlayerEffectsData()).forEach(player::addPotionEffect);
                Sector currentSector = PaperSector.getInstance().getSectorManager().getCurrentSector();
                user.updatePlayerData(player, currentSector);
            });
        }
    }
    }
