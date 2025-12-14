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


    package pl.endixon.sectors.paper.util;

    import lombok.NonNull;
    import org.bukkit.entity.Player;
    import org.bukkit.inventory.ItemStack;
    import org.bukkit.potion.PotionEffect;
    import com.google.gson.Gson;
    import com.google.gson.reflect.TypeToken;
    import org.bukkit.potion.PotionEffectType;
    import org.bukkit.util.io.BukkitObjectInputStream;
    import org.bukkit.util.io.BukkitObjectOutputStream;

    import java.io.ByteArrayInputStream;
    import java.io.ByteArrayOutputStream;
    import java.lang.reflect.Type;
    import java.util.ArrayList;
    import java.util.List;
    import java.util.Map;
    import java.util.Base64;

    public class PlayerDataSerializer {


        public static boolean isItem(final @NonNull ItemStack is) {
            return is.hasItemMeta() && is.getItemMeta() != null && is.getItemMeta().hasDisplayName();
        }


        //itemStackArrayToBase64
        @lombok.SneakyThrows
        public static String serializeItemStacksToBase64(final ItemStack[] items) {

            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            final BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeInt(items.length);
            for (ItemStack item : items) {
                dataOutput.writeObject(item);
            }
            dataOutput.close();
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());

        }

        @lombok.SneakyThrows
        public static ItemStack[] deserializeItemStacksFromBase64(final String data) {
            if (data == null || data.isEmpty()) {
                return new ItemStack[0];
            }

            byte[] decoded;
            try {
                decoded = Base64.getDecoder().decode(data);
            } catch (IllegalArgumentException ex) {
                Logger.info("Failed to decode Base64: " + data + " (" + ex.getMessage() + ")");
                return new ItemStack[0];
            }

            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(decoded);
                 BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {

                ItemStack[] items = new ItemStack[dataInput.readInt()];
                for (int i = 0; i < items.length; ++i) {
                    items[i] = (ItemStack) dataInput.readObject();
                }
                return items;

            } catch (Exception e) {
                Logger.info("Failed to read ItemStacks from Base64: " + e.getMessage());
                return new ItemStack[0];
            }
        }


        public static String serializeEffects(@NonNull Player player) {
            List<PotionEffect> effects = new ArrayList<>(player.getActivePotionEffects());
            List<Map<String, Object>> effectList = new ArrayList<>();


            for (PotionEffect effect : effects) {
                Map<String, Object> effectMap = Map.of(
                        "type", effect.getType().getName(),
                        "amplifier", effect.getAmplifier(),
                        "duration", effect.getDuration()
                );
                effectList.add(effectMap);
            }
            Gson gson = new Gson();
            String json = gson.toJson(effectList);
            return Base64.getEncoder().encodeToString(json.getBytes());
        }


        public static List<PotionEffect> deserializeEffects(String base64Data) {
            if (base64Data == null || base64Data.isEmpty()) {
                return new ArrayList<>();
            }

            String jsonData = new String(Base64.getDecoder().decode(base64Data));
            Gson gson = new Gson();
            Type listType = new TypeToken<List<Map<String, Object>>>() {
            }.getType();
            List<Map<String, Object>> effectList = gson.fromJson(jsonData, listType);

            if (effectList == null) {
                return new ArrayList<>();
            }

            List<PotionEffect> effects = new ArrayList<>();
            for (Map<String, Object> effectMap : effectList) {
                String typeName = (String) effectMap.get("type");
                int amplifier = ((Double) effectMap.get("amplifier")).intValue();
                int duration = ((Double) effectMap.get("duration")).intValue();
                PotionEffectType type = PotionEffectType.getByName(typeName);
                if (type != null) {
                    effects.add(new PotionEffect(type, duration, amplifier));
                }
            }
            return effects;
        }
    }

