package com.animaleconomy;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

public class MobHeads {

    private static final Map<EntityType, String> TEXTURES = new EnumMap<>(EntityType.class);

    static {
        TEXTURES.put(EntityType.ZOMBIE,
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWMyMzJkMDY4M2JhMWUzNjI1OTNmYTFhOTdkNzQ0YzI5MGIxNjU1ZDcyYjIyNzc5ODhhNmM4NWE5Mzg1NDI1NSJ9fX0=");
        TEXTURES.put(EntityType.SKELETON,
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzA1YmQ3NDY3YjA1ZmQyYWMyNTQ1NTNjYmY2NDJiNGNhNWMzYmU3Nzg0YTgzZDY4YjdhZDVhMWM2YTFkYjFjZiJ9fX0=");
        TEXTURES.put(EntityType.CREEPER,
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDY4NGIyNWViMmRiYTc2NjMxMzgzMmIzMGRhZGFiYjE4YTI3NjgxZWVkMTI5OTYwNGZlYmZlNDY5ZTcyMTdhZSJ9fX0=");
        TEXTURES.put(EntityType.PHANTOM,
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjRhZDYzYjY5N2E0YzQ3OTBkMDBjNDM1NDYwYmFmNDkxOTE2NTdlNjFiZWU2MTFmNzU4OGRiY2RhNzE5OGJiZCJ9fX0=");
        TEXTURES.put(EntityType.SPIDER,
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTU4NzFjMjJiODFjMTJlNjdmNWFlYmQ5YWZlMDk1OGI4MWNhZGE2MzA1YzA3NTk5YTA3YjAxYWIxMjZiYTJjNCJ9fX0=");
        TEXTURES.put(EntityType.ENDERMAN,
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGYyNDc2N2M4MTM4YjNkZmVjMDJmNzdiZDE1MTk5NGQ0ODBkNGU4Njk2NjRjZTA5YTI2YjE5Mjg5MjEyMTYyYiJ9fX0=");
        TEXTURES.put(EntityType.WITHER_SKELETON,
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWU0ZDIwNGViYzI0MmVjYTIxNDhmNTg1M2UzYWYwMGY4NGYwZDY3NDA5OWRjMzk0ZjZkMjkyNGIyNDBjYTJlMyJ9fX0=");
        TEXTURES.put(EntityType.ZOMBIFIED_PIGLIN,
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTkzNTg0MmFmNzY5MzgwZjc4ZThiOGE4OGQxZWE2Y2EyODA3YzFlNTY5M2MyY2Y3OTc0NTY2MjA4MzNlOTM2ZiJ9fX0=");
        TEXTURES.put(EntityType.PIGLIN,
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTA1MzFiODJmYTY5ODRjMzAwNjgzZTcwZDY5NGYyM2JkNDAxNmYzZWMyMzY4MTQ2MGFjZmEyZTcxMjg2ZmM5ZSJ9fX0=");
        TEXTURES.put(EntityType.GHAST,
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjRhYjhhMjJlNzY4N2NjNGM3OGYzYjZmZjViMWViMDQ5MTdiNTFjZDNjZDdkYmNlMzYxNzExNjBiM2M3N2NlZCJ9fX0=");
        TEXTURES.put(EntityType.BLAZE,
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzM3NjIzZjc5ZjdlYjRmM2Y4MGRhNjViNjUyY2M0NGIyMTQ4ZWVhNDFmOWZmZTJlODZhMjNiZGY0OWFiNzdiMSJ9fX0=");
        TEXTURES.put(EntityType.SHULKER,
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjVhYmY5MTUxNzYwYzkzOTVmYWU0ZjNkMjUxN2JkZjU3ODM3ODE1ZDI1NDUzNzg3MDU5NjI3MGNiN2ZmMTk2In19fQ==");
        TEXTURES.put(EntityType.ENDER_DRAGON,
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmViMTU3ZGQ5NzM2ZDg4OTc0YmEwYjQwOGE3NTQ3ZDQzMDE5Y2VjYmI5YzM1MjUxM2MyNjg3NmU4NmE1MmU3OCJ9fX0=");
        TEXTURES.put(EntityType.GUARDIAN,
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2FjMTdkN2Q1ZmQyY2VhZjk5M2Y0Y2RjY2Y0MGIxMDQyOWFhMjE4ZTFkZGEzYjcyNGNjZmJiNzBmZGZkNTZhZiJ9fX0=");
        TEXTURES.put(EntityType.SLIME,
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzk4NjY1YmYxNzAyYzY4ZGI5MzlmN2JmZTg4NTAyMWZiNzNhMDgxN2Q5MWFmNDM4MzAyOTNmNzE2MmE1ZjkwMSJ9fX0=");
    }

    public static ItemStack getHead(EntityType type) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        String texture = TEXTURES.get(type);
        if (texture == null) return skull;

        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        if (meta == null) return skull;

        applyTexture(meta, texture);
        skull.setItemMeta(meta);
        return skull;
    }

    public static ItemStack getHead(Entity entity) {
        return getHead(entity.getType());
    }

    public static boolean hasHead(EntityType type) {
        return TEXTURES.containsKey(type);
    }

    private static void applyTexture(SkullMeta meta, String base64) {
        try {
            GameProfile profile = new GameProfile(UUID.randomUUID(), "mob_head");
            profile.getProperties().put("textures", new Property("textures", base64));

            Field profileField = meta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(meta, profile);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            AnimalEconomy.getInstance().getLogger().warning(
                    "MobHeads: не вдалось застосувати текстуру — " + e.getMessage());
        }
    }
}