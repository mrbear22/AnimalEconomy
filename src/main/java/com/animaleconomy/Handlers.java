package com.animaleconomy;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.*;
import org.bukkit.metadata.*;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.*;

public class Handlers implements Listener {
    private static final long MIN_KILL_INTERVAL = 2000;
    private static final double MIN_DISTANCE = 2.0;
    private static final int MAX_KILLS_PER_MIN = 20;
    private static final long RESET_TIME = 60000;
    private static final int SPAWNER_RADIUS = 10;
    
    private static final Set<EntityType> SPAWNER_ALLOWED = Set.of(
        EntityType.BLAZE, EntityType.SILVERFISH, EntityType.CAVE_SPIDER
    );
    
    private static final String LAST_KILL_TIME = "ae_last_kill_time";
    private static final String LAST_KILL_LOC = "ae_last_kill_location";
    private static final String RECENT_KILLS = "ae_recent_kills";
    private static final String RESET_TIME_KEY = "ae_reset_time";
    private static final String SPAWNER_MOB = "ae_spawner";
    private static final String SPAWN_LOC = "ae_spawn_loc";
    private static final String SPAWN_TIME = "ae_spawn_time";

    private final AnimalEconomy economy;

    public Handlers(AnimalEconomy economy) {
        this.economy = economy;
    }

    public void initialize() {
        Bukkit.getPluginManager().registerEvents(this, AnimalEconomy.getInstance());
    }

    public void shutdown() {
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent e) {
        Entity entity = e.getEntity();
        Location loc = e.getLocation();
        
        entity.setMetadata(SPAWNER_MOB, new FixedMetadataValue(AnimalEconomy.getInstance(), isNearSpawner(loc)));
        entity.setMetadata(SPAWN_LOC, new FixedMetadataValue(AnimalEconomy.getInstance(), loc));
        entity.setMetadata(SPAWN_TIME, new FixedMetadataValue(AnimalEconomy.getInstance(), System.currentTimeMillis()));
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        if (!(e.getEntity().getKiller() instanceof Player p)) return;
        
        Entity entity = e.getEntity();
        EntityType type = entity.getType();
        
        if (isSpawnerMob(entity) && !SPAWNER_ALLOWED.contains(type)) {
            sendActionBar(p, "§cВбивство не зараховано: моб із спавнера");
            return;
        }
        
        if (!passesChecks(p, entity)) return;
        
        AnimalEconomyEvent event = new AnimalEconomyEvent(p, type);
        Bukkit.getPluginManager().callEvent(event);
        
        if (event.isCancelled()) return;
        
        type = event.getEntityType();
        updateStats(p, entity.getLocation());
        economy.addKills(p.getUniqueId(), type, 1);
    }

    private boolean isSpawnerMob(Entity entity) {
        return getMeta(entity, SPAWNER_MOB).map(MetadataValue::asBoolean).orElse(false);
    }

    private boolean isNearSpawner(Location loc) {
        for (int x = -SPAWNER_RADIUS; x <= SPAWNER_RADIUS; x++) {
            for (int y = -SPAWNER_RADIUS; y <= SPAWNER_RADIUS; y++) {
                for (int z = -SPAWNER_RADIUS; z <= SPAWNER_RADIUS; z++) {
                    if (loc.clone().add(x, y, z).getBlock().getType() == Material.SPAWNER) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean passesChecks(Player p, Entity entity) {
        Location loc = entity.getLocation();
        long now = System.currentTimeMillis();
        EntityType type = entity.getType();
        
        boolean fromSpawner = isSpawnerMob(entity);
        boolean allowed = SPAWNER_ALLOWED.contains(type);
        
        // Check 1: Time interval
        Optional<Long> lastTime = getMeta(p, LAST_KILL_TIME).map(MetadataValue::asLong);
        if (lastTime.isPresent()) {
            long interval = (fromSpawner && allowed) ? MIN_KILL_INTERVAL / 2 : MIN_KILL_INTERVAL;
            if (now - lastTime.get() < interval) {
                sendActionBar(p, "§cЗанадто швидко! Зачекайте " + (interval / 1000) + " секунд");
                return false;
            }
        }
        
        // Check 2: Distance
        if (!(fromSpawner && allowed)) {
            Optional<Location> lastLoc = getMeta(p, LAST_KILL_LOC).map(m -> (Location) m.value());
            if (lastLoc.isPresent() && lastLoc.get().getWorld().equals(loc.getWorld()) && 
                lastLoc.get().distance(loc) < MIN_DISTANCE) {
                sendActionBar(p, "§cПідозра на ферму! Змініть локацію");
                return false;
            }
        }
        
        // Check 3: Kills per minute
        int maxKills = (fromSpawner && allowed) ? MAX_KILLS_PER_MIN * 2 : MAX_KILLS_PER_MIN;
        if (!checkKillsPerMinute(p, type, maxKills)) {
            sendActionBar(p, "§cЗанадто багато вбивств за хвилину!");
            return false;
        }
        
        return true;
    }

    @SuppressWarnings("unchecked")
    private boolean checkKillsPerMinute(Player p, EntityType type, int max) {
        long now = System.currentTimeMillis();
        
        Optional<Long> lastReset = getMeta(p, RESET_TIME_KEY).map(MetadataValue::asLong);
        if (lastReset.isEmpty() || now - lastReset.get() > RESET_TIME) {
            p.removeMetadata(RECENT_KILLS, AnimalEconomy.getInstance());
            setMeta(p, RESET_TIME_KEY, now);
        }
        
        Map<EntityType, Integer> recent = getMeta(p, RECENT_KILLS)
            .map(m -> (Map<EntityType, Integer>) m.value())
            .orElse(new HashMap<>());
        
        int current = recent.getOrDefault(type, 0);
        if (current >= max) return false;
        
        recent.put(type, current + 1);
        setMeta(p, RECENT_KILLS, recent);
        
        return true;
    }

    private void updateStats(Player p, Location loc) {
        long now = System.currentTimeMillis();
        setMeta(p, LAST_KILL_TIME, now);
        setMeta(p, LAST_KILL_LOC, loc.clone());
    }

    private Optional<MetadataValue> getMeta(org.bukkit.metadata.Metadatable obj, String key) {
        List<MetadataValue> meta = obj.getMetadata(key);
        return meta.isEmpty() ? Optional.empty() : Optional.of(meta.get(0));
    }

    private void setMeta(org.bukkit.metadata.Metadatable obj, String key, Object value) {
        obj.setMetadata(key, new FixedMetadataValue(AnimalEconomy.getInstance(), value));
    }

    private void sendActionBar(Player p, String msg) {
        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(msg));
    }
}