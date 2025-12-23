package pl.endixon.sectors.tools;

import com.mongodb.client.MongoCollection;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import pl.endixon.sectors.paper.SectorsAPI;
import pl.endixon.sectors.tools.command.HomeCommand;
import pl.endixon.sectors.tools.command.RandomTPCommand;
import pl.endixon.sectors.tools.command.SpawnCommand;
import pl.endixon.sectors.tools.config.MessageLoader;
import pl.endixon.sectors.tools.manager.CombatManager;
import pl.endixon.sectors.tools.manager.MongoManager;
import pl.endixon.sectors.tools.user.listeners.CombatListener;
import pl.endixon.sectors.tools.user.listeners.InventoryInternactListener;
import pl.endixon.sectors.tools.user.listeners.ProfileListener;
import pl.endixon.sectors.tools.user.profile.PlayerProfile;
import pl.endixon.sectors.tools.user.profile.PlayerProfileRepository;
import pl.endixon.sectors.tools.utils.LoggerUtil;

import java.io.File;

@Getter
public class Main extends JavaPlugin {

    private static Main instance;
    private CombatManager combatManager;
    private SectorsAPI sectorsAPI;
    private MongoManager mongoService;
    private PlayerProfileRepository repository;
    private MessageLoader messageLoader;

    @Override
    public void onEnable() {
        instance = this;
        this.loadConfigs();
        if (!this.initSectorsAPI()) {
            this.shutdown("EndSectors API dependency not found or disabled! Shutting down.");
            return;
        }
        this.initMongo();
        this.initRepositories();
        this.combatManager = new CombatManager(this);
        this.registerCommands();
        this.registerListeners();
        LoggerUtil.info("EndSectors-Tools successfully enabled and synchronized.");
    }

    @Override
    public void onDisable() {
        this.shutdownMongo();
        LoggerUtil.info("EndSectors-Tools disabled. Resources released.");
    }

    private void loadConfigs() {
        LoggerUtil.info("Loading JSON configuration files...");
        File dataFolder = this.getDataFolder();
        this.messageLoader = MessageLoader.load(dataFolder);
        LoggerUtil.info("Externalized messages loaded successfully.");
    }

    private void initMongo() {
        LoggerUtil.info("Connecting to MongoDB cluster...");
        this.mongoService = new MongoManager();
        this.mongoService.connect("mongodb://localhost:27017", "endsectors");
    }

    private void initRepositories() {
        LoggerUtil.info("Initializing MongoDB repositories...");
        try {
            MongoCollection<PlayerProfile> collection = this.mongoService.getDatabase()
                    .getCollection("players", PlayerProfile.class);

            this.repository = new PlayerProfileRepository(collection);
            long recordCount = collection.countDocuments();

            LoggerUtil.info("PlayerProfile repository initialized. (Collection: players, Records: " + recordCount + ")");
        } catch (Exception e) {
            LoggerUtil.info("Failed to initialize PlayerProfile repository: " + e.getMessage());
            e.printStackTrace();
            this.shutdown("Database repository failure – check MongoDB connection.");
        }
    }

    private boolean initSectorsAPI() {
        var plugin = Bukkit.getPluginManager().getPlugin("EndSectors");
        if (plugin == null || !plugin.isEnabled()) {
            LoggerUtil.info("EndSectors API is missing or disabled!");
            return false;
        }
        try {
            this.sectorsAPI = SectorsAPI.getInstance();
            return this.sectorsAPI != null;
        } catch (Exception e) {
            LoggerUtil.info("Critical error during SectorsAPI initialization: " + e.getMessage());
            return false;
        }
    }

    private void registerListeners() {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new ProfileListener(this.repository), this);
        pm.registerEvents(new CombatListener(this.combatManager, this.sectorsAPI), this);
        pm.registerEvents(new InventoryInternactListener(), this);
        LoggerUtil.info("System event listeners registered.");
    }

    private void registerCommands() {
        this.registerCommand("randomtp", new RandomTPCommand(this.sectorsAPI));
        this.registerCommand("spawn", new SpawnCommand(this.sectorsAPI));
        this.registerCommand("home", new HomeCommand(this.sectorsAPI));
        LoggerUtil.info("Command executors synchronized.");
    }

    private void registerCommand(String name, Object executor) {
        PluginCommand command = this.getCommand(name);
        if (command == null) {
            LoggerUtil.info("Command /" + name + " is missing from plugin.yml!");
            return;
        }
        command.setExecutor((CommandExecutor) executor);
    }

    private void shutdownMongo() {
        if (this.mongoService != null) {
            this.mongoService.disconnect();
            LoggerUtil.info("MongoDB connection closed.");
        }
    }

    private void shutdown(String reason) {
        LoggerUtil.info("Shutting down due to: " + reason);
        Bukkit.getPluginManager().disablePlugin(this);
    }

    public static Main getInstance() {
        return instance;
    }
}