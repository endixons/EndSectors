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

package pl.endixon.sectors.tools.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import pl.endixon.sectors.paper.PaperSector;
import java.io.*;
import java.nio.charset.StandardCharsets;


public class ConfigLoader {


    public String mongoUri = "mongodb://user:password@127.0.0.1:27017/admin?authSource=admin";
    public String mongoDatabase = "endsectors";
    public String redisHost = "127.0.0.1";
    public int redisPort = 6379;
    public String redisPassword = "password";
    public String natsUrl = "nats://user:password@127.0.0.1:4222";
    public String natsConnectionName = "tools_spawn_1";

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static ConfigLoader load(File dataFolder) {
        try {
            if (!dataFolder.exists() && !dataFolder.mkdirs()) {
                PaperSector.getInstance().getLogger().warning("Failed to create configuration directory: " + dataFolder.getAbsolutePath());
            }

            File file = new File(dataFolder, "config.json");

            if (file.exists()) {
                try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
                    return gson.fromJson(reader, ConfigLoader.class);
                } catch (Exception e) {
                    PaperSector.getInstance().getLogger().warning("Error while parsing config.json, rolling back to defaults: " + e.getMessage());
                    return defaultConfig();
                }
            } else {
                ConfigLoader defaultConfig = defaultConfig();
                try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
                    gson.toJson(defaultConfig, writer);
                    PaperSector.getInstance().getLogger().info("Default config.json has been generated successfully.");
                } catch (IOException e) {
                    PaperSector.getInstance().getLogger().warning("Failed to save default config.json: " + e.getMessage());
                }
                return defaultConfig;
            }
        } catch (Exception e) {
            PaperSector.getInstance().getLogger().severe("Unexpected critical error during configuration load: " + e.getMessage());
            return defaultConfig();
        }
    }

    private static ConfigLoader defaultConfig() {
        ConfigLoader config = new ConfigLoader();
        config.mongoUri = "mongodb://localhost:27017";
        config.mongoDatabase  = "endsectors";
        return config;
    }
}
