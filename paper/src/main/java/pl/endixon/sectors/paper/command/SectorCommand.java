package pl.endixon.sectors.paper.command;

import java.util.Arrays;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import pl.endixon.sectors.common.packet.PacketChannel;
import pl.endixon.sectors.common.packet.object.PacketConfigurationRequest;
import pl.endixon.sectors.common.redis.RedisManager;
import pl.endixon.sectors.paper.PaperSector;
import pl.endixon.sectors.paper.inventory.SectorShowWindow;
import pl.endixon.sectors.paper.manager.SectorManager;
import pl.endixon.sectors.paper.redis.packet.PacketExecuteCommand;
import pl.endixon.sectors.paper.user.profile.UserProfile;
import pl.endixon.sectors.paper.user.profile.UserProfileRepository;
import pl.endixon.sectors.paper.util.LoggerUtil;
import pl.endixon.sectors.paper.util.MessagesUtil;

public class SectorCommand implements CommandExecutor {

    private final PaperSector plugin;

    public SectorCommand(PaperSector plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!sender.hasPermission("endsectors.command.sector")) {
            sender.sendMessage(MessagesUtil.NO_PERMISSION.get());
            return true;
        }

        final SectorManager sm = plugin.getSectorManager();
        final RedisManager redis = plugin.getRedisManager();


        if (args.length == 0) {
            MessagesUtil.HELP_MENU.asLore().forEach(sender::sendMessage);
            return true;
        }

        final String sub = args[0].toLowerCase();
        switch (sub) {

            case "reload" -> {
                this.plugin.loadFiles();
                sender.sendMessage(MessagesUtil.RELOAD_SUCCESS.get());
            }

            case "border" -> {
                sender.sendMessage(MessagesUtil.BORDER_REFRESHED.get());
                this.plugin.getRedisManager().publish(
                        PacketChannel.PACKET_CONFIGURATION_REQUEST,
                        new PacketConfigurationRequest(sm.getCurrentSectorName())
                );
                LoggerUtil.info("Requesting new sector configuration from Proxy (triggered by " + sender.getName() + ")");
            }

            case "where" -> sender.sendMessage(MessagesUtil.CURRENT_SECTOR.get(
                    "{SECTOR}", sm.getCurrentSectorName()
            ));

            case "show" -> {
                if (sender instanceof Player player) {
                    new SectorShowWindow(player, sm).open();
                }
            }
            case "execute" -> {
                if (args.length < 2) {
                    sender.sendMessage(MessagesUtil.USAGE_EXECUTE.get());
                    return true;
                }
                String commandToSend = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                redis.publish(PacketChannel.PACKET_EXECUTE_COMMAND, new PacketExecuteCommand(commandToSend));
                sender.sendMessage(MessagesUtil.COMMAND_BROADCASTED.get());
            }

            case "isonline" -> {
                if (args.length < 2) {
                    sender.sendMessage(MessagesUtil.SPECIFY_NICKNAME.get("{SUB}", "isonline <nick>"));
                    return true;
                }
                String nick = args[1];
                sm.isPlayerOnline(nick, isOnline -> sender.sendMessage(MessagesUtil.PLAYER_ONLINE_STATUS.get(
                        "{NICK}", nick,
                        "{STATUS}", isOnline ? "<#4ade80>ONLINE" : "<#ef4444>OFFLINE"
                )));
            }

            case "who" -> sm.getOnlinePlayers(online -> sender.sendMessage(MessagesUtil.GLOBAL_ONLINE.get(
                    "{SIZE}", String.valueOf(online.size()),
                    "{PLAYERS}", String.join("<gray>, <gold>", online)
            )));

            case "inspect" -> {
                if (args.length < 2) {
                    sender.sendMessage(MessagesUtil.SPECIFY_NICKNAME.get("{SUB}", "inspect <nick>"));
                    return true;
                }
                this.handleInspect(sender, args[1]);
            }

            default -> sender.sendMessage(MessagesUtil.UNKNOWN_OPTION.get());
        }

        return true;
    }

    private void handleInspect(CommandSender sender, String targetName) {
        UserProfileRepository.getIfPresent(targetName).ifPresentOrElse(
                user -> this.sendInspectInfo(sender, user),
                () -> UserProfileRepository.getUserAsync(targetName).thenAccept(opt ->
                        opt.ifPresentOrElse(
                                remoteUser -> this.sendInspectInfo(sender, remoteUser),
                                () -> sender.sendMessage(MessagesUtil.PLAYER_NOT_FOUND_DB.get())
                        )
                )
        );
    }

    private void sendInspectInfo(CommandSender sender, UserProfile u) {
        final long now = System.currentTimeMillis();
        final long cooldownRemaining = Math.max(0, u.getTransferOffsetUntil() - now);
        final long lastTransferElapsed = u.getLastTransferTimestamp() == 0 ? 0 : now - u.getLastTransferTimestamp();

        final String lastTransfer = (u.getLastTransferTimestamp() == 0) ? "NONE" : (lastTransferElapsed / 1000) + "s ago";
        final String cooldown = (cooldownRemaining <= 0) ? "READY" : (cooldownRemaining / 1000) + "s";

        MessagesUtil.INSPECT_FORMAT.asLore(
                "{NICK}", u.getName(),
                "{SECTOR}", u.getSectorName(),
                "{GM}", u.getPlayerGameMode(),
                "{LVL}", String.valueOf(u.getExperienceLevel()),
                "{EXP}", String.valueOf(u.getExperience()),
                "{LAST}", lastTransfer,
                "{COOLDOWN}", cooldown
        ).forEach(sender::sendMessage);
    }
}