package com.worldofnormies.animaeditor.kit;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * AnimaKit — represents a saved collection of items.
 */
public class AnimaKit {

    private String id;
    private String displayName;
    private String creator;
    private String createdAt;
    private List<String> description;
    private boolean publicKit;
    private boolean featured;

    private List<ItemStack> items;

    public AnimaKit() {
        items = new ArrayList<>();
        description = new ArrayList<>();
    }

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

        List<?> itemsList = cfg.getList("items");
        if (itemsList != null) {
            for (Object obj : itemsList) {
                if (obj instanceof ItemStack is) {
                    kit.items.add(is);
                }
            }
        }

        return kit;
    }

    public void toConfig(FileConfiguration cfg) {
        cfg.set("meta.id", id);
        cfg.set("meta.name", displayName);
        cfg.set("meta.creator", creator);
        cfg.set("meta.created_at", createdAt != null ? createdAt :
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        cfg.set("meta.description", description);
        cfg.set("meta.public", publicKit);
        cfg.set("meta.featured", featured);

        cfg.set("items", items);
    }

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

    public List<ItemStack> getItems() { return items; }
    public void setItems(List<ItemStack> items) { this.items = items; }

    // For GUI preview
    public String getMaterial() {
        if (items.isEmpty()) return "CHEST";
        return items.get(0).getType().name();
    }
}
