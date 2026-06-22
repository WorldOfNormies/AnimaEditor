package com.worldofnormies.animaeditor.manager;

import com.worldofnormies.animaeditor.AnimaEditorPlugin;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.Banner;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * AnimaBannerManager — add/clear banner patterns on shields and banner
 * items. Shields and banners both use BlockStateMeta wrapping a Banner
 * block-state, which is where pattern layers live.
 */
public class AnimaBannerManager {

    private final AnimaEditorPlugin plugin;

    public AnimaBannerManager(AnimaEditorPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isBannerCapable(ItemStack item) {
        if (item == null) return false;
        return item.getType() == Material.SHIELD || item.getType().name().endsWith("_BANNER");
    }

    public PatternType resolvePattern(String name) {
        return Registry.BANNER_PATTERN.get(NamespacedKey.minecraft(name.toLowerCase()));
    }

    public DyeColor resolveColor(String name) {
        try {
            return DyeColor.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /** Add one pattern layer (on top of existing layers). */
    public boolean addPattern(ItemStack item, PatternType type, DyeColor color) {
        if (!isBannerCapable(item) || type == null || color == null) return false;
        if (!(item.getItemMeta() instanceof BlockStateMeta blockMeta)) return false;
        if (!(blockMeta.getBlockState() instanceof Banner banner)) return false;

        banner.addPattern(new Pattern(color, type));
        banner.update();
        blockMeta.setBlockState(banner);
        item.setItemMeta(blockMeta);
        return true;
    }

    /** Remove the most recently added pattern layer. */
    public boolean removeLastPattern(ItemStack item) {
        if (!isBannerCapable(item)) return false;
        if (!(item.getItemMeta() instanceof BlockStateMeta blockMeta)) return false;
        if (!(blockMeta.getBlockState() instanceof Banner banner)) return false;
        List<Pattern> patterns = banner.getPatterns();
        if (patterns.isEmpty()) return false;
        banner.removePattern(patterns.size() - 1);
        banner.update();
        blockMeta.setBlockState(banner);
        item.setItemMeta(blockMeta);
        return true;
    }

    /** Wipe all pattern layers. */
    public boolean clearPatterns(ItemStack item) {
        if (!isBannerCapable(item)) return false;
        if (!(item.getItemMeta() instanceof BlockStateMeta blockMeta)) return false;
        if (!(blockMeta.getBlockState() instanceof Banner banner)) return false;
        banner.setPatterns(new ArrayList<>());
        banner.update();
        blockMeta.setBlockState(banner);
        item.setItemMeta(blockMeta);
        return true;
    }

    public List<String> patternKeys() {
        List<String> keys = new ArrayList<>();
        for (PatternType p : Registry.BANNER_PATTERN) keys.add(p.getKey().getKey());
        return keys;
    }
}
