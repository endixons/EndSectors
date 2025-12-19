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
import pl.endixon.sectors.tools.command.SetHomeCommand;
import pl.endixon.sectors.tools.command.SpawnCommand;
import pl.endixon.sectors.tools.listeners.PlayerJoinListener;
import pl.endixon.sectors.tools.listeners.PlayerQuitListener;
import pl.endixon.sectors.tools.service.MongoService;
import pl.endixon.sectors.tools.service.Repository.PlayerProfileRepository;
import pl.endixon.sectors.tools.service.users.PlayerProfile;
import pl.endixon.sectors.tools.utils.Logger;

@Getter
public class Main extends JavaPlugin {

    private static Main instance;

    private SectorsAPI sectorsAPI;
    private MongoService mongoService;
    private PlayerProfileRepository repository;

    @Override
    public void onEnable() {
        instance = this;
        if (!initSectorsAPI()) {
            shutdown("Brak EndSectors – plugin wyłączony");
            return;
        }
        initMongo();
        initRepositories();
        registerCommands();
        registerListeners();
        Logger.info("EndSectors-Tools wystartował");
    }

    @Override
    public void onDisable() {
        shutdownMongo();
    }


    private void initMongo() {
        mongoService = new MongoService();
        mongoService.connect(
                "mongodb://localhost:27017",
                "endsectors"
        );
    }

    private void initRepositories() {
        Logger.info("Inicjalizacja repozytoriów MongoDB...");
        try {
            MongoCollection<PlayerProfile> collection =
                    mongoService.getDatabase().getCollection(
                            "players",
                            PlayerProfile.class
                    );
            repository = new PlayerProfileRepository(collection);
            long loaded = collection.countDocuments();
            Logger.info("Repozytorium PlayerProfile załadowane (kolekcja: players, rekordy: " + loaded + ")");
        } catch (Exception e) {
            Logger.info("Błąd inicjalizacji repozytorium PlayerProfile: " + e.getMessage());
            e.printStackTrace();
            shutdown("Nie można zainicjalizować repozytoriów MongoDB");
        }
    }




    private boolean initSectorsAPI() {
        var plugin = Bukkit.getPluginManager().getPlugin("EndSectors");
        if (plugin == null || !plugin.isEnabled()) {
            return false;
        }
        try {
            sectorsAPI = SectorsAPI.getInstance();
            return sectorsAPI != null;
        } catch (Exception e) {
            Logger.info("Błąd przy inicjalizacji SectorsAPI: " + e.getMessage());
            return false;
        }
    }

    private void registerListeners() {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new PlayerJoinListener(repository), this);
        pm.registerEvents(new PlayerQuitListener(repository), this);
    }

    private void registerCommands() {
        registerCommand("randomtp", new RandomTPCommand());
        registerCommand("spawn", new SpawnCommand());
        registerCommand("home", new HomeCommand());
        registerCommand("sethome", new SetHomeCommand());

    }

    private void registerCommand(String name, Object executor) {
        PluginCommand command = getCommand(name);
        if (command == null) {
            Logger.info("Komenda /" + name + " NIE jest w plugin.yml");
            return;
        }
        command.setExecutor((CommandExecutor) executor);
    }


    private void shutdownMongo() {
        if (mongoService != null) {
            mongoService.disconnect();
        }
    }

    private void shutdown(String reason) {
        Logger.info(reason);
        Bukkit.getPluginManager().disablePlugin(this);
    }

    public static Main getInstance() {
        return instance;
    }
}
