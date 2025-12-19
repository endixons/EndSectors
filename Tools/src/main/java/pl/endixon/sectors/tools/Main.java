package pl.endixon.sectors.tools;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import pl.endixon.sectors.paper.SectorsAPI;
import pl.endixon.sectors.tools.command.RandomTPCommand;
import pl.endixon.sectors.tools.command.SpawnCommand;
import pl.endixon.sectors.tools.utils.Logger;

public class Main extends JavaPlugin {

    @Getter
    private static Main instance;

    @Getter
    private SectorsAPI sectorsAPI;

    @Override
    public void onEnable() {
        instance = this;

        if (!initSectorsAPI()) {
            shutdown("Brak EndSectors – plugin wyłączony");
            return;
        }

        registerCommands();

        Logger.info("Plugin wystartował");
    }

    private boolean initSectorsAPI() {
        var plugin = Bukkit.getPluginManager().getPlugin("EndSectors");

        if (plugin == null || !plugin.isEnabled()) {
            return false;
        }

        try {
            this.sectorsAPI = SectorsAPI.getInstance();
            if (this.sectorsAPI == null) {
                Logger.info("SectorsAPI nie jest dostępne!");
                return false;
            }
        } catch (Exception e) {
            Logger.info("Błąd przy inicjalizacji SectorsAPI: " + e.getMessage());
            return false;
        }

        return true;
    }

    private void registerCommands() {
        // teraz RandomTPCommand i SpawnCommand korzystają bezpośrednio z SectorsAPI
        registerCommand("randomtp", new RandomTPCommand());
        registerCommand("spawn", new SpawnCommand());
    }

    private void registerCommand(String name, Object executor) {
        PluginCommand command = getCommand(name);

        if (command == null) {
            Logger.info("Komenda /" + name + " NIE jest w plugin.yml");
            return;
        }

        command.setExecutor((CommandExecutor) executor);
    }

    private void shutdown(String reason) {
        Logger.info(reason);
        Bukkit.getPluginManager().disablePlugin(this);
    }
}
