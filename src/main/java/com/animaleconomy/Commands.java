package com.animaleconomy;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class Commands implements CommandExecutor, TabCompleter {

    private final AnimalEconomy plugin;

    public Commands(AnimalEconomy plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("add", "remove", "set");
        }
        else if (args.length == 2) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.toList());
        }
        else if (args.length == 3) {
            return Arrays.stream(EntityType.values())
                    .map(EntityType::name)
                    .map(String::toLowerCase)
                    .collect(Collectors.toList());
        }
        return List.of();
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 4) {
            sender.sendMessage("Використання: /ae <add|remove|set> <player> <mob> <кількість>");
            return false;
        }

        String action = args[0];
        String playerName = args[1];
        String mobType = args[2];
        int count;

        try {
            count = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage("Кількість має бути числом.");
            return false;
        }

        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            sender.sendMessage("Гравця не знайдено.");
            return false;
        }

        UUID playerId = player.getUniqueId();
        EntityType entityType;

        try {
            entityType = EntityType.valueOf(mobType.toUpperCase());
        } catch (IllegalArgumentException e) {
            sender.sendMessage("Невідомий тип мобу.");
            return false;
        }

        switch (action.toLowerCase()) {
            case "add":
            	plugin.addKills(playerId, entityType, count);
                sender.sendMessage("Додано " + count + " вбивств для " + playerName + ".");
                break;

            case "remove":
                plugin.removeKills(playerId, entityType, count);
                sender.sendMessage("Знято " + count + " вбивств для " + playerName + ".");
                break;

            case "set":
            	plugin.setKills(playerId, entityType, count);
                sender.sendMessage("Встановлено " + count + " вбивств для " + playerName + ".");
                break;

            default:
                sender.sendMessage("Невідома команда. Використання: /ae <add|remove|set> <player> <mob> <кількість>");
                return false;
        }

        return true;
    }
}

