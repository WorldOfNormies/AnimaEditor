package com.worldofnormies.animaeditor.manager;

import com.worldofnormies.animaeditor.AnimaEditorPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AnimaEnchantManager — apply/remove enchantments on any item at any
 * level, ignoring vanilla's level cap AND item-type restriction
 * (e.g. Sharpness on a pickaxe, Mending at level 99, etc).
 *
 * Enchantments are referenced by their vanilla key, e.g. "sharpness",
 * "mending", "unbreaking" — matches org.bukkit.Registry.ENCHANTMENT keys.
 */
public class AnimaEnchantManager {

    private final AnimaEditorPlugin plugin;

    public AnimaEnchantManager(AnimaEditorPlugin plugin) {
        this.plugin = plugin;
    }

    /** Resolve an enchantment by its plain key name, e.g. "sharpness". */
    public Enchantment resolve(String name) {
        NamespacedKey key = NamespacedKey.minecraft(name.toLowerCase().replace(" ", "_"));
        return Registry.ENCHANTMENT.get(key);
    }

    /**
     * Apply an enchantment at the given level, bypassing both the
     * vanilla max-level cap and the "this enchant can't go on this item"
     * restriction.
     */
    public boolean apply(ItemStack item, Enchantment enchant, int level) {
        if (item == null || enchant == null) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        meta.addEnchant(enchant, level, true);
        item.setItemMeta(meta);
        return true;
    }

    /** Remove a single enchantment from the item. */
    public boolean remove(ItemStack item, Enchantment enchant) {
        if (item == null || enchant == null) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        boolean had = meta.hasEnchant(enchant);
        meta.removeEnchant(enchant);
        item.setItemMeta(meta);
        return had;
    }

    /** Strip every enchantment from the item. */
    public void clear(ItemStack item) {
        if (item == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        new ArrayList<>(meta.getEnchants().keySet()).forEach(meta::removeEnchant);
        item.setItemMeta(meta);
    }

    /** All known enchant key names, e.g. for tab-completion. */
    public List<String> allKeys() {
        List<String> keys = new ArrayList<>();
        for (Enchantment ench : Registry.ENCHANTMENT) {
            keys.add(ench.getKey().getKey());
        }
        return keys;
    }

    public List<String> matching(String prefix) {
        String p = prefix.toLowerCase();
        return allKeys().stream().filter(k -> k.startsWith(p)).collect(Collectors.toList());
    }

    /** Checks against blacklisted_enchantments in Permissions.yml (admins bypass via anima.*). */
    public boolean isBlacklisted(String enchantKey) {
        List<String> blacklist = plugin.getConfigManager().getPermissionsConfig().getStringList("blacklisted_enchantments");
        return blacklist.stream().anyMatch(b -> b.equalsIgnoreCase(enchantKey));
    }
}
