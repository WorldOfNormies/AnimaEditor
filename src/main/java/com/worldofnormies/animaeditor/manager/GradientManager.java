package com.worldofnormies.animaeditor.manager;

import com.worldofnormies.animaeditor.AnimaEditorPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * GradientManager — handles named gradient presets and MiniMessage
 * text parsing for item names, lore, and descriptions.
 *
 * All gradient names are defined in config.yml under gradient_presets.
 * Players reference them as: /anima gradient <preset-name>
 *
 * MiniMessage format supported:
 *   <gradient:#FF0000:#FFFFFF:#0000FF>Text</gradient>
 *   <bold><italic>Text</italic></bold>
 *   <rainbow>Text</rainbow>
 *   <#RRGGBB>Text</#RRGGBB>
 */
public class GradientManager {

    private final AnimaEditorPlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Map<String, String> presets = new HashMap<>();

    public GradientManager(AnimaEditorPlugin plugin) {
        this.plugin = plugin;
        loadPresets();
    }

    public void reload() {
        presets.clear();
        loadPresets();
    }

    private void loadPresets() {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("gradient_presets");
        if (section == null) return;
        for (String key : section.getKeys(false)) {
            presets.put(key.toLowerCase(), section.getString(key, ""));
        }
        plugin.getLogger().info("Loaded " + presets.size() + " gradient presets.");
    }

    /**
     * Parse a raw MiniMessage string into an Adventure Component.
     */
    public Component parse(String miniMessageText) {
        return miniMessage.deserialize(miniMessageText);
    }

    /**
     * Apply a named preset gradient tag to wrap around the given text.
     * Example: applyPreset("fire", "Sword") → "<gradient:#FF4500:#FFD700>Sword</gradient>"
     */
    public String applyPreset(String presetName, String text) {
        String tag = presets.getOrDefault(presetName.toLowerCase(), null);
        if (tag == null) return text;
        // Close the gradient tag
        String closeTag = tag.replaceFirst("<gradient", "</gradient");
        return tag + text + "</gradient>";
    }

    /**
     * Build a gradient name string from raw hex colors.
     * Example: buildGradient(new String[]{"#FF0000","#FFFFFF","#0000FF"}, "Sword")
     */
    public String buildGradient(String[] hexColors, String text) {
        StringBuilder sb = new StringBuilder("<gradient");
        for (String hex : hexColors) {
            sb.append(":").append(hex.startsWith("#") ? hex : "#" + hex);
        }
        sb.append(">").append(text).append("</gradient>");
        return sb.toString();
    }

    /**
     * Wrap text with a font style tag.
     */
    public String applyFont(String fontPreset, String text) {
        ConfigurationSection fonts = plugin.getConfig().getConfigurationSection("font_presets");
        if (fonts == null) return text;
        String openTag = fonts.getString(fontPreset.toLowerCase(), "");
        if (openTag.isEmpty()) return text;
        // Build close tags from open tags
        String closeTags = openTag.replace("<", "</").replace("><", "> </");
        return openTag + text + closeTags;
    }

    /**
     * Strip all MiniMessage formatting tags from a string.
     */
    public String stripFormatting(String text) {
        return miniMessage.stripTags(text);
    }

    public Set<String> getPresetNames() { return presets.keySet(); }
    public Map<String, String> getPresets() { return presets; }
    public MiniMessage getMiniMessage() { return miniMessage; }
}
