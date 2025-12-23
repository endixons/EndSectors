package pl.endixon.sectors.proxy.command;

import com.velocitypowered.api.command.SimpleCommand;
import net.kyori.adventure.text.minimessage.MiniMessage;
import pl.endixon.sectors.proxy.VelocitySectorPlugin;
import pl.endixon.sectors.proxy.util.Logger;

public class SectorsCommand implements SimpleCommand {

    private final VelocitySectorPlugin plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public SectorsCommand(VelocitySectorPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        if (!invocation.source().hasPermission("endsectors.admin")) {
            invocation.source().sendMessage(mm.deserialize("<red>Brak uprawnień."));
            return;
        }

        String[] args = invocation.arguments();
        if (args.length != 1 || !args[0].equalsIgnoreCase("reload")) {
            invocation.source().sendMessage(mm.deserialize("<red>Użycie: <gold>/sectors reload"));
            return;
        }


        this.plugin.getSectorManager().clear();
        this.plugin.loadSectors();

        invocation.source().sendMessage(mm.deserialize("<#4ade80><b>SYSTEM</b> <#888888>» <#f2f2f2>Sektory zostały pomyślnie przeładowane z <gold>config.json<#f2f2f2>."));

        String senderName = (invocation.source() instanceof com.velocitypowered.api.proxy.Player player)
                ? player.getUsername()
                : "Console";

        Logger.info("Configuration reloaded by " + senderName);
    }
}