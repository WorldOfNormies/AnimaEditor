package com.worldofnormies.animaeditor;

import com.worldofnormies.animaeditor.commands.AnimaCommand;
import com.worldofnormies.animaeditor.gui.GuiListener;
import com.worldofnormies.animaeditor.manager.ConfigManager;
import com.worldofnormies.animaeditor.manager.GradientManager;
import com.worldofnormies.animaeditor.manager.KitManager;
import com.worldofnormies.animaeditor.manager.PermissionManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Objects;

public class AnimaEditorPlugin extends JavaPlugin {

    private static AnimaEditorPlugin instance;
    private ConfigManager configManager;
    private KitManager kitManager;
    private PermissionManager permissionManager;
    private GradientManager gradientManager;
    private com.worldofnormies.animaeditor.manager.AnimaEffectManager effectManager;
    private com.worldofnormies.animaeditor.manager.AnimaParticleManager particleManager;

    @Override
    public void onEnable() {
        instance = this;

        // Save default configs
        saveDefaultConfig();
        saveResource("AnimaEditorGUI.yml", false);
        saveResource("kits.yml", false);
        saveResource("Permissions.yml", false);
        saveResource("commands.yml", false);

        // Create plugin subdirectories
        createDirectory("Kits");
        createDirectory("Permissions");

        // Initialize managers
        configManager = new ConfigManager(this);
        configManager.load();

        gradientManager = new GradientManager(this);

        kitManager = new KitManager(this);
        kitManager.loadAll();

        permissionManager = new PermissionManager(this);
        permissionManager.loadAll();

        effectManager = new com.worldofnormies.animaeditor.manager.AnimaEffectManager(this);
        effectManager.start();

        particleManager = new com.worldofnormies.animaeditor.manager.AnimaParticleManager(this);
        particleManager.start();

        // Register command
        Objects.requireNonNull(getCommand("anima"))
                .setExecutor(new AnimaCommand(this));
        Objects.requireNonNull(getCommand("anima"))
                .setTabCompleter(new AnimaCommand(this));

        // Register GUI listener
        getServer().getPluginManager().registerEvents(new GuiListener(this), this);

        getLogger().info("AnimaEditor enabled! Version " + getDescription().getVersion());
    }

    @Override
    public void onDisable() {
        if (kitManager != null) kitManager.saveAll();
        if (permissionManager != null) permissionManager.saveAll();
        if (effectManager != null) effectManager.stop();
        if (particleManager != null) particleManager.stop();
        getLogger().info("AnimaEditor disabled.");
    }

    // ── Helpers ───────────────────────────────────────────────

    private void createDirectory(String name) {
        File dir = new File(getDataFolder(), name);
        if (!dir.exists()) dir.mkdirs();
    }

    public void reload() {
        reloadConfig();
        configManager.load();
        gradientManager.reload();
        kitManager.loadAll();
        permissionManager.loadAll();
        if (effectManager != null) {
            effectManager.stop();
            effectManager.start();
        }
        if (particleManager != null) {
            particleManager.stop();
            particleManager.start();
        }
    }

    // ── Getters ───────────────────────────────────────────────

    public static AnimaEditorPlugin getInstance() { return instance; }
    public ConfigManager getConfigManager()       { return configManager; }
    public KitManager getKitManager()             { return kitManager; }
    public PermissionManager getPermissionManager(){ return permissionManager; }
    public GradientManager getGradientManager()   { return gradientManager; }
    public com.worldofnormies.animaeditor.manager.AnimaEffectManager getEffectManager() { return effectManager; }
    public com.worldofnormies.animaeditor.manager.AnimaParticleManager getParticleManager() { return particleManager; }
}
