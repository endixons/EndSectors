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

package pl.endixon.sectors.paper.nats.listener;

import pl.endixon.sectors.common.Common;
import pl.endixon.sectors.common.packet.PacketChannel;
import pl.endixon.sectors.common.packet.PacketListener;
import pl.endixon.sectors.common.packet.object.PacketUserCheck;
import pl.endixon.sectors.common.sector.SectorType;
import pl.endixon.sectors.paper.PaperSector;
import pl.endixon.sectors.paper.sector.Sector;
import pl.endixon.sectors.paper.user.profile.UserProfile;
import pl.endixon.sectors.paper.user.profile.UserProfileRepository;

import java.util.Optional;

public class PacketUserCheckListener implements PacketListener<PacketUserCheck> {

    @Override
    public void handle(PacketUserCheck packet) {
        final Sector currentSector = PaperSector.getInstance().getSectorManager().getCurrentSector();
        String username = packet.getUsername().toLowerCase();
        Optional<UserProfile> user = UserProfileRepository.getUser(username);
        boolean exists = user.isPresent();
        String sector = user.map(UserProfile::getSectorName).orElse(null);
        PacketUserCheck response = new PacketUserCheck(username, exists, sector);

        if (currentSector == null || currentSector.getType() != SectorType.QUEUE) {
            return;
        }
        Common.getInstance().getNatsManager().publish(PacketChannel.USER_CHECK_RESPONSE.getSubject(), response);
    }
}
