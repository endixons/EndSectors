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

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import pl.endixon.sectors.common.Common;

public final class PlayerConnectionListener {

    @Subscribe
    public void onPlayerLogin(LoginEvent event) {
        Common.getInstance().getRedisManager().addOnlinePlayer(event.getPlayer().getUsername());
    }

    @Subscribe
    public void onPlayerDisconnect(DisconnectEvent event) {
        Common.getInstance().getRedisManager().removeOnlinePlayer(event.getPlayer().getUsername());
    }
}
