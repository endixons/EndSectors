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

package pl.endixon.sectors.paper.command;

import java.util.Arrays;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.endixon.sectors.common.packet.PacketChannel;
import pl.endixon.sectors.common.redis.RedisManager;
import pl.endixon.sectors.common.util.ChatUtil;
import pl.endixon.sectors.paper.PaperSector;
import pl.endixon.sectors.paper.inventory.SectorShowWindow;
import pl.endixon.sectors.paper.manager.SectorManager;
import pl.endixon.sectors.paper.redis.packet.PacketExecuteCommand;
import pl.endixon.sectors.paper.user.profile.UserProfileRepository;

public class SectorCommand implements CommandExecutor {

    private final PaperSector plugin;

    public SectorCommand(PaperSector plugin) {
        this.plugin = plugin;
    }

    private boolean checkPermission(CommandSender sender) {
        if (!sender.hasPermission("endsectors.command.sector")) {
            sender.sendMessage(ChatUtil.fixColors("&cBrak permisji!"));
            return false;
        }
        return true;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!checkPermission(sender)) {
            return false;
        }

        SectorManager sm = plugin.getSectorManager();
        RedisManager redis = plugin.getRedisManager();

        if (args.length == 0) {
            sender.sendMessage(ChatUtil.fixColors("&8────────── &6&lSECTOR HELP &8──────────"));
            sender.sendMessage(ChatUtil.fixColors("&6/sector where &8- &7Aktualny sektor"));
            sender.sendMessage(ChatUtil.fixColors("&6/sector show &8- &7Lista sektorów"));
            sender.sendMessage(ChatUtil.fixColors("&6/sector execute <cmd> &8- &7Komenda na wszystkie sektory"));
            sender.sendMessage(ChatUtil.fixColors("&6/sector isonline <nick> &8- &7Sprawdza online gracza"));
            sender.sendMessage(ChatUtil.fixColors("&6/sector who &8- &7Lista graczy online"));
            sender.sendMessage(ChatUtil.fixColors("&6/sector inspect <nick> &8- &7Info o graczu"));
            sender.sendMessage(ChatUtil.fixColors("&8──────────────────────────────────"));
            return true;
        }

        String sub = args[0].toLowerCase();

        if ("where".equals(sub)) {
            sender.sendMessage(ChatUtil.fixColors("&7Aktualny sektor: &6" + sm.getCurrentSectorName()));
        } else if ("show".equals(sub)) {
            if (sender instanceof Player player) {
                new SectorShowWindow(player, sm).open();
            }
        } else if ("execute".equals(sub)) {
            if (args.length < 2) {
                sender.sendMessage(ChatUtil.fixColors("&cUżycie: &6/sector execute <komenda>"));
                return true;
            }

            String commandToSend = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            redis.publish(PacketChannel.PACKET_EXECUTE_COMMAND, new PacketExecuteCommand(commandToSend));
            sender.sendMessage(ChatUtil.fixColors("&aWysłano komendę do sektorów."));
        } else if ("isonline".equals(sub)) {
            if (args.length < 2) {
                sender.sendMessage(ChatUtil.fixColors("&cPodaj nick: &6/sector isonline <nick>"));
                return true;
            }

            String nick = args[1];
            sm.isPlayerOnline(nick, isOnline -> {
                sender.sendMessage(ChatUtil.fixColors(
                        "&7Gracz &6" + nick + " &7jest: " + (isOnline ? "&aONLINE" : "&cOFFLINE")
                ));
            });
        } else if ("who".equals(sub)) {
            sm.getOnlinePlayers(online -> {
                sender.sendMessage(ChatUtil.fixColors(
                        "&7Online (&6" + online.size() + "&7): &6" + String.join("&7, &6", online)
                ));
            });
        } else if ("inspect".equals(sub)) {
            if (args.length < 2) {
                sender.sendMessage(ChatUtil.fixColors("&cPodaj nick: &6/sector inspect <nick>"));
                return true;
            }

            String targetName = args[1];
            UserProfileRepository.getUserAsync(targetName).thenAccept(optionalUser -> {optionalUser.ifPresentOrElse(u -> {
                    long now = System.currentTimeMillis();
                    long cooldownRemaining = Math.max(0, u.getTransferOffsetUntil() - now);
                    long lastTransferElapsed = u.getLastTransferTimestamp() == 0 ? 0 : now - u.getLastTransferTimestamp();

                    sender.sendMessage(ChatUtil.fixHexColors("      &#00FFFFINFORMACJE O GRACZU            "));
                    sender.sendMessage(ChatUtil.fixHexColors(" &#FFFFFFNick: &#00FF00" + u.getName()));
                    sender.sendMessage(ChatUtil.fixHexColors(" &#FFFFFFSektor: &#00BFFF" + u.getSectorName()));
                    sender.sendMessage(ChatUtil.fixHexColors(" &#FFFFFFGamemode: &#FF69B4" + u.getPlayerGameMode()));
                    sender.sendMessage(ChatUtil.fixHexColors(" &#FFFFFFLevel: &#ADFF2F" + u.getExperienceLevel()));
                    sender.sendMessage(ChatUtil.fixHexColors(" &#FFFFFFExp: &#FFFF00" + u.getExperience()));
                    sender.sendMessage(ChatUtil.fixHexColors(" &#FFFFFFOstatnia zmiana sektora: &#FF8C00" + (u.getLastTransferTimestamp() == 0 ? "BRAK" : (lastTransferElapsed / 1000) + "s temu")));
                    sender.sendMessage(ChatUtil.fixHexColors(" &#FFFFFFpozostaly czas do zmiany sektora: &#FF4500" + (cooldownRemaining <= 0 ? "BRAK" : (cooldownRemaining / 1000) + "s")));
                    sender.sendMessage(ChatUtil.fixHexColors("                                             "));
                    },
                    () -> {
                    sender.sendMessage(ChatUtil.fixHexColors("&cNie znaleziono danych lub gracz jest offline."));
                });
            });
        } else {
            sender.sendMessage(ChatUtil.fixColors("&cNie ma takiej opcji."));
        }

        return true;
    }
}