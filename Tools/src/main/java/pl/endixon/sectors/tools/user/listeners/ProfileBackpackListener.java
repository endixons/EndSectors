package pl.endixon.sectors.tools.user.listeners;

import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import pl.endixon.sectors.tools.backpack.repository.BackpackRepository;
import pl.endixon.sectors.tools.user.profile.cache.ProfileBackpackCache;
import pl.endixon.sectors.tools.user.profile.player.PlayerBackpackProfile;

import java.util.UUID;

@RequiredArgsConstructor
public class ProfileBackpackListener implements Listener {

    private final BackpackRepository repository;

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerBackpackProfile backpack = repository.find(player.getUniqueId()).orElseGet(() -> repository.create(player.getUniqueId(), player.getName()));
        ProfileBackpackCache.put(backpack);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        PlayerBackpackProfile backpack = ProfileBackpackCache.get(uuid);

        if (backpack != null) {
            repository.save(backpack);
            ProfileBackpackCache.remove(uuid);
        }
    }
}