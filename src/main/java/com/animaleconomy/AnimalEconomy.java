package com.animaleconomy;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import java.io.File;
import java.io.IOException;
import java.util.*;


public class AnimalEconomy implements Listener {

    private final File dataFile;
    private FileConfiguration dataConfig;
    private static final Map<UUID, Map<EntityType, Integer>> playerKills = new HashMap<>();
    
    private static final long MIN_KILL_INTERVAL = 2000;
    private static final double MIN_DISTANCE_BETWEEN_KILLS = 2.0;
    private static final int MAX_KILLS_PER_MINUTE = 20;
    private static final long RECENT_KILLS_RESET_TIME = 60000;
    private static final int SPAWNER_CHECK_RADIUS = 10;
    
    private static final Set<EntityType> SPAWNER_ALLOWED_MOBS = new HashSet<>(Arrays.asList(
        EntityType.BLAZE,
        EntityType.SILVERFISH,
        EntityType.CAVE_SPIDER
    ));
    
    private static final String PLAYER_LAST_KILL_TIME = "last_kill_time";
    private static final String PLAYER_LAST_KILL_LOCATION = "last_kill_location";
    private static final String PLAYER_RECENT_KILLS = "recent_kills_count";
    private static final String PLAYER_RECENT_KILLS_RESET = "recent_kills_reset_time";
    
    private static final String SPAWNER_MOB_KEY = "spawner_mob";
    private static final String NATURAL_MOB_KEY = "natural_mob";
    private static final String SPAWN_LOCATION_KEY = "spawn_location";
    private static final String SPAWN_TIME_KEY = "spawn_time";

    public AnimalEconomy() {
        dataFile = new File(Brain.getInstance().getDataFolder(), "db.yml");
        loadData();
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        Entity entity = event.getEntity();
        Location spawnLocation = event.getLocation();
        
        boolean isFromSpawner = isNearSpawner(spawnLocation);
        
        if (isFromSpawner) {
            entity.setMetadata(SPAWNER_MOB_KEY, new FixedMetadataValue(Brain.getInstance(), true));
        } else {
            entity.setMetadata(NATURAL_MOB_KEY, new FixedMetadataValue(Brain.getInstance(), true));
        }
        
        entity.setMetadata(SPAWN_LOCATION_KEY, new FixedMetadataValue(Brain.getInstance(), spawnLocation));
        entity.setMetadata(SPAWN_TIME_KEY, new FixedMetadataValue(Brain.getInstance(), System.currentTimeMillis()));
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!(event.getEntity().getKiller() instanceof Player)) {
            return;
        }
        
        Player player = event.getEntity().getKiller();
        UUID playerId = player.getUniqueId();
        Entity entity = event.getEntity();
        EntityType entityType = entity.getType();
        
        if (isSpawnerMob(entity) && !isSpawnerAllowed(entityType)) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, 
                new TextComponent("§cВбивство не зараховано: моб із спавнера"));
            return;
        }
        
        if (!passesAntiFarmChecks(player, entity)) {
            return;
        }
        
        AnimalEconomyEvent killEvent = new AnimalEconomyEvent(player, entityType);
        Bukkit.getPluginManager().callEvent(killEvent);
        
        if (killEvent.isCancelled()) {
            return;
        }

        entityType = killEvent.getEntityType();
        
        updateAntiFarmStats(player, entity.getLocation());

        playerKills.putIfAbsent(playerId, new HashMap<>());
        Map<EntityType, Integer> kills = playerKills.get(playerId);
        kills.put(entityType, kills.getOrDefault(entityType, 0) + 1);
        
        saveData();
    }

    private boolean isSpawnerMob(Entity entity) {
        List<MetadataValue> spawnerMeta = entity.getMetadata(SPAWNER_MOB_KEY);
        return !spawnerMeta.isEmpty() && spawnerMeta.get(0).asBoolean();
    }
    
    private boolean isSpawnerAllowed(EntityType entityType) {
        return SPAWNER_ALLOWED_MOBS.contains(entityType);
    }

    private boolean isNearSpawner(Location location) {
        for (int x = -SPAWNER_CHECK_RADIUS; x <= SPAWNER_CHECK_RADIUS; x++) {
            for (int y = -SPAWNER_CHECK_RADIUS; y <= SPAWNER_CHECK_RADIUS; y++) {
                for (int z = -SPAWNER_CHECK_RADIUS; z <= SPAWNER_CHECK_RADIUS; z++) {
                    Block block = location.clone().add(x, y, z).getBlock();
                    if (block.getType() == Material.SPAWNER) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean passesAntiFarmChecks(Player player, Entity entity) {
        Location currentLocation = entity.getLocation();
        long currentTime = System.currentTimeMillis();
        EntityType entityType = entity.getType();
        
        boolean isFromSpawner = isSpawnerMob(entity);
        boolean isSpawnerAllowed = isSpawnerAllowed(entityType);
        
        // Перевірка 1: Мінімальний інтервал між вбивствами
        List<MetadataValue> lastKillTimeMeta = player.getMetadata(PLAYER_LAST_KILL_TIME);
        if (!lastKillTimeMeta.isEmpty()) {
            long lastTime = lastKillTimeMeta.get(0).asLong();
            long minInterval = (isFromSpawner && isSpawnerAllowed) ? MIN_KILL_INTERVAL / 2 : MIN_KILL_INTERVAL;
            if (currentTime - lastTime < minInterval) {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, 
                    new TextComponent("§cЗанадто швидко! Зачекайте " + (minInterval / 1000) + " секунд"));
                return false;
            }
        }
        
        // Перевірка 2: Мінімальна відстань між вбивствами (не застосовується для дозволених спавнер-мобів)
        if (!(isFromSpawner && isSpawnerAllowed)) {
            List<MetadataValue> lastKillLocationMeta = player.getMetadata(PLAYER_LAST_KILL_LOCATION);
            if (!lastKillLocationMeta.isEmpty()) {
                Location lastLocation = (Location) lastKillLocationMeta.get(0).value();
                if (lastLocation != null && lastLocation.getWorld().equals(currentLocation.getWorld()) && 
                    lastLocation.distance(currentLocation) < MIN_DISTANCE_BETWEEN_KILLS) {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, 
                        new TextComponent("§cПідозра на ферму! Змініть локацію"));
                    return false;
                }
            }
        }
        
        // Перевірка 3: Максимальна кількість вбивств за хвилину (збільшена для дозволених спавнер-мобів)
        int maxKills = (isFromSpawner && isSpawnerAllowed) ? MAX_KILLS_PER_MINUTE * 2 : MAX_KILLS_PER_MINUTE;
        if (!checkKillsPerMinute(player, entity.getType(), maxKills)) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, 
                new TextComponent("§cЗанадто багато вбивств за хвилину! Зачекайте"));
            return false;
        }
        
        /*// Перевірка 4: Час існування моба (скорочений для дозволених спавнер-мобів)
        List<MetadataValue> spawnTimeMeta = entity.getMetadata(SPAWN_TIME_KEY);
        if (!spawnTimeMeta.isEmpty()) {
            long spawnTime = spawnTimeMeta.get(0).asLong();
            long minAge = (isFromSpawner && isSpawnerAllowed) ? 30000 : 5000; // 5 секунд для спавнер-мобів, 30 для інших
            if (currentTime - spawnTime < minAge) {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, 
                    new TextComponent("§cМоб щойно заспавнився! Зачекайте"));
                return false;
            }
        }
        
        // Перевірка 5: Моб не повинен бути в одному чанку з гравцем довго (не застосовується для дозволених спавнер-мобів)
        if (!(isFromSpawner && isSpawnerAllowed)) {
            List<MetadataValue> spawnLocationMeta = entity.getMetadata(SPAWN_LOCATION_KEY);
            if (!spawnLocationMeta.isEmpty()) {
                Location spawnLocation = (Location) spawnLocationMeta.get(0).value();
                if (spawnLocation != null && spawnLocation.getChunk().equals(player.getLocation().getChunk())) {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, 
                        new TextComponent("§cПідозрілий спавн моба в тому ж чанку!"));
                    //return false;
                }
            }
        }
        */
        return true;
    }

    @SuppressWarnings("unchecked")
    private boolean checkKillsPerMinute(Player player, EntityType entityType, int maxKills) {
        long currentTime = System.currentTimeMillis();
        
        List<MetadataValue> lastResetMeta = player.getMetadata(PLAYER_RECENT_KILLS_RESET);
        if (!lastResetMeta.isEmpty()) {
            long lastReset = lastResetMeta.get(0).asLong();
            if (currentTime - lastReset > RECENT_KILLS_RESET_TIME) {
                player.removeMetadata(PLAYER_RECENT_KILLS, Brain.getInstance());
                player.setMetadata(PLAYER_RECENT_KILLS_RESET, new FixedMetadataValue(Brain.getInstance(), currentTime));
            }
        } else {
            player.setMetadata(PLAYER_RECENT_KILLS_RESET, new FixedMetadataValue(Brain.getInstance(), currentTime));
        }
        
        Map<EntityType, Integer> playerRecentKills = new HashMap<>();
        List<MetadataValue> recentKillsMeta = player.getMetadata(PLAYER_RECENT_KILLS);
        if (!recentKillsMeta.isEmpty()) {
            playerRecentKills = (Map<EntityType, Integer>) recentKillsMeta.get(0).value();
        }
        
        int currentKills = playerRecentKills.getOrDefault(entityType, 0);
        
        if (currentKills >= maxKills) {
            return false;
        }
        
        playerRecentKills.put(entityType, currentKills + 1);
        player.setMetadata(PLAYER_RECENT_KILLS, new FixedMetadataValue(Brain.getInstance(), playerRecentKills));
        
        return true;
    }

    private void updateAntiFarmStats(Player player, Location location) {
        long currentTime = System.currentTimeMillis();
        player.setMetadata(PLAYER_LAST_KILL_TIME, new FixedMetadataValue(Brain.getInstance(), currentTime));
        player.setMetadata(PLAYER_LAST_KILL_LOCATION, new FixedMetadataValue(Brain.getInstance(), location.clone()));
    }

    public int getKills(UUID playerId, EntityType entityType) {
        return playerKills.getOrDefault(playerId, new HashMap<>()).getOrDefault(entityType, 0);
    }

    public void setKills(UUID playerId, EntityType entityType, int count) {
        playerKills.putIfAbsent(playerId, new HashMap<>());
        playerKills.get(playerId).put(entityType, count);
        saveData();
    }

    public void addKills(UUID playerId, EntityType entityType, int count) {
        int currentKills = getKills(playerId, entityType);
        setKills(playerId, entityType, currentKills + count);
    }

    public void removeKills(UUID playerId, EntityType entityType, int count) {
        int currentKills = getKills(playerId, entityType);
        setKills(playerId, entityType, Math.max(0, currentKills - count));
    }
    
    public Map<EntityType, Integer> getAllKills(UUID playerId) {
        return playerKills.getOrDefault(playerId, new HashMap<>());
    }

    private void loadData() {
        if (!dataFile.exists()) {
            return;
        }

        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        for (String playerId : dataConfig.getKeys(false)) {
            UUID uuid = UUID.fromString(playerId);
            Map<EntityType, Integer> kills = new HashMap<>();

            for (String entityType : dataConfig.getConfigurationSection(playerId).getKeys(false)) {
                EntityType type = EntityType.valueOf(entityType);
                int count = dataConfig.getInt(playerId + "." + entityType);
                kills.put(type, count);
            }

            playerKills.put(uuid, kills);
        }
    }

    private void saveData() {
        if (dataConfig == null) {
            dataConfig = new YamlConfiguration();
        }

        for (UUID playerId : playerKills.keySet()) {
            for (EntityType entityType : playerKills.get(playerId).keySet()) {
                int count = playerKills.get(playerId).get(entityType);
                dataConfig.set(playerId.toString() + "." + entityType.name(), count);
            }
        }

        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}