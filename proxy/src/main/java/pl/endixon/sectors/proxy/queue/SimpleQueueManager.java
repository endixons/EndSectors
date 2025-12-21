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

package pl.endixon.sectors.proxy.queue;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleQueueManager<T, ID> {

    private final Map<T, ID> map = new ConcurrentHashMap<>();

    public Map<T, ID> getMap() {
        return this.map;
    }
}
