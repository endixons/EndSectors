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

package pl.endixon.sectors.paper.redis.listener;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import pl.endixon.sectors.common.packet.PacketListener;
import pl.endixon.sectors.paper.PaperSector;
import pl.endixon.sectors.paper.redis.packet.PacketPlayerInfoRequest;
import pl.endixon.sectors.paper.sector.Sector;
import pl.endixon.sectors.paper.user.profile.UserProfileRepository;
import pl.endixon.sectors.paper.util.LoggerUtil;
import pl.endixon.sectors.paper.util.PlayerDataSerializerUtil;

public class PacketPlayerInfoRequestPacketListener implements PacketListener<PacketPlayerInfoRequest> {

    @Override
    public void handle(PacketPlayerInfoRequest dto) {
        UserProfileRepository.getUserAsync(dto.getName()).thenAccept(optionalUser -> optionalUser.ifPresent(user -> {
            Player player = user.getPlayer();

            if (player == null) {
                LoggerUtil.info(() -> String.format("UserProfile exists but player object is null for '%s'", dto.getName()));
                return;
            }

            Sector currentSector = PaperSector.getInstance().getSectorManager().getCurrentSector();

            Bukkit.getScheduler().runTask(PaperSector.getInstance(), () -> {
                player.setGameMode(GameMode.valueOf(dto.getPlayerGameMode()));
                player.setFoodLevel(dto.getFoodLevel());
                player.setTotalExperience(dto.getExperience());
                player.setLevel(dto.getExperienceLevel());
                player.setFireTicks(dto.getFireTicks());
                player.setAllowFlight(dto.isAllowFlight());
                player.setFlying(dto.isFlying());
                player.getInventory().setContents(PlayerDataSerializerUtil.deserializeItemStacksFromBase64(dto.getPlayerInventoryData()));
                player.getEnderChest().setContents(PlayerDataSerializerUtil.deserializeItemStacksFromBase64(dto.getPlayerEnderChestData()));
                player.getActivePotionEffects().forEach(e -> player.removePotionEffect(e.getType()));
                PlayerDataSerializerUtil.deserializeEffects(dto.getPlayerEffectsData()).forEach(player::addPotionEffect);

                user.updateAndSave(player, currentSector);
            });
        }));
    }
}
