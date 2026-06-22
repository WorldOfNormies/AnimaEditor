package com.worldofnormies.animaeditor.manager;

import com.worldofnormies.animaeditor.AnimaEditorPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class ConfigManager {

    private final AnimaEditorPlugin plugin;
    private FileConfiguration guiConfig;
    private FileConfiguration kitsConfig;
    private FileConfiguration permissionsConfig;
    private FileConfiguration commandsConfig;

    public ConfigManager(AnimaEditorPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        guiConfig         = loadFile("AnimaEditorGUI.yml");
        kitsConfig        = loadFile("kits.yml");
        permissionsConfig = loadFile("Permissions.yml");
        commandsConfig    = loadFile("commands.yml");
    }

    private FileConfiguration loadFile(String name) {
        File file = new File(plugin.getDataFolder(), name);
        if (!file.exists()) plugin.saveResource(name, false);
        return YamlConfiguration.loadConfiguration(file);
    }

    public String getPrefix() {
        return plugin.getConfig().getString("settings.prefix",
                "<bold><gradient:#FF0000:#FFFFFF:#0000FF>AnimaEditor</gradient></bold> <dark_gray>»</dark_gray>");
    }

    public String getMessage(String key) {
        return plugin.getConfig().getString("messages." + key, "<red>Message not found: " + key);
    }

    public boolean requireItemInHand() {
        return plugin.getConfig().getBoolean("settings.require_item_in_hand", true);
    }

    public FileConfiguration getGuiConfig()         { return guiConfig; }
    public FileConfiguration getKitsConfig()        { return kitsConfig; }
    public FileConfiguration getPermissionsConfig() { return permissionsConfig; }
    public FileConfiguration getCommandsConfig()    { return commandsConfig; }
}
