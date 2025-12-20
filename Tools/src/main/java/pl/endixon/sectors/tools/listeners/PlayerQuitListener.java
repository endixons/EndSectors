package pl.endixon.sectors.tools.listeners;

import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import pl.endixon.sectors.tools.service.users.UserCache;
import pl.endixon.sectors.tools.service.Repository.PlayerProfileRepository;
import pl.endixon.sectors.tools.service.users.PlayerProfile;

import java.util.UUID;

@RequiredArgsConstructor
public class PlayerQuitListener implements Listener {

    private final PlayerProfileRepository repository;

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        PlayerProfile profile = UserCache.get(uuid);

        if (profile != null) {
            repository.save(profile);
            UserCache.remove(uuid);
        }
    }
}
