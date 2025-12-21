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

package pl.endixon.sectors.paper.util;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;

public class HeadFactory {

    public static ItemStack generateHead(String base64Texture) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID());

        try {
            String decodedJson = new String(Base64.getDecoder().decode(base64Texture), StandardCharsets.UTF_8);
            String search = "\"url\":\"";
            int startIndex = decodedJson.indexOf(search) + search.length();
            int endIndex = decodedJson.indexOf("\"", startIndex);

            if (startIndex >= search.length() && endIndex > startIndex) {
                String skinUrl = decodedJson.substring(startIndex, endIndex);
                profile.getTextures().setSkin(new URL(skinUrl));
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        meta.setOwnerProfile(profile);
        skull.setItemMeta(meta);
        return skull;
    }

    public static ItemStack pickOnlineOfflineHead(boolean online) {
        String onlineBase64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGEyZjljNzYxZmMxMzFkYmViZDA3M2IwYjFkZDdkMWJhZWExOTFjZTlkMzNjNDljM2FjYTk0NDhiMWI2YjY4NCJ9fX0=";
        String offlineBase64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWIwZTA3NjMyMmZjOWFmNzk1OTJlYjg1MmNhOGM3YzQ1YmIyYzNjZWFiYzNjMGU4YTZhMWUwNGI0Y2UzZDM0YiJ9fX0=";
        return generateHead(online ? onlineBase64 : offlineBase64);
    }
}
