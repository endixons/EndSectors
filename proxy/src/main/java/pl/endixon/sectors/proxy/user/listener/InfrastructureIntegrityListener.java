/*
 *
 *  EndSectors  Non-Commercial License
 *  (c) 2025 Endixon
 *
 *  Permission is granted to use, copy, and
 *  modify this software **only** for personal
 *  or educational purposes.
 *
 *   Commercial use, redistribution, claiming
 *  this work as your own, or copying code
 *  without explicit permission is strictly
 *  prohibited.
 *
 *  Visit https://github.com/Endixon/EndSectors
 *  for more info.
 *
 */

package pl.endixon.sectors.proxy.user.listener;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import pl.endixon.sectors.proxy.VelocitySectorPlugin;
import pl.endixon.sectors.proxy.util.LoggerUtil;
import pl.endixon.sectors.proxy.util.ProxyMessages;


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
        event.getPlayer().disconnect(ProxyMessages.EMERGENCY_KICK.get());
    }
}