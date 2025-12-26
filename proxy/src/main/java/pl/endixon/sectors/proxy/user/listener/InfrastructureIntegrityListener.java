package pl.endixon.sectors.proxy.user.listener;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import pl.endixon.sectors.proxy.VelocitySectorPlugin;
import pl.endixon.sectors.proxy.util.LoggerUtil;


public final class InfrastructureIntegrityListener {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private final VelocitySectorPlugin plugin = VelocitySectorPlugin.getInstance();

    @Subscribe(order = PostOrder.FIRST)
    public void onPlayerConnect(final ServerPreConnectEvent event) {
        if (this.plugin.getHeartbeatHook() == null || !this.plugin.getHeartbeatHook().isCommonReady()) {
            LoggerUtil.warn("[GUARD] Denied access for " + event.getPlayer().getUsername() + " - Infrastructure Lockdown.");
            this.handleEmergencyPing(event);
        }
    }


    private void handleEmergencyPing(final ServerPreConnectEvent event) {
        event.setResult(ServerPreConnectEvent.ServerResult.denied());
        final Component kickMessage = MINI_MESSAGE.deserialize(
                "<bold><gradient:#ff4b2b:#ff416c>ENDSECTORS</gradient></bold><br><br>" +
                        "<gray>Obecnie trwają <gradient:#ffe259:#ffa751>PRACE KONSERWACYJNE</gradient>.<br>" +
                        "<gray>Zapraszamy ponownie za kilka minut!<br><br>" +
                        "<dark_gray>Status: <red>Tryb Optymalizacji"
        );
        event.getPlayer().disconnect(kickMessage);
    }
}