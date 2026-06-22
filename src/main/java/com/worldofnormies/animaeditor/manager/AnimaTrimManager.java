package com.worldofnormies.animaeditor.manager;

import com.worldofnormies.animaeditor.AnimaEditorPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;

import java.util.ArrayList;
import java.util.List;

/**
 * AnimaTrimManager — apply or remove smithing-template armor trims on
 * any armor piece directly, without needing a smithing table or the
 * actual template/material items.
 *
 * Pattern/material are referenced by their vanilla key, e.g.
 * pattern "sentry", material "redstone".
 */
public class AnimaTrimManager {

    private final AnimaEditorPlugin plugin;

    public AnimaTrimManager(AnimaEditorPlugin plugin) {
        this.plugin = plugin;
    }

    public TrimPattern resolvePattern(String name) {
        return Registry.TRIM_PATTERN.get(NamespacedKey.minecraft(name.toLowerCase()));
    }

    public TrimMaterial resolveMaterial(String name) {
        return Registry.TRIM_MATERIAL.get(NamespacedKey.minecraft(name.toLowerCase()));
    }

    /**
     * Apply a trim to the item. The item must have ArmorMeta (helmet,
     * chestplate, leggings, boots — vanilla armor or modern equippable items).
     * Returns false if the item isn't armor or the pattern/material is invalid.
     */
    public boolean apply(ItemStack item, TrimPattern pattern, TrimMaterial material) {
        if (item == null || pattern == null || material == null) return false;
        if (!(item.getItemMeta() instanceof ArmorMeta armorMeta)) return false;
        armorMeta.setTrim(new ArmorTrim(material, pattern));
        item.setItemMeta(armorMeta);
        return true;
    }

    public boolean remove(ItemStack item) {
        if (item == null) return false;
        if (!(item.getItemMeta() instanceof ArmorMeta armorMeta)) return false;
        if (!armorMeta.hasTrim()) return false;
        armorMeta.setTrim(null);
        item.setItemMeta(armorMeta);
        return true;
    }

    public List<String> patternKeys() {
        List<String> keys = new ArrayList<>();
        for (TrimPattern p : Registry.TRIM_PATTERN) keys.add(p.getKey().getKey());
        return keys;
    }

    public List<String> materialKeys() {
        List<String> keys = new ArrayList<>();
        for (TrimMaterial m : Registry.TRIM_MATERIAL) keys.add(m.getKey().getKey());
        return keys;
    }
}
