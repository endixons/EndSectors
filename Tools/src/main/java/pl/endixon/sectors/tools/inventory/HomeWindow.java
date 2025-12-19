package pl.endixon.sectors.tools.inventory;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import pl.endixon.sectors.common.sector.SectorType;
import pl.endixon.sectors.common.util.ChatUtil;
import pl.endixon.sectors.paper.SectorsAPI;
import pl.endixon.sectors.paper.sector.Sector;
import pl.endixon.sectors.paper.user.UserRedis;
import pl.endixon.sectors.tools.Main;
import pl.endixon.sectors.tools.inventory.api.WindowUI;
import pl.endixon.sectors.tools.inventory.api.builder.StackBuilder;
import pl.endixon.sectors.tools.service.home.Home;
import pl.endixon.sectors.tools.service.users.PlayerProfile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class HomeWindow {

    private final Player player;
    private final SectorsAPI sectorsAPI = SectorsAPI.getInstance();
    private final Main plugin = Main.getInstance();
    private static final int HOME_SLOTS = 3;

    private PlayerProfile profile;
    private UserRedis user;

    public HomeWindow(Player player, PlayerProfile profile) {
        this.player = player;
        this.profile = profile;
        this.user = sectorsAPI.getUser(player).orElse(null);
        open();
    }

    public void open() {
        Map<String, Home> homes = profile.getHomes();
        WindowUI window = new WindowUI("&7Twoje Domki", 1);
        Sector currentSector = sectorsAPI.getSectorManager().getCurrentSector();

        for (int i = 0; i < HOME_SLOTS; i++) {
            String homeKey = "Domek #" + (i + 1);
            Home home = homes.get(homeKey);

            ItemStack item;
            if (home != null) {
                item = new StackBuilder(new ItemStack(Material.OAK_SIGN))
                        .name("&a" + home.getName())
                        .lores(buildLore(home))
                        .build();
            } else {
                item = new StackBuilder(new ItemStack(Material.OAK_SIGN))
                        .name(ChatUtil.fixHexColors("&#4ade80" + homeKey))
                        .lores(List.of(
                                ChatUtil.fixHexColors(""),
                                ChatUtil.fixHexColors(""),
                                ChatUtil.fixHexColors("&#9ca3afKliknij &#4ade80lewy lub prawy przyciskiem&#9ca3af, aby"),
                                ChatUtil.fixHexColors("&#9ca3afzapisać aktualną pozycję jako nowy domek."),
                                ChatUtil.fixHexColors(""),
                                ChatUtil.fixHexColors("&#ffffffTwój domek zostanie automatycznie zapisany"),
                                ChatUtil.fixHexColors("&#ffffffna tej pozycji w sektorze &#facc15" + currentSector.getName()),
                                ChatUtil.fixHexColors(""),
                                ChatUtil.fixHexColors("")
                        ))
                        .build();
            }

            final int currentSlot = i;
            window.setSlot(currentSlot, item, event -> {
                if (home != null) {
                    if (event.isLeftClick()) {
                        handleTeleport(home, user);
                    } else if (event.isRightClick()) {
                        homes.remove(home.getName());
                        plugin.getRepository().save(profile);
                        player.sendMessage(ChatUtil.fixHexColors("&#FF5555Usunięto Domek"));
                        open();
                    }
                } else {

                    if (currentSector.getType() == SectorType.SPAWN) {
                        player.closeInventory();
                        player.sendMessage(ChatUtil.fixHexColors("&#FF5555Nie możesz tworzyć domków na sektorze SPAWN!"));
                        return;
                    }

                    Location loc = player.getLocation();
                    Home newHome = new Home(
                            homeKey,
                            currentSector.getName(),
                            loc.getWorld().getName(),
                            loc.getX(),
                            loc.getY(),
                            loc.getZ(),
                            loc.getYaw(),
                            loc.getPitch()
                    );
                    homes.put(homeKey, newHome);
                    plugin.getRepository().save(profile);
                    player.sendMessage(ChatUtil.fixHexColors("&#00FFAAPomyślnie utworzono Domek " + homeKey));
                    open();
                }
            });
        }

        player.openInventory(window.getInventory());
    }

    private List<String> buildLore(Home home) {
        return List.of(
                ChatUtil.fixHexColors(""),
                ChatUtil.fixHexColors(""),
                ChatUtil.fixHexColors("&#9ca3afKliknij &#4ade80lewy przycisk&#9ca3af, aby"),
                ChatUtil.fixHexColors("&#9ca3afprzeteleportować się do tego domku."),
                ChatUtil.fixHexColors(""),
                ChatUtil.fixHexColors("&#9ca3afKliknij &#4ade80prawy przycisk&#9ca3af, aby"),
                ChatUtil.fixHexColors("&#9ca3afusunąć ten domek."),
                ChatUtil.fixHexColors(""),
                ChatUtil.fixHexColors("&#ffffffTwój domek znajduje się w sektorze &#facc15" + home.getSector()),
                ChatUtil.fixHexColors("&#ffffffPozycja: X:" + home.getX() + " Y:" + home.getY() + " Z:" + home.getZ()),
                ChatUtil.fixHexColors(""),
                ChatUtil.fixHexColors("")
        );
    }


    private void handleTeleport(Home home, UserRedis user) {
        if (user == null) return;

        Sector homeSector = sectorsAPI.getSectorManager().getSector(home.getSector());
        if (homeSector == null) {
            player.sendMessage(Component.text(ChatUtil.fixHexColors("&#FF5555Nie udało się znaleźć sektora dla home!")));
            return;
        }

        World world = Bukkit.getWorld(homeSector.getWorldName());
        if (world == null) {
            player.sendMessage(Component.text(ChatUtil.fixHexColors("&#FF5555Świat dla home nie jest załadowany!")));
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
            player.sendMessage(Component.text(ChatUtil.fixHexColors("&#00FFAAPomyślnie przeteleportowano!")));
        } else {
            sectorsAPI.getPaperSector().getSectorTeleportService()
                    .teleportToSector(player, user, homeSector, false, true);
            player.sendMessage(Component.text(ChatUtil.fixHexColors("&#00FFAAPomyślnie przeteleportowano!")));
        }
    }
}
