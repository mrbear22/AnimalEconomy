package com.animaleconomy;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
public class Commands implements CommandExecutor, TabCompleter {
    private final AnimalEconomy plugin;
    public Commands(AnimalEconomy plugin) {
        this.plugin = plugin;
    }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> cmds = new ArrayList<>(List.of("pay"));
            if (sender.hasPermission("animaleconomy.admin")) cmds.addAll(List.of("add", "remove", "set"));
            return cmds;
        }
        else if (args.length == 2) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
        }
        else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("pay")) return List.of();
            return Arrays.stream(EntityType.values()).map(EntityType::name).map(String::toLowerCase).collect(Collectors.toList());
        }
        return List.of();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 4) {
            sender.sendMessage("Використання: /ae <add|remove|set|pay> <player> <mob> <кількість>");
            return false;
        }

        String action = args[0].toLowerCase();
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
        if (player == null) { sender.sendMessage("Гравця не знайдено."); return false; }

        EntityType entityType;
        try {
            entityType = EntityType.valueOf(mobType.toUpperCase());
        } catch (IllegalArgumentException e) {
            sender.sendMessage("Невідомий тип мобу.");
            return false;
        }

        switch (action) {
            case "add":
                if (!sender.hasPermission("animaleconomy.admin")) { sender.sendMessage("У вас немає прав для цієї команди."); return false; }
                plugin.addKills(player.getUniqueId(), entityType, count);
                sender.sendMessage("Додано " + count + " вбивств для " + playerName + ".");
                break;
            case "remove":
                if (!sender.hasPermission("animaleconomy.admin")) { sender.sendMessage("У вас немає прав для цієї команди."); return false; }
                plugin.removeKills(player.getUniqueId(), entityType, count);
                sender.sendMessage("Знято " + count + " вбивств для " + playerName + ".");
                break;
            case "set":
                if (!sender.hasPermission("animaleconomy.admin")) { sender.sendMessage("У вас немає прав для цієї команди."); return false; }
                plugin.setKills(player.getUniqueId(), entityType, count);
                sender.sendMessage("Встановлено " + count + " вбивств для " + playerName + ".");
                break;
            case "pay":
                if (!(sender instanceof Player payer)) { sender.sendMessage("Тільки гравці можуть використовувати цю команду."); return false; }
                if (count <= 0) { sender.sendMessage("Кількість має бути більше нуля."); return false; }
                if (player.equals(payer)) { sender.sendMessage("Не можна передавати голови самому собі."); return false; }
                int current = plugin.getKills(payer.getUniqueId(), entityType);
                if (current < count) { sender.sendMessage("Недостатньо голів. У вас є: " + current); return false; }
                plugin.removeKills(payer.getUniqueId(), entityType, count);
                plugin.addKills(player.getUniqueId(), entityType, count);
                sender.sendMessage("Ви передали " + count + "x " + entityType.name().toLowerCase() + " гравцю " + player.getName() + ".");
                player.sendMessage(payer.getName() + " передав вам " + count + "x " + entityType.name().toLowerCase() + ".");
                break;
            default:
                sender.sendMessage("Невідома команда. Використання: /ae <add|remove|set|pay> <player> <mob> <кількість>");
                return false;
        }
        return true;
    }
}