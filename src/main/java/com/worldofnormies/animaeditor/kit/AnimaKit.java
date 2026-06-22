package com.worldofnormies.animaeditor.kit;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AnimaKit — represents a single saved kit with all its item data.
 * Loaded from and saved to individual .yml files in the /Kits/ folder.
 */
public class AnimaKit {

    private String id;
    private String displayName;
    private String creator;
    private String createdAt;
    private List<String> description;
    private boolean publicKit;
    private boolean featured;

    // Item data
    private String material;
    private String itemName;
    private List<String> lore;
    private Map<String, Integer> enchantments;
    private List<String> flags;
    private boolean unbreakable;
    private boolean glow;
    private int customModelData;

    public AnimaKit() {
        lore = new ArrayList<>();
        enchantments = new HashMap<>();
        flags = new ArrayList<>();
        description = new ArrayList<>();
    }

    /**
     * Deserialize an AnimaKit from a FileConfiguration (loaded from a .yml file).
     */
    public static AnimaKit fromConfig(FileConfiguration cfg, File file) {
        AnimaKit kit = new AnimaKit();

        ConfigurationSection meta = cfg.getConfigurationSection("meta");
        if (meta == null) return null;

        kit.id = meta.getString("id", file.getName().replace(".yml", ""));
        kit.displayName = meta.getString("name", kit.id);
        kit.creator = meta.getString("creator", "Unknown");
        kit.createdAt = meta.getString("created_at", "");
        kit.description = meta.getStringList("description");
        kit.publicKit = meta.getBoolean("public", false);
        kit.featured = meta.getBoolean("featured", false);

        ConfigurationSection item = cfg.getConfigurationSection("item");
        if (item != null) {
            kit.material = item.getString("material", "STONE");
            kit.itemName = item.getString("name", "");
            kit.lore = item.getStringList("lore");
            kit.unbreakable = item.getBoolean("unbreakable", false);
            kit.glow = item.getBoolean("glow", false);
            kit.customModelData = item.getInt("custom_model_data", 0);
            kit.flags = item.getStringList("flags");

            ConfigurationSection enchSec = item.getConfigurationSection("enchantments");
            if (enchSec != null) {
                for (String key : enchSec.getKeys(false)) {
                    kit.enchantments.put(key, enchSec.getInt(key, 1));
                }
            }
        }

        return kit;
    }

    /**
     * Serialize this AnimaKit into a FileConfiguration for saving.
     */
    public void toConfig(FileConfiguration cfg) {
        cfg.set("meta.id", id);
        cfg.set("meta.name", displayName);
        cfg.set("meta.creator", creator);
        cfg.set("meta.created_at", createdAt != null ? createdAt :
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        cfg.set("meta.description", description);
        cfg.set("meta.public", publicKit);
        cfg.set("meta.featured", featured);

        cfg.set("item.material", material != null ? material : "STONE");
        cfg.set("item.name", itemName);
        cfg.set("item.lore", lore);
        cfg.set("item.unbreakable", unbreakable);
        cfg.set("item.glow", glow);
        cfg.set("item.custom_model_data", customModelData);
        cfg.set("item.flags", flags);

        for (Map.Entry<String, Integer> entry : enchantments.entrySet()) {
            cfg.set("item.enchantments." + entry.getKey(), entry.getValue());
        }
    }

    // ── Getters & Setters ──────────────────────────────────────

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getCreator() { return creator; }
    public void setCreator(String creator) { this.creator = creator; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public List<String> getDescription() { return description; }
    public void setDescription(List<String> description) { this.description = description; }

    public boolean isPublicKit() { return publicKit; }
    public void setPublicKit(boolean publicKit) { this.publicKit = publicKit; }

    public boolean isFeatured() { return featured; }
    public void setFeatured(boolean featured) { this.featured = featured; }

    public String getMaterial() { return material; }
    public void setMaterial(String material) { this.material = material; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public List<String> getLore() { return lore; }
    public void setLore(List<String> lore) { this.lore = lore; }

    public Map<String, Integer> getEnchantments() { return enchantments; }
    public void setEnchantments(Map<String, Integer> enchantments) { this.enchantments = enchantments; }

    public List<String> getFlags() { return flags; }
    public void setFlags(List<String> flags) { this.flags = flags; }

    public boolean isUnbreakable() { return unbreakable; }
    public void setUnbreakable(boolean unbreakable) { this.unbreakable = unbreakable; }

    public boolean isGlow() { return glow; }
    public void setGlow(boolean glow) { this.glow = glow; }

    public int getCustomModelData() { return customModelData; }
    public void setCustomModelData(int customModelData) { this.customModelData = customModelData; }
}
