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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

public class PlayerDataSerializerUtil {

    public static boolean isItem(final @NonNull ItemStack is) {
        return is.hasItemMeta() && is.getItemMeta() != null && is.getItemMeta().hasDisplayName();
    }

    public static String serializeItemStacksToBase64(final ItemStack[] items) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream)) {

            dataOutput.writeInt(items.length);
            for (ItemStack item : items) {
                dataOutput.writeObject(item);
            }
            dataOutput.flush();

            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            LoggerUtil.info("Failed to serialize ItemStacks: " + e.getMessage());
            return "";
        }
    }

    @lombok.SneakyThrows
    public static ItemStack[] deserializeItemStacksFromBase64(final String data) {
        if (data == null || data.isEmpty())
            return new ItemStack[0];

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(data)); BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {

            ItemStack[] items = new ItemStack[dataInput.readInt()];
            for (int i = 0; i < items.length; i++) {
                items[i] = (ItemStack) dataInput.readObject();
            }
            return items;

        } catch (Exception e) {
            LoggerUtil.info("Failed to deserialize ItemStacks: " + e.getMessage());
            return new ItemStack[0];
        }
    }

    public static String serializeEffects(@NonNull Player player) {
        List<PotionEffect> effects = new ArrayList<>(player.getActivePotionEffects());
        List<Map<String, Object>> effectList = new ArrayList<>();

        for (PotionEffect effect : effects) {
            Map<String, Object> effectMap = Map.of("type", effect.getType().getName(), "amplifier", effect.getAmplifier(), "duration", effect.getDuration());
            effectList.add(effectMap);
        }

        Gson gson = new Gson();
        String json = gson.toJson(effectList);
        return Base64.getEncoder().encodeToString(json.getBytes());
    }

    public static List<PotionEffect> deserializeEffects(String base64Data) {
        if (base64Data == null || base64Data.isEmpty())
            return List.of();

        try {
            String jsonData = new String(Base64.getDecoder().decode(base64Data));
            Gson gson = new Gson();
            Type listType = new TypeToken<List<Map<String, Object>>>() {
            }.getType();
            List<Map<String, Object>> effectList = gson.fromJson(jsonData, listType);

            if (effectList == null)
                return List.of();

            List<PotionEffect> effects = new ArrayList<>(effectList.size());
            for (Map<String, Object> map : effectList) {
                String typeName = (String) map.get("type");
                int amplifier = ((Double) map.get("amplifier")).intValue();
                int duration = ((Double) map.get("duration")).intValue();

                PotionEffectType type = PotionEffectType.getByName(typeName);
                if (type != null) {
                    effects.add(new PotionEffect(type, duration, amplifier));
                }
            }

            return effects;

        } catch (Exception e) {
            LoggerUtil.info("Failed to deserialize potion effects: " + e.getMessage());
            return List.of();
        }
    }
}
