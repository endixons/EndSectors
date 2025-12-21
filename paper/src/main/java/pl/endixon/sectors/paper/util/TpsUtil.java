/*
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
 */

package pl.endixon.sectors.paper.util;

import java.lang.reflect.Field;
import org.bukkit.Bukkit;

public class TpsUtil {

    public static double getTPS() {
        try {
            Object minecraftServer = Bukkit.getServer().getClass().getMethod("getServer").invoke(Bukkit.getServer());

            Field recentTpsField = minecraftServer.getClass().getField("recentTps");
            double[] recentTps = (double[]) recentTpsField.get(minecraftServer);

            return recentTps[0];
        } catch (Exception e) {
            e.printStackTrace();
            return 20.0;
        }
    }
}
