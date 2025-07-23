package com.animaleconomy;

import org.bukkit.plugin.java.JavaPlugin;

public class Brain extends JavaPlugin {

    private AnimalEconomy economy;
    private static Brain instace;

    @Override
    public void onEnable() {
    	
    	instace = this;
        economy = new AnimalEconomy();
        
        getServer().getPluginManager().registerEvents(new AnimalEconomy(), this);

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
        getLogger().info("AnimalEconomy Plugin Disabled!");
    }

    public AnimalEconomy getAnimalEconomy() {
        return economy;
    }

	public static Brain getInstance() {
		return instace;
	}
}
