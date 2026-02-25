package com.animaleconomy;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class AnimalEconomy extends JavaPlugin {
	
    private static AnimalEconomy instance;
    private Handlers handlers;
    private final static Map<UUID, Map<EntityType, Integer>> playerKills = new ConcurrentHashMap<>();
    private FileConfiguration data;
    private File dataFile;
    
    @Override
    public void onEnable() {
        instance = this;
        
        saveDefaultConfig();
        
        handlers = new Handlers(this);
        handlers.initialize();

        loadData();
        
        getCommand("ae").setExecutor(new Commands(this));
        getCommand("ae").setTabCompleter(new Commands(this));

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new Placeholders(this).register();
        }

        if (getServer().getPluginManager().getPlugin("Plan") != null) {
            new PlanAPI().initialize();
        }

        getLogger().info("AnimalEconomy Plugin Enabled!");
    }

    @Override
    public void onDisable() {
        if (handlers != null) {
        	handlers.shutdown();
        }
        shutdown();
        getLogger().info("AnimalEconomy Plugin Disabled!");
    }

    public static AnimalEconomy getInstance() {
        return instance;
    }
    
    public int getKills(UUID uuid, EntityType type) {
        return playerKills.getOrDefault(uuid, new HashMap<>()).getOrDefault(type, 0);
    }

    public void setKills(UUID uuid, EntityType type, int count) {
        playerKills.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>()).put(type, count);
    }

    public void addKills(UUID uuid, EntityType type, int count) {
        setKills(uuid, type, getKills(uuid, type) + count);
        saveDataAsync();
    }

    public void removeKills(UUID uuid, EntityType type, int count) {
        setKills(uuid, type, Math.max(0, getKills(uuid, type) - count));
        saveDataAsync();
    }
    
    public static Map<EntityType, Integer> getAllKills(UUID uuid) {
        return new HashMap<>(playerKills.getOrDefault(uuid, new HashMap<>()));
    }

    public static ItemStack getHead(EntityType type) {
        return MobHeads.getHead(type);
    }

    public static ItemStack getHead(Entity entity) {
        return MobHeads.getHead(entity.getType());
    }

    public static boolean hasHead(EntityType type) {
        return MobHeads.hasHead(type);
    }
    
    public void shutdown() {
        saveData();
        playerKills.clear();
    }

    private void loadData() {
        dataFile = new File(AnimalEconomy.getInstance().getDataFolder(), "db.yml");
        if (!dataFile.exists()) {
            dataFile.getParentFile().mkdirs();
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        data = YamlConfiguration.loadConfiguration(dataFile);

        for (String key : data.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                Map<EntityType, Integer> kills = new ConcurrentHashMap<>();

                for (String typeStr : data.getConfigurationSection(key).getKeys(false)) {
                    EntityType type = EntityType.valueOf(typeStr);
                    int count = data.getInt(key + "." + typeStr);
                    kills.put(type, count);
                }

                playerKills.put(uuid, kills);
            } catch (Exception e) {
                AnimalEconomy.getInstance().getLogger().warning("Failed to load data for: " + key);
            }
        }
    }

    private void saveDataAsync() {
        Bukkit.getScheduler().runTaskAsynchronously(AnimalEconomy.getInstance(), this::saveData);
    }

    private void saveData() {
        if (data == null) data = new YamlConfiguration();

        playerKills.forEach((uuid, kills) -> 
            kills.forEach((type, count) -> 
                data.set(uuid + "." + type.name(), count)));

        try {
            data.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}