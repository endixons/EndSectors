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
import pl.endixon.sectors.tools.user.profile.player.PlayerMarketProfile;
import pl.endixon.sectors.tools.user.profile.player.PlayerProfile;
import pl.endixon.sectors.tools.user.profile.cache.ProfileCache;
import pl.endixon.sectors.tools.utils.MessagesUtil;
import pl.endixon.sectors.tools.utils.PlayerDataSerializerUtil;

import java.util.List;

public class MarketSellCommand implements CommandExecutor {

    private final EndSectorsToolsPlugin plugin = EndSectorsToolsPlugin.getInstance();
    private static final MiniMessage MM = MiniMessage.miniMessage();

    // UI Constants
    private static final String PREFIX = "<newline><dark_gray><bold>» <gradient:#ffaa00:#ffff55>MARKET</gradient> <dark_gray><bold>« ";
    private static final String ERROR_COLOR = "<#ff4b2b>";
    private static final String SUCCESS_COLOR = "<#00ff87>";
    private static final String ACCENT_COLOR = "<#fbff00>";
    private static final String TEXT_COLOR = "<#a8a8a8>";

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(MessagesUtil.CONSOLE_BLOCK.get());
            return true;
        }

        if (args.length < 1) {
            sendMarketMessage(player, TEXT_COLOR + "Poprawne użycie: " + ACCENT_COLOR + "/wystaw <cena> [ilość]");
            return true;
        }

        final PlayerProfile profile = ProfileCache.get(player.getUniqueId());
        if (profile == null) {
            sendMarketMessage(player, ERROR_COLOR + "Twój profil nie został jeszcze załadowany!");
            return true;
        }

        String amountArg = (args.length > 1) ? args[1] : null;
        this.handleSellAction(player, profile, args[0], amountArg);
        return true;
    }

    private void handleSellAction(Player player, PlayerProfile profile, String priceRaw, String amountRaw) {
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (itemInHand.getType() == Material.AIR) {
            sendMarketMessage(player, ERROR_COLOR + "Musisz trzymać przedmiot w ręce!");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceRaw);
            if (price <= 0) {
                sendMarketMessage(player, ERROR_COLOR + "Cena musi być większa niż 0!");
                return;
            }
            if (price > 1_000_000_000) {
                sendMarketMessage(player, ERROR_COLOR + "Cena jest zbyt wysoka! Zejdź na ziemię, to nie Dubaj.");
                return;
            }
        } catch (NumberFormatException e) {
            sendMarketMessage(player, ERROR_COLOR + "Podana cena nie jest poprawną liczbą!");
            return;
        }

        int actualAmount = itemInHand.getAmount();
        int amountToSell = actualAmount;

        if (amountRaw != null) {
            try {
                int parsedAmount = Integer.parseInt(amountRaw);
                if (parsedAmount <= 0) {
                    sendMarketMessage(player, ERROR_COLOR + "Ilość musi być większa niż 0.");
                    return;
                }
                if (parsedAmount > actualAmount) {
                    sendMarketMessage(player, ERROR_COLOR + "Nie masz tylu przedmiotów! Posiadasz tylko: " + ACCENT_COLOR + actualAmount);
                    return;
                }
                amountToSell = parsedAmount;
            } catch (NumberFormatException e) {
                sendMarketMessage(player, ERROR_COLOR + "Podana ilość nie jest liczbą całkowitą!");
                return;
            }
        }

        final List<PlayerMarketProfile> activeOffers = plugin.getMarketRepository().findBySeller(player.getUniqueId());
        final int limit = plugin.getMarketService().getMarketLimit(player);

        if (activeOffers.size() >= limit) {
            sendMarketMessage(player, ERROR_COLOR + "Osiągnąłeś limit ofert (" + ACCENT_COLOR + activeOffers.size() + "<dark_gray>/</dark_gray>" + ACCENT_COLOR + limit + ERROR_COLOR + ")!");
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


        sendMarketMessage(player, SUCCESS_COLOR + "Pomyślnie wystawiono: <white>" + resolvedName + TEXT_COLOR + " (x" + amountToSell + ")");
        sendMarketMessage(player, TEXT_COLOR + "Cena: <gradient:#55ff55:#00aa00><bold>" + String.format("%.2f", price) + "$</bold></gradient> <dark_gray>| " + TEXT_COLOR + "Kategoria: <#00d2ff>" + category);
    }

    private void sendMarketMessage(Player player, String message) {
        player.sendMessage(MM.deserialize(PREFIX + message));
    }
}