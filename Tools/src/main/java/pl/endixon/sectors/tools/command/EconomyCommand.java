package pl.endixon.sectors.tools.command;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.endixon.sectors.tools.EndSectorsToolsPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class EconomyCommand implements CommandExecutor, TabCompleter {

    private final Economy economy = EndSectorsToolsPlugin.getInstance().getEconomy();
    private static final MiniMessage MM = MiniMessage.miniMessage();

    private static final String TEXT = "<#a8a8a8>";
    private static final String ERROR = "<#ff4b2b>";
    private static final String ACCENT = "<#ff5f6d>";

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {

        if (args.length == 0) {
            if (!(sender instanceof Player player)) return true;
            this.sendBalance(player, player);
            return true;
        }

        if (args[0].equalsIgnoreCase("pay") && args.length == 3) {
            if (!(sender instanceof Player player)) return true;
            this.handlePay(player, args[1], args[2]);
            return true;
        }

        if (args.length == 1 && sender.hasPermission("economy.admin")) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
            this.sendBalance(sender, target);
            return true;
        }

        if (args.length == 3 && sender.hasPermission("economy.admin")) {
            this.handleAdminActions(sender, args);
            return true;
        }

        this.sendUsage(sender);
        return true;
    }

    private void sendUsage(CommandSender sender) {
        String usage = sender.hasPermission("economy.admin")
                ? TEXT + "Użycie: " + ACCENT + "/eco <nick> | /eco <set/add/take> <nick> <kwota> | /eco pay <nick> <kwota>"
                : TEXT + "Użycie: " + ACCENT + "/eco | /eco pay <nick> <kwota>";

        sender.sendMessage(MM.deserialize(usage));
    }

    private void sendBalance(CommandSender viewer, OfflinePlayer target) {
        double bal = economy.getBalance(target);
        String msg = TEXT + "Stan konta <white>" + target.getName() + TEXT + ": <#fbff00>" + economy.format(bal);
        viewer.sendMessage(MM.deserialize(msg));
    }

    private void handlePay(Player sender, String targetName, String amountRaw) {
        double amount;
        try {
            amount = Double.parseDouble(amountRaw);
            if (amount < 0.01) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            sender.sendMessage(MM.deserialize(ERROR + "Podaj poprawną kwotę (min. 0.01)!"));
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

        if (target.getUniqueId().equals(sender.getUniqueId())) {
            sender.sendMessage(MM.deserialize(ERROR + "Nie możesz przelać pieniędzy samemu sobie!"));
            return;
        }

        if (!economy.has(sender, amount)) {
            sender.sendMessage(MM.deserialize(ERROR + "Nie masz wystarczających środków!"));
            return;
        }

        economy.withdrawPlayer(sender, amount);
        economy.depositPlayer(target, amount);

        sender.sendMessage(MM.deserialize("<#00ff87>Przelałeś <#fbff00>" + economy.format(amount) + TEXT + " do <white>" + target.getName()));

        if (target.isOnline() && target.getPlayer() != null) {
            target.getPlayer().sendMessage(MM.deserialize("<#00ff87>Otrzymałeś <#fbff00>" + economy.format(amount) + TEXT + " od <white>" + sender.getName()));
        }
    }

    private void handleAdminActions(CommandSender sender, String[] args) {
        String action = args[0].toLowerCase();
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);

        if (!economy.hasAccount(target)) {
            sender.sendMessage(MM.deserialize("<#ef4444>Profil użytkownika nie został znaleziony!"));
            return;
        }

        double value;
        try {
            value = Double.parseDouble(args[2]);
        } catch (Exception e) {
            this.sendUsage(sender);
            return;
        }

        switch (action) {
            case "set" -> {
                double current = economy.getBalance(target);
                economy.withdrawPlayer(target, current);
                economy.depositPlayer(target, value);
            }
            case "add" -> economy.depositPlayer(target, value);
            case "take" -> economy.withdrawPlayer(target, value);
            default -> {
                this.sendUsage(sender);
                return;
            }
        }

        sender.sendMessage(MM.deserialize(TEXT + "Zaktualizowano balans <white>" + target.getName() + TEXT + ": <#fbff00>" + economy.format(economy.getBalance(target))));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> matches = new ArrayList<>();
            matches.add("pay");
            if (sender.hasPermission("economy.admin")) {
                matches.addAll(List.of("set", "add", "take"));
            }
            return matches.stream().filter(s -> s.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        return (args.length == 2) ? null : Collections.emptyList();
    }
}