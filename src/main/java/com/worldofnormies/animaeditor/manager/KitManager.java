package com.worldofnormies.animaeditor.manager;

import com.worldofnormies.animaeditor.AnimaEditorPlugin;
import com.worldofnormies.animaeditor.kit.AnimaKit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * KitManager — loads all .yml files from the /Kits/ folder,
 * keeps them in memory, and handles save/delete operations.
 *
 * Each kit is stored as an individual file:
 *   plugins/AnimaEditor/Kits/<kit-id>.yml
 */
public class KitManager {

    private final AnimaEditorPlugin plugin;
    private final Map<String, AnimaKit> kits = new HashMap<>();
    private File kitsFolder;

    public KitManager(AnimaEditorPlugin plugin) {
        this.plugin = plugin;
        this.kitsFolder = new File(plugin.getDataFolder(), "Kits");
        if (!kitsFolder.exists()) kitsFolder.mkdirs();
    }

    /** Load all kit files from the /Kits/ folder. */
    public void loadAll() {
        kits.clear();
        File[] files = kitsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return;
        for (File file : files) {
            try {
                FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
                AnimaKit kit = AnimaKit.fromConfig(cfg, file);
                if (kit != null) {
                    kits.put(kit.getId().toLowerCase(), kit);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load kit file: " + file.getName() + " - " + e.getMessage());
            }
        }
        plugin.getLogger().info("Loaded " + kits.size() + " kit(s) from /Kits/ folder.");
    }

    /** Save all kits to disk. */
    public void saveAll() {
        for (AnimaKit kit : kits.values()) {
            saveKit(kit);
        }
    }

    /** Save a single kit. */
    public void saveKit(AnimaKit kit) {
        File file = new File(kitsFolder, kit.getId() + ".yml");
        FileConfiguration cfg = new YamlConfiguration();
        kit.toConfig(cfg);
        try {
            cfg.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save kit: " + kit.getId() + " - " + e.getMessage());
        }
    }

    /** Register and save a new kit. */
    public void addKit(AnimaKit kit) {
        kits.put(kit.getId().toLowerCase(), kit);
        saveKit(kit);
    }

    /** Delete a kit from memory and disk. */
    public boolean deleteKit(String id) {
        String key = id.toLowerCase();
        AnimaKit kit = kits.remove(key);
        if (kit == null) return false;
        File file = new File(kitsFolder, key + ".yml");
        if (file.exists()) file.delete();
        return true;
    }

    public AnimaKit getKit(String id) {
        return kits.get(id.toLowerCase());
    }

    public boolean hasKit(String id) {
        return kits.containsKey(id.toLowerCase());
    }

    public Collection<AnimaKit> getAllKits() {
        return kits.values();
    }

    public File getKitsFolder() { return kitsFolder; }
}
