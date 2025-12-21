/*
 *
 *  EndSectors  Non-Commercial License
 *  (c) 2025 Endixon
 *
 *  Permission is granted to use, copy, and
 *  modify this software **only** for personal
 *  or educational purposes.
 *
 *   Commercial use, redistribution, claiming
 *  this work as your own, or copying code
 *  without explicit permission is strictly
 *  prohibited.
 *
 *  Visit https://github.com/Endixon/EndSectors
 *  for more info.
 *
 */

package pl.endixon.sectors.tools.inventory;

import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.endixon.sectors.common.sector.SectorType;
import pl.endixon.sectors.common.util.ChatUtil;
import pl.endixon.sectors.paper.SectorsAPI;
import pl.endixon.sectors.paper.sector.Sector;
import pl.endixon.sectors.paper.user.profile.UserProfile;
import pl.endixon.sectors.paper.util.ChatAdventureUtil;
import pl.endixon.sectors.tools.Main;
import pl.endixon.sectors.tools.inventory.api.WindowUI;
import pl.endixon.sectors.tools.inventory.api.builder.StackBuilder;
import pl.endixon.sectors.tools.user.profile.ProfileHome;
import pl.endixon.sectors.tools.utils.LoggerUtil;

public class HomeWindow {
    private final ChatAdventureUtil CHAT = new ChatAdventureUtil();
    private final Player player;
    private final SectorsAPI sectorsAPI;
    private final Main plugin = Main.getInstance();
    private static final int HOME_SLOTS = 3;
    private pl.endixon.sectors.tools.user.profile.PlayerProfile profile;
    private UserProfile user;

    public HomeWindow(Player player, pl.endixon.sectors.tools.user.profile.PlayerProfile profile, SectorsAPI sectorsAPI) {
        if (sectorsAPI == null) {
            throw new IllegalArgumentException("SectorsAPI cannot be null!");
        }

        this.player = player;
        this.profile = profile;
        this.sectorsAPI = sectorsAPI;
        this.user = sectorsAPI.getUser(player).orElse(null);
        open();
    }

    public void open() {
        Map<String, ProfileHome> homes = profile.getHomes();
        WindowUI window = new WindowUI("&7Twoje Domki", 1);
        Sector currentSector = sectorsAPI.getSectorManager().getCurrentSector();

        for (int i = 0; i < HOME_SLOTS; i++) {
            String homeKey = "Domek #" + (i + 1);
            ProfileHome home = homes.get(homeKey);
            ItemStack item;

            if (home != null) {
                item = new StackBuilder(new ItemStack(Material.OAK_SIGN)).name(ChatUtil.fixHexColors("&#4ade80" + home.getName())).lores(buildLore(home)).build();
            } else {
                item = new StackBuilder(new ItemStack(Material.OAK_SIGN)).name(ChatUtil.fixHexColors("&#4ade80" + homeKey)).lores(List.of(ChatUtil.fixHexColors(""), ChatUtil.fixHexColors(""), ChatUtil.fixHexColors("&#9ca3afKliknij &#4ade80lewy lub prawy przyciskiem&#9ca3af, aby"), ChatUtil.fixHexColors("&#9ca3afzapisać aktualną pozycję jako nowy domek."), ChatUtil.fixHexColors(""), ChatUtil.fixHexColors("&#ffffffTwój domek zostanie automatycznie zapisany"), ChatUtil.fixHexColors("&#ffffffna tej pozycji w sektorze &#facc15" + currentSector.getName()), ChatUtil.fixHexColors(""), ChatUtil.fixHexColors(""))).build();
            }

            window.setSlot(i, item, event -> {
                if (home != null) {
                    if (event.isLeftClick()) {
                        handleTeleport(home, user);
                    } else if (event.isRightClick()) {
                        homes.remove(home.getName());
                        plugin.getRepository().save(profile);
                        player.sendMessage(CHAT.toComponent("&#FF5555Usunięto Domek"));
                        open();
                    }
                } else {

                    if (currentSector.getType() == SectorType.SPAWN) {
                        player.closeInventory();
                        player.sendMessage(CHAT.toComponent("&#FF5555Nie możesz tworzyć domków na sektorze SPAWN!"));
                        return;
                    }

                    Location loc = player.getLocation();
                    ProfileHome newHome = new ProfileHome(homeKey, currentSector.getName(), loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                    homes.put(homeKey, newHome);
                    plugin.getRepository().save(profile);
                    player.sendMessage(CHAT.toComponent("&#00FFAAPomyślnie utworzono Domek"));
                    open();
                }
            });
        }

        player.openInventory(window.getInventory());
    }

    private List<String> buildLore(ProfileHome home) {
        return List.of(ChatUtil.fixHexColors(""), ChatUtil.fixHexColors(""), ChatUtil.fixHexColors("&#9ca3afKliknij &#4ade80lewy przycisk&#9ca3af, aby"), ChatUtil.fixHexColors("&#9ca3afprzeteleportować się do tego domku."), ChatUtil.fixHexColors(""), ChatUtil.fixHexColors("&#9ca3afKliknij &#4ade80prawy przycisk&#9ca3af, aby"), ChatUtil.fixHexColors("&#9ca3afusunąć ten domek."), ChatUtil.fixHexColors(""), ChatUtil.fixHexColors("&#ffffffTwój domek znajduje się w sektorze &#facc15" + home.getSector()), ChatUtil.fixHexColors("&#ffffffPozycja: X:" + home.getX() + " Y:" + home.getY() + " Z:" + home.getZ()), ChatUtil.fixHexColors(""), ChatUtil.fixHexColors(""));
    }

    private void handleTeleport(ProfileHome home, UserProfile user) {

        if (user == null) {
            LoggerUtil.info("handleTeleport called with null user for home: " + (home != null ? home.getName() : "null"));
            return;
        }

        Sector homeSector = sectorsAPI.getSectorManager().getSector(home.getSector());

        if (homeSector == null) {
            player.sendMessage(CHAT.toComponent("&#FF5555Nie udało się znaleźć sektora dla twojego domku!"));
            return;
        }

        World world = Bukkit.getWorld(homeSector.getWorldName());

        if (world == null) {
            player.sendMessage(CHAT.toComponent("&#FF5555Świat dla tego sektora nie jest załadowany!"));
            return;
        }

        user.setTransferOffsetUntil(0);
        user.setX(home.getX());
        user.setY(home.getY());
        user.setZ(home.getZ());
        user.setYaw(home.getYaw());
        user.setPitch(home.getPitch());
        Location loc = new Location(world, home.getX(), home.getY(), home.getZ(), home.getYaw(), home.getPitch());

        if (home.getSector().equals(user.getSectorName())) {
            player.teleport(loc);
            user.updateAndSave(player, homeSector);
            player.sendMessage(CHAT.toComponent("&#00FFAAPomyślnie przeteleportowano!"));

        } else {
            sectorsAPI.getPaperSector().getSectorTeleport().teleportToSector(player, user, homeSector, false, true);
            player.sendMessage(CHAT.toComponent("&#00FFAAPomyślnie przeteleportowano!"));
        }
    }
}
