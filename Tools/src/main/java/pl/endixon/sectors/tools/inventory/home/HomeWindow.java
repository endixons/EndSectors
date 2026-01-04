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

package pl.endixon.sectors.tools.inventory.home;

import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.endixon.sectors.common.sector.SectorType;
import pl.endixon.sectors.paper.SectorsAPI;
import pl.endixon.sectors.paper.sector.Sector;
import pl.endixon.sectors.paper.user.profile.UserProfile;
import pl.endixon.sectors.tools.EndSectorsToolsPlugin;
import pl.endixon.sectors.tools.inventory.api.WindowUI;
import pl.endixon.sectors.tools.inventory.api.builder.StackBuilder;
import pl.endixon.sectors.tools.user.profile.player.PlayerProfile;
import pl.endixon.sectors.tools.user.profile.cache.ProfileHome;
import pl.endixon.sectors.tools.utils.MessagesUtil;
import pl.endixon.sectors.tools.utils.TeleportUtil;

public class HomeWindow {
    private final Player player;
    private final SectorsAPI sectorsAPI;
    private final EndSectorsToolsPlugin plugin = EndSectorsToolsPlugin.getInstance();
    private final PlayerProfile profile;
    private final UserProfile user;
    private static final int HOME_SLOTS = 3;
    private static final int COUNTDOWN_TIME = 10;



    public HomeWindow(Player player, PlayerProfile profile, SectorsAPI sectorsAPI) {
        this.player = player;
        this.profile = profile;
        this.sectorsAPI = sectorsAPI;
        this.user = sectorsAPI.getUser(player).orElse(null);
        open();
    }

    public void open() {
        Map<String, ProfileHome> homes = profile.getHomes();
        WindowUI window = new WindowUI(MessagesUtil.HOME_GUI_TITLE.getText(), 1);
        Sector currentSector = sectorsAPI.getSectorManager().getCurrentSector();

        for (int i = 0; i < HOME_SLOTS; i++) {
            String homeKey = "Domek #" + (i + 1);
            ProfileHome home = homes.get(homeKey);

            String homeName = (home != null ? home.getName() : homeKey);


            String name = MessagesUtil.HOME_NAME_FORMAT.getText("{NAME}", homeName);

            StackBuilder builder = new StackBuilder(new ItemStack(Material.OAK_SIGN)).name(name);
            if (home != null) {
                builder.lores(MessagesUtil.HOME_SET_LORE.asLore(
                        "{SECTOR}", home.getSector(),
                        "{X}", String.valueOf((int)home.getX()),
                        "{Y}", String.valueOf((int)home.getY()),
                        "{Z}", String.valueOf((int)home.getZ())
                ));
            } else {
                builder.lores(MessagesUtil.HOME_EMPTY_LORE.asLore(
                        "{SECTOR}", currentSector.getName()
                ));
            }

            window.setSlot(i, builder.build(), event -> {
                if (home != null) {
                    if (event.isLeftClick()) {
                        handleTeleport(home, user);
                    } else if (event.isRightClick()) {
                        homes.remove(home.getName());
                        plugin.getRepository().save(profile);
                        player.sendMessage(MessagesUtil.HOME_DELETED_SUCCESS.get());
                        open();
                    }
                } else {
                    if (currentSector.getType() == SectorType.SPAWN) {
                        player.closeInventory();
                        player.sendMessage(MessagesUtil.HOME_CANT_CREATE_SPAWN.get());
                        return;
                    }
                    Location loc = player.getLocation();
                    ProfileHome newHome = new ProfileHome(homeKey, currentSector.getName(), loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                    homes.put(homeKey, newHome);
                    plugin.getRepository().save(profile);
                    player.sendMessage(MessagesUtil.HOME_CREATED_SUCCESS.get());
                    open();
                }
            });
        }
        player.openInventory(window.getInventory());
    }

    private void handleTeleport(ProfileHome home, UserProfile user) {
        if (user == null) return;

        Sector homeSector = sectorsAPI.getSectorManager().getSector(home.getSector());
        if (homeSector == null) {
            player.sendMessage(MessagesUtil.HOME_SECTOR_NOT_FOUND.get());
            return;
        }

        World world = Bukkit.getWorld(homeSector.getWorldName());
        if (world == null) {
            player.sendMessage(MessagesUtil.HOME_WORLD_NOT_LOADED.get());
            return;
        }

        user.setX(home.getX()); user.setY(home.getY()); user.setZ(home.getZ());
        user.setYaw(home.getYaw()); user.setPitch(home.getPitch());

        boolean isAdmin = player.hasPermission("endsectors.admin");
        int countdown = isAdmin ? 0 : COUNTDOWN_TIME;


        if (home.getSector().equals(sectorsAPI.getSectorManager().getCurrentSectorName())) {

            TeleportUtil.startTeleportCountdown(player, countdown, () -> {
                player.closeInventory();
            player.teleport(new Location(world, home.getX(), home.getY(), home.getZ(), home.getYaw(), home.getPitch()));
            user.updateAndSave(player, homeSector, false);

            });
        } else {
            TeleportUtil.startTeleportCountdown(player, countdown, () -> {
                player.closeInventory();
            sectorsAPI.getPaperSector().getSectorTeleport().teleportToSector(player, user, homeSector, false, true);
            });
        }
    }
}