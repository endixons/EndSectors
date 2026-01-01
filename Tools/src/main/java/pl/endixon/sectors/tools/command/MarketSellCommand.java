package pl.endixon.sectors.tools.command;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import pl.endixon.sectors.tools.EndSectorsToolsPlugin;
import pl.endixon.sectors.tools.market.utils.MarketItemUtil;
import pl.endixon.sectors.tools.user.profile.PlayerMarketProfile;
import pl.endixon.sectors.tools.user.profile.PlayerProfile;
import pl.endixon.sectors.tools.user.profile.ProfileCache;
import pl.endixon.sectors.tools.utils.PlayerDataSerializerUtil;

import java.util.List;

public class MarketSellCommand implements CommandExecutor {

    private final EndSectorsToolsPlugin plugin = EndSectorsToolsPlugin.getInstance();
    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final String PREFIX_FORMAT = "<newline><dark_gray><bold>» <gradient:#ffaa00:#ffff55>MARKET</gradient> <dark_gray><bold>« ";

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Komenda tylko dla graczy");
            return true;
        }

        if (args.length < 1) {
            sendMarketMessage(player, "<gray>Poprawne użycie: <yellow>/wystaw <cena> [ilość]");
            return true;
        }

        final PlayerProfile profile = ProfileCache.get(player.getUniqueId());
        if (profile == null) {
            sendMarketMessage(player, "<red>Twój profil nie został jeszcze załadowany");
            return true;
        }

        String amountArg = (args.length > 1) ? args[1] : null;
        this.handleSellAction(player, profile, args[0], amountArg);
        return true;
    }

    private void handleSellAction(Player player, PlayerProfile profile, String priceRaw, String amountRaw) {
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (itemInHand.getType() == Material.AIR) {
            sendMarketMessage(player, "<red>Musisz trzymać przedmiot w ręce!");
            return;
        }


        double price;
        try {
            price = Double.parseDouble(priceRaw);
            if (price <= 0) {
                sendMarketMessage(player, "<red>Cena musi być większa niż 0!");
                return;
            }
            if (price > 1_000_000_000) {
                sendMarketMessage(player, "<red>Cena jest zbyt wysoka! Zejdź na ziemię, to nie Dubaj.");
                return;
            }
        } catch (NumberFormatException e) {
            sendMarketMessage(player, "<red>Podana cena nie jest poprawną liczbą!");
            return;
        }


        int actualAmount = itemInHand.getAmount();
        int amountToSell = actualAmount;

        if (amountRaw != null) {
            try {
                int parsedAmount = Integer.parseInt(amountRaw);
                if (parsedAmount <= 0) {
                    sendMarketMessage(player, "<red>Ilość musi być większa niż 0.");
                    return;
                }
                if (parsedAmount > actualAmount) {
                    sendMarketMessage(player, "<red>Nie masz tylu przedmiotów! Posiadasz tylko: <yellow>" + actualAmount);
                    return;
                }
                amountToSell = parsedAmount;
            } catch (NumberFormatException e) {
                sendMarketMessage(player, "<red>Podana ilość nie jest liczbą całkowitą!");
                return;
            }
        }

        final List<PlayerMarketProfile> activeOffers = plugin.getMarketRepository().findBySeller(player.getUniqueId());
        final int limit = plugin.getMarketService().getMarketLimit(player);

        if (activeOffers.size() >= limit) {
            sendMarketMessage(player, "<red>Osiągnąłeś limit ofert (<yellow>" + activeOffers.size() + "<gray>/<yellow>" + limit + "<red>)!");
            return;
        }

        ItemStack itemToSerialize = itemInHand.clone();
        itemToSerialize.setAmount(amountToSell);

        final String resolvedName = MarketItemUtil.resolveItemName(itemToSerialize);
        final String category = MarketItemUtil.determineCategory(itemToSerialize.getType());
        final String itemData = PlayerDataSerializerUtil.serializeItemStacksToBase64(new ItemStack[]{itemToSerialize});


        if (amountToSell == actualAmount) {
            player.getInventory().setItemInMainHand(null);
        } else {
            itemInHand.setAmount(actualAmount - amountToSell);
        }

        plugin.getMarketService().listOffer(profile, itemData, resolvedName, category, price);
        sendMarketMessage(player, "<green>Pomyślnie wystawiono przedmiot: <white>" + resolvedName + " <gray>(x" + amountToSell + ")");
        sendMarketMessage(player, "<gray>Cena: <gradient:#55ff55:#00aa00><bold>" + price + "$</bold></gradient> <dark_gray>| <gray>Kategoria: <aqua>" + category);
    }


    private void sendMarketMessage(Player player, String message) {
        player.sendMessage(MM.deserialize(PREFIX_FORMAT + message));
    }
}