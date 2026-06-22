package com.worldofnormies.animaeditor.permission;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * PlayerPermissionData — holds a single player's permission group,
 * overrides, and limit overrides.
 * Serialized to/from Permissions/<PlayerName>.yml
 */
public class PlayerPermissionData {

    private UUID uuid;
    private String playerName;
    private String group;

    /** node → true (granted) / false (denied) */
    private Map<String, Boolean> overrides = new HashMap<>();

    /** limit key → integer value override */
    private Map<String, Object> limitOverrides = new HashMap<>();

    /** kit id → last used timestamp (epoch ms) */
    private Map<String, Long> kitCooldowns = new HashMap<>();

    public PlayerPermissionData(UUID uuid, String playerName) {
        this.uuid = uuid;
        this.playerName = playerName;
        this.group = "default";
    }

    // ── Serialization ─────────────────────────────────────────

    public static PlayerPermissionData fromConfig(FileConfiguration cfg, File file) {
        String uuidStr = cfg.getString("player.uuid", null);
        String name = cfg.getString("player.name", file.getName().replace(".yml", ""));
        if (uuidStr == null) return null;

        UUID uuid;
        try {
            uuid = UUID.fromString(uuidStr);
        } catch (IllegalArgumentException e) {
            return null;
        }

        PlayerPermissionData data = new PlayerPermissionData(uuid, name);
        data.group = cfg.getString("player.group", "default");

        ConfigurationSection overrideSec = cfg.getConfigurationSection("overrides");
        if (overrideSec != null) {
            for (String key : overrideSec.getKeys(false)) {
                data.overrides.put(key, overrideSec.getBoolean(key, false));
            }
        }

        ConfigurationSection limitSec = cfg.getConfigurationSection("limit_overrides");
        if (limitSec != null) {
            for (String key : limitSec.getKeys(false)) {
                data.limitOverrides.put(key, limitSec.get(key));
            }
        }

        ConfigurationSection cooldownSec = cfg.getConfigurationSection("kit_cooldowns");
        if (cooldownSec != null) {
            for (String key : cooldownSec.getKeys(false)) {
                data.kitCooldowns.put(key, cooldownSec.getLong(key, 0L));
            }
        }

        return data;
    }

    public void toConfig(FileConfiguration cfg) {
        cfg.set("player.name", playerName);
        cfg.set("player.uuid", uuid.toString());
        cfg.set("player.group", group);

        // Clear and rewrite overrides
        cfg.set("overrides", null);
        for (Map.Entry<String, Boolean> entry : overrides.entrySet()) {
            cfg.set("overrides." + entry.getKey(), entry.getValue());
        }

        // Limit overrides
        cfg.set("limit_overrides", null);
        for (Map.Entry<String, Object> entry : limitOverrides.entrySet()) {
            cfg.set("limit_overrides." + entry.getKey(), entry.getValue());
        }

        // Cooldowns
        cfg.set("kit_cooldowns", null);
        for (Map.Entry<String, Long> entry : kitCooldowns.entrySet()) {
            cfg.set("kit_cooldowns." + entry.getKey(), entry.getValue());
        }
    }

    // ── Getters & Setters ──────────────────────────────────────

    public UUID getUuid() { return uuid; }
    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public String getGroup() { return group; }
    public void setGroup(String group) { this.group = group; }

    public Map<String, Boolean> getOverrides() { return overrides; }
    public Map<String, Object> getLimitOverrides() { return limitOverrides; }
    public Map<String, Long> getKitCooldowns() { return kitCooldowns; }

    public boolean hasOverride(String node) { return overrides.containsKey(node); }
    public boolean getOverride(String node, boolean def) { return overrides.getOrDefault(node, def); }

    public void setKitCooldown(String kitId, long timestamp) { kitCooldowns.put(kitId, timestamp); }
    public long getKitCooldown(String kitId) { return kitCooldowns.getOrDefault(kitId, 0L); }
}
