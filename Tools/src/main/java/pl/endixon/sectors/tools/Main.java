package pl.endixon.sectors.tools;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import pl.endixon.sectors.paper.PaperSector;
import pl.endixon.sectors.paper.sector.SectorManager;
import pl.endixon.sectors.tools.command.RandomTPCommand;
import pl.endixon.sectors.tools.command.SpawnCommand;
import pl.endixon.sectors.tools.utils.Logger;

public class Main extends JavaPlugin {

    @Getter
    private static Main instance;

    @Getter
    private SectorManager sectorManager;

    @Override
    public void onEnable() {
        instance = this;

        if (!initSectorManager()) {
            shutdown("Brak EndSectors – plugin wyłączony");
            return;
        }

        registerCommands();

        Logger.info("Plugin wystartował");
    }

    private boolean initSectorManager() {
        var plugin = Bukkit.getPluginManager().getPlugin("EndSectors");

        if (!(plugin instanceof PaperSector paperSector)) {
            return false;
        }

        this.sectorManager = paperSector.getSectorManager();
        return true;
    }

    private void registerCommands() {
        registerCommand("randomtp", new RandomTPCommand(sectorManager));
        registerCommand("spawn", new SpawnCommand(sectorManager));
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
