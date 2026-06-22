package com.worldofnormies.animaeditor.manager;

import com.worldofnormies.animaeditor.AnimaEditorPlugin;
import com.worldofnormies.animaeditor.permission.PlayerPermissionData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * PermissionManager — loads per-player permission override files from
 * the /Permissions/ folder and manages them in memory.
 *
 * Each player has their own file: plugins/AnimaEditor/Permissions/<PlayerName>.yml
 */
public class PermissionManager {

    private final AnimaEditorPlugin plugin;
    private final Map<UUID, PlayerPermissionData> playerData = new HashMap<>();
    private File permissionsFolder;

    public PermissionManager(AnimaEditorPlugin plugin) {
        this.plugin = plugin;
        this.permissionsFolder = new File(plugin.getDataFolder(), "Permissions");
        if (!permissionsFolder.exists()) permissionsFolder.mkdirs();
    }

    /** Load all player permission files from /Permissions/. */
    public void loadAll() {
        playerData.clear();
        File[] files = permissionsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return;
        for (File file : files) {
            try {
                FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
                PlayerPermissionData data = PlayerPermissionData.fromConfig(cfg, file);
                if (data != null) {
                    playerData.put(data.getUuid(), data);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load permission file: " + file.getName() + " — " + e.getMessage());
            }
        }
        plugin.getLogger().info("Loaded " + playerData.size() + " player permission file(s).");
    }

    /** Save all player data to disk. */
    public void saveAll() {
        for (PlayerPermissionData data : playerData.values()) {
            savePlayer(data);
        }
    }

    /** Save a single player's data. */
    public void savePlayer(PlayerPermissionData data) {
        File file = new File(permissionsFolder, data.getPlayerName() + ".yml");
        FileConfiguration cfg = new YamlConfiguration();
        data.toConfig(cfg);
        try {
            cfg.save(file);
            if (plugin.getConfig().getBoolean("permissions_manager.log_changes", true)) {
                plugin.getLogger().info("Saved permissions for " + data.getPlayerName());
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save permissions for " + data.getPlayerName() + " — " + e.getMessage());
        }
    }

    /** Get or create permission data for a player. */
    public PlayerPermissionData getOrCreate(UUID uuid, String playerName) {
        return playerData.computeIfAbsent(uuid, k -> {
            PlayerPermissionData data = new PlayerPermissionData(uuid, playerName);
            data.setGroup("default");
            return data;
        });
    }

    public PlayerPermissionData getData(UUID uuid) {
        return playerData.get(uuid);
    }

    public boolean hasData(UUID uuid) {
        return playerData.containsKey(uuid);
    }

    /** Check if a player has a specific permission node override. */
    public boolean hasPermission(UUID uuid, String node) {
        PlayerPermissionData data = playerData.get(uuid);
        if (data == null) return false;
        return data.getOverrides().getOrDefault(node, false);
    }

    /** Get the group for a player (defaults to "default"). */
    public String getGroup(UUID uuid) {
        PlayerPermissionData data = playerData.get(uuid);
        return data != null ? data.getGroup() : "default";
    }

    /** Set a player's group and save. */
    public void setGroup(UUID uuid, String playerName, String group) {
        PlayerPermissionData data = getOrCreate(uuid, playerName);
        data.setGroup(group);
        savePlayer(data);
    }

    /** Set a permission override for a player. */
    public void setOverride(UUID uuid, String playerName, String node, boolean value) {
        PlayerPermissionData data = getOrCreate(uuid, playerName);
        data.getOverrides().put(node, value);
        savePlayer(data);
    }

    public Collection<PlayerPermissionData> getAllData() {
        return playerData.values();
    }

    public File getPermissionsFolder() { return permissionsFolder; }
}
