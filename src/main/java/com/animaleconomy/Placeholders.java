package com.animaleconomy;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class Placeholders extends PlaceholderExpansion {

    private final AnimalEconomy plugin;

    public Placeholders(AnimalEconomy plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String getIdentifier() {
        return "ae";
    }

    @Override
    public String getAuthor() {
        return "mrbear22";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) {
            return "";
        }

        for (EntityType type : EntityType.values()) {
            if (identifier.equals(type.name().toLowerCase())) {
                return String.valueOf(plugin.getKills(player.getUniqueId(), type));
            }
        }

        return null;
    }
}

