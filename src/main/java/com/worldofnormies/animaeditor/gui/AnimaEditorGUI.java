package com.worldofnormies.animaeditor.gui;

import com.worldofnormies.animaeditor.AnimaEditorPlugin;
import com.worldofnormies.animaeditor.kit.AnimaKit;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * AnimaEditorGUI — builds and opens every menu defined in AnimaEditorGUI.yml.
 *
 * This class is intentionally "dumb": it does NOT hardcode any slot, item,
 * or layout. Every menu (main_menu, kits_main_menu, equipment_effect_*_menu,
 * etc.) is read straight out of the config section with the matching name.
 * If you want to change a button, material, name, lore, or position — edit
 * the YAML. You do not need to touch this file.
 *
 * The only "special case" is kits_main_menu: any slot in that menu whose
 * configured material is GLASS_PANE is treated as a dynamic kit display
 * slot and gets overwritten with real kit data at open-time.
 */
public class AnimaEditorGUI {

    private final AnimaEditorPlugin plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public static final String MAIN_MENU_KEY = "main_menu";
    public static final String KITS_MENU_KEY = "kits_main_menu";

    public AnimaEditorGUI(AnimaEditorPlugin plugin) {
        this.plugin = plugin;
    }

    /** Open the main AnimaEditor GUI for a player. */
    public void open(Player player) {
        openMenu(player, MAIN_MENU_KEY);
    }

    /** Open the kits sub-menu. */
    public void openKits(Player player) {
        openKits(player, 0);
    }

    public void openKits(Player player, int page) {
        Inventory inv = buildMenu(KITS_MENU_KEY);
        if (inv == null) {
            player.sendMessage(mm.deserialize("<red>kits_main_menu is missing from AnimaEditorGUI.yml"));
            return;
        }
        overlayKits(inv, page);
        player.openInventory(inv);
    }

    /**
     * Generic entry point — open ANY menu key defined in AnimaEditorGUI.yml.
     * Used by GuiListener for actions like OPEN_NAME_FONT_MENU, OPEN_EFFECT_MAIN_MENU, etc.
     */
    public void openMenu(Player player, String menuKey) {
        Inventory inv = buildMenu(menuKey);
        if (inv == null) {
            player.sendMessage(mm.deserialize("<red>Menu '" + menuKey + "' is missing from AnimaEditorGUI.yml"));
            return;
        }
        if (menuKey.equals(KITS_MENU_KEY)) {
            overlayKits(inv, 0);
        }
        player.openInventory(inv);
    }

    // ── Core builder ─────────────────────────────────────────

    /**
     * Builds an Inventory purely from the named config section.
     * Returns null if the section doesn't exist.
     */
    private Inventory buildMenu(String menuKey) {
        ConfigurationSection menu = plugin.getConfigManager().getGuiConfig().getConfigurationSection(menuKey);
        if (menu == null) return null;

        int rows = menu.getInt("rows", 6);
        String title = menu.getString("title", "<gray>Menu");
        Inventory inv = Bukkit.createInventory(null, rows * 9, mm.deserialize(title));

        ConfigurationSection items = menu.getConfigurationSection("items");
        if (items != null) {
            for (String slotKey : items.getKeys(false)) {
                int slot;
                try {
                    slot = Integer.parseInt(slotKey);
                } catch (NumberFormatException e) {
                    continue;
                }
                if (slot < 0 || slot >= rows * 9) continue;

                ConfigurationSection itemSec = items.getConfigurationSection(slotKey);
                if (itemSec == null) continue;
                inv.setItem(slot, buildItemFromConfig(itemSec));
            }
        }

        return inv;
    }

    /** Builds a single ItemStack from an "items.<slot>" config section. */
    private ItemStack buildItemFromConfig(ConfigurationSection itemSec) {
        String matName = itemSec.getString("material", "STONE");
        Material material;
        try {
            material = Material.valueOf(matName.toUpperCase());
        } catch (IllegalArgumentException e) {
            material = Material.BARRIER;
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        String name = itemSec.getString("name", " ");
        meta.displayName(mm.deserialize(name));

        List<String> loreLines = itemSec.getStringList("lore");
        if (!loreLines.isEmpty()) {
            List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
            for (String line : loreLines) {
                lore.add(mm.deserialize(line));
            }
            meta.lore(lore);
        }

        item.setItemMeta(meta);
        return item;
    }

    // ── Kit overlay (dynamic content for kits_main_menu) ────────

    /**
     * Replaces every GLASS_PANE slot in the kits menu (in slot order) with
     * a real kit, with pagination.
     */
    private void overlayKits(Inventory inv, int page) {
        List<AnimaKit> kits = new ArrayList<>(plugin.getKitManager().getAllKits());
        int slotsPerPage = 0;
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && item.getType() == Material.GLASS_PANE) slotsPerPage++;
        }

        int start = page * slotsPerPage;
        int currentKitIndex = start;

        for (int slot = 0; slot < inv.getSize(); slot++) {
            ItemStack current = inv.getItem(slot);
            if (current == null || current.getType() != Material.GLASS_PANE) continue;
            if (currentKitIndex >= kits.size()) break;

            AnimaKit kit = kits.get(currentKitIndex++);
            Material mat;
            try {
                mat = Material.valueOf(kit.getMaterial());
            } catch (Exception e) {
                mat = Material.PAPER;
            }

            ItemStack kitItem = new ItemStack(mat);
            ItemMeta meta = kitItem.getItemMeta();
            if (meta != null) {
                meta.displayName(mm.deserialize(kit.getDisplayName() != null ? kit.getDisplayName() : kit.getId()));
                List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
                lore.add(mm.deserialize("<dark_gray>Creator: <gray>" + kit.getCreator()));
                lore.add(mm.deserialize("<dark_gray>ID: <gray>" + kit.getId()));
                lore.add(mm.deserialize(" "));
                lore.add(mm.deserialize("<yellow>Left-click <gray>to load"));
                lore.add(mm.deserialize("<red>Right-click <gray>to delete"));
                meta.lore(lore);
                kitItem.setItemMeta(meta);
            }
            inv.setItem(slot, kitItem);
        }
    }
}