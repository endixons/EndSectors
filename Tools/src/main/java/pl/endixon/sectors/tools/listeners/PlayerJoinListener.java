    package pl.endixon.sectors.tools.listeners;

    import lombok.RequiredArgsConstructor;
    import org.bukkit.entity.Player;
    import org.bukkit.event.EventHandler;
    import org.bukkit.event.Listener;
    import org.bukkit.event.player.PlayerJoinEvent;
    import pl.endixon.sectors.tools.service.users.ProfileCache;
    import pl.endixon.sectors.tools.service.users.PlayerProfileRepository;
    import pl.endixon.sectors.tools.service.users.PlayerProfile;

    @RequiredArgsConstructor
    public class PlayerJoinListener implements Listener {

        private final PlayerProfileRepository repository;

        @EventHandler
        public void onJoin(PlayerJoinEvent event) {
            Player player = event.getPlayer();

            PlayerProfile profile = repository.find(player.getUniqueId())
                    .orElseGet(() ->
                            repository.create(player.getUniqueId(), player.getName())
                    );

            ProfileCache.put(profile);
        }
    }
