package pl.endixon.sectors.paper.user;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;
import pl.endixon.sectors.common.redis.MongoExecutor;
import pl.endixon.sectors.common.sector.SectorType;
import pl.endixon.sectors.paper.PaperSector;
import pl.endixon.sectors.paper.redis.packet.PacketPlayerInfoRequest;
import pl.endixon.sectors.paper.sector.Sector;
import pl.endixon.sectors.paper.util.Logger;
import pl.endixon.sectors.paper.util.PlayerDataSerializer;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Getter
@Setter
public class UserMongo {

    private String name;
    private String sectorName;
    private boolean firstJoin;
    private long lastSectorTransfer;
    private long lastTransferTimestamp;
    private boolean teleportingToSector;
    private int foodLevel;
    private int experience;
    private int experienceLevel;
    private int fireTicks;
    private boolean allowFlight;
    private boolean flying;
    private double x, y, z;
    private float yaw, pitch;
    private String playerGameMode;
    private String playerInventoryData;
    private String playerEnderChestData;
    private String playerEffectsData;

    private final MongoCollection<Document> collection;

    public UserMongo(@NonNull Player player) {
        this.collection = PaperSector.getInstance().getMongoManager().getUsersCollection();
        Sector current = PaperSector.getInstance().getSectorManager().getCurrentSector();
        this.sectorName = (current != null && current.getType() != SectorType.QUEUE) ? current.getName() : "null";
        this.firstJoin = true;
        this.lastSectorTransfer = 0L;
        this.lastTransferTimestamp = 0L;
        this.teleportingToSector = false;
        updateFromPlayer(player);
    }


    public UserMongo(@NonNull Document doc) {
        this.collection = PaperSector.getInstance().getMongoManager().getUsersCollection();
        updateFromMongo(doc);
    }

    public boolean needsUpdate(@NonNull Document doc) {
        try {
            if (!name.equals(doc.getString("Name"))) return true;
            if (!sectorName.equals(doc.getString("sectorName"))) return true;
            if (firstJoin != doc.getBoolean("firstJoin", true)) return true;
            if (lastSectorTransfer != getLongSafe(doc, "lastSectorTransfer")) return true;
            if (lastTransferTimestamp != getLongSafe(doc, "lastTransferTimestamp")) return true;
            if (teleportingToSector != doc.getBoolean("teleportingToSector", false)) return true;

            if (Double.compare(x, getDoubleSafe(doc, "X")) != 0) return true;
            if (Double.compare(y, getDoubleSafe(doc, "Y")) != 0) return true;
            if (Double.compare(z, getDoubleSafe(doc, "Z")) != 0) return true;
            if (Float.compare(yaw, (float) getDoubleSafe(doc, "Yaw")) != 0) return true;
            if (Float.compare(pitch, (float) getDoubleSafe(doc, "Pitch")) != 0) return true;

            if (!playerGameMode.equals(doc.getString("playerGameMode"))) return true;
            if (!playerInventoryData.equals(doc.getString("playerInventoryData"))) return true;
            if (!playerEnderChestData.equals(doc.getString("playerEnderChestData"))) return true;
            if (!playerEffectsData.equals(doc.getString("playerEffectsData"))) return true;

            if (foodLevel != getIntSafe(doc, "foodLevel", 20)) return true;
            if (experience != getIntSafe(doc, "experience", 0)) return true;
            if (experienceLevel != getIntSafe(doc, "experienceLevel", 0)) return true;
            if (fireTicks != getIntSafe(doc, "fireTicks", 0)) return true;
            if (allowFlight != doc.getBoolean("allowFlight", false)) return true;
            if (flying != doc.getBoolean("flying", false)) return true;

            return false;
        } catch (Exception e) {
            return true;
        }
    }


    public void updateFromMongo(@NonNull Document doc) {
        this.name = doc.getString("Name");
        this.sectorName = doc.getString("sectorName");
        this.firstJoin = doc.getBoolean("firstJoin", true);
        this.lastSectorTransfer = doc.getLong("lastSectorTransfer") != null ? doc.getLong("lastSectorTransfer") : 0L;
        this.lastTransferTimestamp = doc.getLong("lastTransferTimestamp") != null ? doc.getLong("lastTransferTimestamp") : 0L;
        this.teleportingToSector = doc.getBoolean("teleportingToSector", false);
        this.x = doc.getDouble("X");
        this.y = doc.getDouble("Y");
        this.z = doc.getDouble("Z");
        this.yaw = doc.getDouble("Yaw").floatValue();
        this.pitch = doc.getDouble("Pitch").floatValue();
        this.playerGameMode = doc.getString("playerGameMode");
        this.playerInventoryData = doc.getString("playerInventoryData");
        this.playerEnderChestData = doc.getString("playerEnderChestData");
        this.playerEffectsData = doc.getString("playerEffectsData");
        this.foodLevel = doc.getInteger("foodLevel", 20);
        this.experience = doc.getInteger("experience", 0);
        this.experienceLevel = doc.getInteger("experienceLevel", 0);
        this.fireTicks = doc.getInteger("fireTicks", 0);
        this.allowFlight = doc.getBoolean("allowFlight", false);
        this.flying = doc.getBoolean("flying", false);
    }

    public void updateFromPlayer(@NonNull Player player) {
        this.name = player.getName();
        Location loc = player.getLocation();
        this.x = loc.getX();
        this.y = loc.getY();
        this.z = loc.getZ();
        this.yaw = loc.getYaw();
        this.pitch = loc.getPitch();

        this.playerGameMode = player.getGameMode().name();
        this.foodLevel = player.getFoodLevel();
        this.experience = player.getTotalExperience();
        this.experienceLevel = player.getLevel();
        this.fireTicks = player.getFireTicks();
        this.allowFlight = player.getAllowFlight();
        this.flying = player.isFlying();

        this.playerInventoryData = PlayerDataSerializer.serializeItemStacksToBase64(player.getInventory().getContents());
        this.playerEnderChestData = PlayerDataSerializer.serializeItemStacksToBase64(player.getEnderChest().getContents());
        this.playerEffectsData = PlayerDataSerializer.serializeEffects(player);
    }




    public CompletableFuture<Void> insert() {
        return CompletableFuture.runAsync(() -> {
                    collection.insertOne(toDocument());
                    UserManager.getUsers().put(name.toLowerCase(), this);
                }, MongoExecutor.EXECUTOR)
                .exceptionally(ex -> {
                    Logger.info(() -> "Failed to insert player " + name + ": " + ex.getMessage());
                    return null;
                });
    }


    public void updatePlayerData(@NonNull Player player, Sector sector) {
        Sector current = PaperSector.getInstance().getSectorManager().getCurrentSector();
        this.sectorName = (current != null && current.getType() != SectorType.QUEUE) ? current.getName() : "null";

        updateFromPlayer(player);
        UserManager.getUsers().put(name.toLowerCase(), this);
        CompletableFuture.runAsync(() ->
                        collection.updateOne(Filters.eq("Name", name), new Document("$set", toDocument())),
                MongoExecutor.EXECUTOR
        ).exceptionally(ex -> {
            Logger.info(() -> "Failed to save player data for " + player.getName() + ": " + ex.getMessage());
            return null;
        });
    }



    public void setFirstJoin(boolean firstJoin) {
        this.firstJoin = firstJoin;

        CompletableFuture.runAsync(() ->
                        collection.updateOne(
                                Filters.eq("Name", name),
                                new Document("$set", new Document("firstJoin", firstJoin))
                        ),
                MongoExecutor.EXECUTOR
        ).exceptionally(ex -> {
            Logger.info(() -> "Failed to update firstJoin for " + name + ": " + ex.getMessage());
            return null;
        });
    }


    public void setLastSectorTransfer(boolean redirecting) {
        this.lastSectorTransfer = redirecting ? System.currentTimeMillis() : 0L;

        CompletableFuture.runAsync(() ->
                        collection.updateOne(
                                Filters.eq("Name", name),
                                new Document("$set", new Document("lastSectorTransfer", this.lastSectorTransfer))
                        ),
                MongoExecutor.EXECUTOR
        ).exceptionally(ex -> {
            Logger.info(() -> "Failed to update lastSectorTransfer for " + name + ": " + ex.getMessage());
            return null;
        });
    }


    public void setLastTransferTimestamp(long cooldown) {
        this.lastTransferTimestamp = cooldown;

        CompletableFuture.runAsync(() ->
                        collection.updateOne(
                                Filters.eq("Name", name),
                                new Document("$set", new Document("lastTransferTimestamp", this.lastTransferTimestamp))
                        ),
                MongoExecutor.EXECUTOR
        ).exceptionally(ex -> {
            Logger.info(() -> "Failed to update lastTransferTimestamp for " + name + ": " + ex.getMessage());
            return null;
        });
    }


    private Document toDocument() {
        return new Document()
                .append("Name", name)
                .append("sectorName", sectorName)
                .append("firstJoin", firstJoin)
                .append("lastSectorTransfer", lastSectorTransfer)
                .append("lastTransferTimestamp", lastTransferTimestamp)
                .append("teleportingToSector", teleportingToSector)
                .append("X", x)
                .append("Y", y)
                .append("Z", z)
                .append("Yaw", yaw)
                .append("Pitch", pitch)
                .append("playerGameMode", playerGameMode)
                .append("playerInventoryData", playerInventoryData)
                .append("playerEnderChestData", playerEnderChestData)
                .append("playerEffectsData", playerEffectsData)
                .append("foodLevel", foodLevel)
                .append("experience", experience)
                .append("experienceLevel", experienceLevel)
                .append("fireTicks", fireTicks)
                .append("allowFlight", allowFlight)
                .append("flying", flying);
    }

    public void applyPlayerData() {
        Player player = getPlayer();
        if (player == null) return;

        Sector current = PaperSector.getInstance().getSectorManager().getCurrentSector();
        if (current == null) return;

        switch (current.getType()) {
            case QUEUE -> handleQueueSector(player);
            case NETHER -> handleNetherSector(player);
            case SPAWN -> handleSpawnSector(player);
            default -> handleDefaultSector(player);
        }
    }

    private void loadPlayerData(@NonNull Player player) {
        loadPlayerGameMode(player);
        loadPlayerInventory(player);
        loadPlayerEnderChest(player);
        loadPlayerEffects(player);
        loadPlayerFoodLevel(player);
        loadPlayerExperience(player);
        loadPlayerFireTicks(player);
        loadPlayerFlight(player);
    }





    private void loadPlayerFoodLevel(@NonNull Player player) {
        player.setFoodLevel(foodLevel);
    }

    private void loadPlayerExperience(@NonNull Player player) {
        player.setTotalExperience(experience);
        player.setLevel(experienceLevel);
    }

    private void loadPlayerFireTicks(@NonNull Player player) {
        player.setFireTicks(fireTicks);
    }

    private void loadPlayerFlight(@NonNull Player player) {
        player.setAllowFlight(allowFlight);
        player.setFlying(flying);
    }

    private void loadPlayerGameMode(@NonNull Player player) {
        player.setGameMode(GameMode.valueOf(playerGameMode));
    }

    private void teleportPlayerToStoredLocation(@NonNull Player player) {
        Location loc = new Location(player.getWorld(), x, y, z, yaw, pitch);
        Vector direction = loc.getDirection();
        direction.setY(0).normalize().multiply(4);
        loc.add(direction);
        player.teleport(loc);
    }


    private void loadPlayerInventory(@NonNull Player player) {
        if (playerInventoryData == null || playerInventoryData.isEmpty()) return;
        player.getInventory().setContents(PlayerDataSerializer.deserializeItemStacksFromBase64(playerInventoryData));
    }

    private void loadPlayerEnderChest(@NonNull Player player) {
        if (playerEnderChestData == null || playerEnderChestData.isEmpty()) return;
        player.getEnderChest().setContents(PlayerDataSerializer.deserializeItemStacksFromBase64(playerEnderChestData));
    }

    private void loadPlayerEffects(@NonNull Player player) {
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
        List<PotionEffect> effects = PlayerDataSerializer.deserializeEffects(playerEffectsData);
        effects.forEach(player::addPotionEffect);
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(name);
    }

    private long getLongSafe(Document doc, String key) {
        Object val = doc.get(key);
        if (val == null) return 0L;
        if (val instanceof Number) return ((Number) val).longValue();
        return 0L;
    }

    private double getDoubleSafe(Document doc, String key) {
        Object val = doc.get(key);
        if (val == null) return 0.0;
        if (val instanceof Number) return ((Number) val).doubleValue();
        return 0.0;
    }

    private int getIntSafe(Document doc, String key, int def) {
        Object val = doc.get(key);
        if (val == null) return def;
        if (val instanceof Number) return ((Number) val).intValue();
        return def;
    }


    public void handleQueueSector(Player player) {
        Location targetLocation = new Location(player.getWorld(), 0, 70, 0);
        if (player.teleport(targetLocation)) { // teleport synchroniczny
            player.setGameMode(GameMode.ADVENTURE);
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (!online.equals(player)) online.hidePlayer(PaperSector.getInstance(), player);
                if (!online.equals(player)) player.hidePlayer(PaperSector.getInstance(), online);
            }
        }
    }

    private void handleNetherSector(Player player) {
        Location targetLocation = new Location(player.getWorld(), 0, 70, 0);
        if (player.teleport(targetLocation)) {
            loadPlayerData(player);
        }
    }

    private void handleSpawnSector(Player player) {
        Location targetLocation = new Location(player.getWorld(), 0, 70, 0);
        if (player.teleport(targetLocation)) {
            loadPlayerData(player);
        }
    }




    private void handleDefaultSector(Player player) {
        teleportPlayerToStoredLocation(player);
        loadPlayerData(player);
    }


}
