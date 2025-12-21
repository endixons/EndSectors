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
import pl.endixon.sectors.tools.manager.CombatManager;
import pl.endixon.sectors.tools.manager.MongoManager;
import pl.endixon.sectors.tools.user.listeners.CombatListener;
import pl.endixon.sectors.tools.user.listeners.InventoryInternactListener;
import pl.endixon.sectors.tools.user.listeners.ProfileListener;
import pl.endixon.sectors.tools.user.profile.PlayerProfile;
import pl.endixon.sectors.tools.user.profile.PlayerProfileRepository;
import pl.endixon.sectors.tools.utils.LoggerUtil;

@Getter
public class Main extends JavaPlugin {

    private static Main instance;
    private CombatManager combatManager;
    private SectorsAPI sectorsAPI;
    private MongoManager mongoService;
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
        combatManager = new CombatManager(this);
        registerListeners();
        LoggerUtil.info("EndSectors-Tools wystartował");
    }

    @Override
    public void onDisable() {
        shutdownMongo();
    }

    private void initMongo() {
        mongoService = new MongoManager();
        mongoService.connect("mongodb://localhost:27017", "endsectors");
    }

    private void initRepositories() {
        LoggerUtil.info("Inicjalizacja repozytoriów MongoDB...");
        try {
            MongoCollection<PlayerProfile> collection = mongoService.getDatabase().getCollection("players", PlayerProfile.class);
            repository = new PlayerProfileRepository(collection);
            long loaded = collection.countDocuments();
            LoggerUtil.info("Repozytorium UserProfile załadowane (kolekcja: players, rekordy: " + loaded + ")");
        } catch (Exception e) {
            LoggerUtil.info("Błąd inicjalizacji repozytorium UserProfile: " + e.getMessage());
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
            LoggerUtil.info("Błąd przy inicjalizacji SectorsAPI: " + e.getMessage());
            return false;
        }
    }

    private void registerListeners() {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new ProfileListener(repository), this);
        pm.registerEvents(new CombatListener(combatManager, sectorsAPI), this);
        pm.registerEvents(new InventoryInternactListener(), this);
    }

    private void registerCommands() {
        registerCommand("randomtp", new RandomTPCommand(sectorsAPI));
        registerCommand("spawn", new SpawnCommand(sectorsAPI));
        registerCommand("home", new HomeCommand(sectorsAPI));
    }

    private void registerCommand(String name, Object executor) {
        PluginCommand command = getCommand(name);
        if (command == null) {
            LoggerUtil.info("Komenda /" + name + " NIE jest w plugin.yml");
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
        LoggerUtil.info(reason);
        Bukkit.getPluginManager().disablePlugin(this);
    }

    public static Main getInstance() {
        return instance;
    }
}
