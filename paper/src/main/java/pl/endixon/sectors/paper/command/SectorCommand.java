/*
 * EndSectors - Non-Commercial License (2025)
 * (c) Endixon
 */

package pl.endixon.sectors.paper.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import org.bukkit.entity.Player;
import pl.endixon.sectors.common.packet.PacketChannel;
import pl.endixon.sectors.common.redis.RedisManager;
import pl.endixon.sectors.paper.inventory.SectorShowWindow;
import pl.endixon.sectors.paper.redis.packet.PacketExecuteCommand;
import pl.endixon.sectors.common.util.ChatUtil;
import pl.endixon.sectors.paper.PaperSector;
import pl.endixon.sectors.paper.sector.Sector;
import pl.endixon.sectors.paper.sector.SectorManager;
import pl.endixon.sectors.paper.user.UserMongo;
import pl.endixon.sectors.paper.util.TpsUtil;

import java.util.Arrays;
import java.util.List;

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

        if (!checkPermission(sender)) return false;

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

        switch (sub) {

            case "where": {
                sender.sendMessage(ChatUtil.fixColors("&7Aktualny sektor: &6" + sm.getCurrentSectorName()));
                break;
            }

            case "show": {
                if (!(sender instanceof Player player)) return true;

                new SectorShowWindow(player, sm).open();
                break;
            }


            case "execute": {
                if (args.length < 2) {
                    sender.sendMessage(ChatUtil.fixColors("&cUżycie: &6/sector execute <komenda>"));
                    return true;
                }

                String commandToSend = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                redis.publish(PacketChannel.PACKET_EXECUTE_COMMAND, new PacketExecuteCommand(commandToSend));

                sender.sendMessage(ChatUtil.fixColors("&aWysłano komendę do sektorów."));
                break;
            }

            case "isonline": {
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

                break;
            }


            case "who": {
                sm.getOnlinePlayers(online -> {
                    sender.sendMessage(ChatUtil.fixColors(
                            "&7Online (&6" + online.size() + "&7): &6" + String.join("&7, &6", online)
                    ));
                });
                break;
            }


            case "inspect": {
                if (args.length < 2) {
                    sender.sendMessage(ChatUtil.fixColors("&cPodaj nick: &6/sector inspect <nick>"));
                    return true;
                }

                String targetName = args[1];

                plugin.getUserManager().getUser(targetName).thenAccept(u -> {
                    if (u == null) {
                        sender.sendMessage(ChatUtil.fixColors("&cNie znaleziono danych lub gracz jest offline."));
                        return;
                    }

                    sender.sendMessage(ChatUtil.fixColors("&e&m==========&6&l[ INFORMACJE O GRACZU ]&e&m=========="));
                    sender.sendMessage(ChatUtil.fixColors(" &fNick: &a" + u.getName()));
                    sender.sendMessage(ChatUtil.fixColors(" &fSektor: &b" + u.getSectorName()));
                    sender.sendMessage(ChatUtil.fixColors(" &fGamemode: &d" + u.getPlayerGameMode()));
                    sender.sendMessage(ChatUtil.fixColors(" &fLevel: &a" + u.getExperienceLevel()));
                    sender.sendMessage(ChatUtil.fixColors(" &fExp: &e" + u.getExperience()));
                    sender.sendMessage(ChatUtil.fixColors("&e&m=========================================="));
                });

                break;
            }


            default: {
                sender.sendMessage(ChatUtil.fixColors("&cNie ma takiej opcji."));
                break;
            }
        }

        return true;
    }
}
