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

package pl.endixon.sectors.proxy.manager;

import java.util.HashSet;
import java.util.Set;

public class TeleportationManager {

    private final Set<String> pending = new HashSet<>();

    public void addPending(String playerName) {
        pending.add(playerName);
    }

    public void removePending(String playerName) {
        pending.remove(playerName);
    }

    public boolean isPending(String playerName) {
        return pending.contains(playerName);
    }
}
