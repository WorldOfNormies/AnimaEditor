package com.worldofnormies.animaeditor.gui;

import com.worldofnormies.animaeditor.AnimaEditorPlugin;
import com.worldofnormies.animaeditor.kit.AnimaKit;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

/**
 * GuiListener — intercepts inventory clicks for every menu defined in
 * AnimaEditorGUI.yml and routes them based on each slot's configured
 * "action" string. No slot numbers are hardcoded here; if you add a new
 * menu/button in the YAML, just add a matching case in handleAction().
 */
public class GuiListener implements Listener {

    private final AnimaEditorPlugin plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final PlainTextComponentSerializer plain = PlainTextComponentSerializer.plainText();

    public GuiListener(AnimaEditorPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String plainTitle = plain.serialize(event.getView().title());
        String menuKey = resolveMenuKey(plainTitle);
        if (menuKey == null) return; // not one of our GUIs

        // Allow interaction with AIR slots in new_kit_menu
        if (menuKey.equals("new_kit_menu")) {
            ConfigurationSection itemSec = plugin.getConfigManager().getGuiConfig()
                    .getConfigurationSection(menuKey + ".items." + event.getSlot());
            if (itemSec != null && "AIR".equalsIgnoreCase(itemSec.getString("material"))) {
                return; // Let player place/take items in the kit grid
            }
        }

        event.setCancelled(true);
        if (event.getCurrentItem() == null) return;

        // Kits menu has dynamic per-kit slots layered on top of the config.
        if (menuKey.equals(AnimaEditorGUI.KITS_MENU_KEY)) {
            handleKitsClick(player, event.getSlot(), event.getCurrentItem(),
                    event.getClick(), event.getView().getTopInventory().getSize());
            return;
        }

        ConfigurationSection itemSec = plugin.getConfigManager().getGuiConfig()
                .getConfigurationSection(menuKey + ".items." + event.getSlot());
        if (itemSec == null) return;

        String permission = itemSec.getString("permission", null);
        if (permission != null && !player.hasPermission(permission)) {
            send(player, plugin.getConfigManager().getPrefix() + " " + plugin.getConfigManager().getMessage("no_permission"));
            return;
        }

        String action = itemSec.getString("action", null);
        if (action != null) {
            handleAction(player, action.toUpperCase());
            playActionSound(player, itemSec.getString("material", ""));
        }
    }

    private void playActionSound(Player player, String material) {
        if (material.contains("RED_BUNDLE")) {
            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
        } else if (material.contains("GREEN_BUNDLE") || material.contains("LIME_BUNDLE")) {
            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        String plainTitle = plain.serialize(event.getView().title());
        if (resolveMenuKey(plainTitle) != null) {
            event.setCancelled(true);
        }
    }

    /**
     * Finds which AnimaEditorGUI.yml menu key produced this inventory by
     * comparing plain-text titles. Built fresh each click so /anima reload
     * is always respected without needing a cache invalidation step.
     */
    private String resolveMenuKey(String plainTitle) {
        ConfigurationSection root = plugin.getConfigManager().getGuiConfig().getRoot()
                .getConfigurationSection("");
        if (root == null) root = plugin.getConfigManager().getGuiConfig();

        for (String key : plugin.getConfigManager().getGuiConfig().getKeys(false)) {
            ConfigurationSection menu = plugin.getConfigManager().getGuiConfig().getConfigurationSection(key);
            if (menu == null) continue;
            String title = menu.getString("title", null);
            if (title == null) continue;
            String menuPlain = plain.serialize(mm.deserialize(title));
            if (menuPlain.equals(plainTitle)) {
                return key;
            }
        }
        return null;
    }

    // ── Action routing ──────────────────────────────────────────

    private void handleAction(Player player, String action) {
        String prefix = plugin.getConfigManager().getPrefix();
        ItemStack held = player.getInventory().getItemInMainHand();
        boolean hasItem = held.getType() != Material.AIR;

        switch (action) {

            // ── Navigation ──────────────────────────────────
            case "OPEN_MAIN_MENU" -> {
                player.closeInventory();
                new AnimaEditorGUI(plugin).open(player);
            }
            case "OPEN_KITS_MENU" -> {
                player.closeInventory();
                new AnimaEditorGUI(plugin).openKits(player);
            }
            case "OPEN_NEW_KIT_MENU" -> {
                player.closeInventory();
                if (!hasItem) { send(player, prefix + " " + plugin.getConfigManager().getMessage("no_item_in_hand")); return; }
                send(player, prefix + " <gray>Type a name for this kit in chat:");
                plugin.getServer().getPluginManager().registerEvents(new ChatInputListener(plugin, player, "kit_save"), plugin);
            }
            case "OPEN_NAME_MENU" -> {
                player.closeInventory();
                if (!hasItem) { send(player, prefix + " " + plugin.getConfigManager().getMessage("no_item_in_hand")); return; }
                send(player, prefix + " <gray>Type the new name in chat (MiniMessage supported). Type <white>cancel <gray>to abort.");
                plugin.getServer().getPluginManager().registerEvents(new ChatInputListener(plugin, player, "name"), plugin);
            }
            case "OPEN_LORE_MENU" -> {
                player.closeInventory();
                if (!hasItem) { send(player, prefix + " " + plugin.getConfigManager().getMessage("no_item_in_hand")); return; }
                send(player, prefix + " <gray>Type a lore line in chat. Type <white>done <gray>to finish or <white>cancel <gray>to abort.");
                plugin.getServer().getPluginManager().registerEvents(new ChatInputListener(plugin, player, "lore"), plugin);
            }
            case "OPEN_NAME_FONT_MENU", "OPEN_EFFECT_MAIN_MENU", "OPEN_PERMANENT_EFFECTS_MENU",
                 "OPEN_INTERVAL_EFFECTS_MENU", "OPEN_SPREAD_EFFECTS_MENU",
                 "OPEN_SHIELD_MENU", "OPEN_PARTICLE_MENU", "OPEN_COLOR_MENU", "OPEN_ARMOR_TRIM_MENU" -> {
                player.closeInventory();
                new AnimaEditorGUI(plugin).openMenu(player, lowerMenuKeyFor(action));
            }

            case "OPEN_ENCHANT_MENU" -> {
                player.closeInventory();
                if (!hasItem) { send(player, prefix + " " + plugin.getConfigManager().getMessage("no_item_in_hand")); return; }
                send(player, prefix + " <gray>Type the enchantment name and level in chat (e.g. <white>sharpness 10<gray>). Type <white>cancel <gray>to abort.");
                plugin.getServer().getPluginManager().registerEvents(new ChatInputListener(plugin, player, "enchant"), plugin);
            }

            case "OPEN_GRADIENT_MENU" -> {
                player.closeInventory();
                if (!hasItem) { send(player, prefix + " " + plugin.getConfigManager().getMessage("no_item_in_hand")); return; }
                send(player, prefix + " <gray>Type gradient colors and text (e.g. <white>#FF0000 #00FF00 Hello<gray>). Type <white>cancel <gray>to abort.");
                plugin.getServer().getPluginManager().registerEvents(new ChatInputListener(plugin, player, "gradient"), plugin);
            }

            case "CLEAR_KIT_GRID" -> {
                for (int slot : getKitGridSlots()) {
                    player.getOpenInventory().setItem(slot, null);
                }
            }
            case "SAVE_NEW_KIT" -> {
                java.util.List<ItemStack> items = new java.util.ArrayList<>();
                for (int slot : getKitGridSlots()) {
                    ItemStack item = player.getOpenInventory().getItem(slot);
                    if (item != null && item.getType() != Material.AIR) items.add(item);
                }
                if (items.isEmpty()) { send(player, prefix + " <red>Kit is empty!"); return; }

                player.closeInventory();
                send(player, prefix + " <gray>Type a name for this kit in chat:");
                ChatInputListener listener = new ChatInputListener(plugin, player, "kit_save_multi");
                listener.setPendingItems(items);
                plugin.getServer().getPluginManager().registerEvents(listener, plugin);
            }

            // ── Not-yet-implemented systems ──
            case "RESET_ACTIONS", "SAVE_EFFECTS", "APPLY_COLOR_SELECTION", "APPLY_FONT_STYLES",
                 "EDIT_KIT_META", "LOAD_KIT_FIRST_SLOT",
                 "TOGGLE_BOLD", "TOGGLE_ITALIC", "TOGGLE_UNDERLINE" -> {
                send(player, prefix + " <yellow>This feature (" + action + ") isn't fully implemented yet.");
            }

            // ── Direct item actions ──────────────────────────
            case "TOGGLE_GLOW" -> {
                player.closeInventory();
                if (!hasItem) { send(player, prefix + " " + plugin.getConfigManager().getMessage("no_item_in_hand")); return; }
                ItemMeta meta = held.getItemMeta();
                if (meta != null) {
                    boolean current = Boolean.TRUE.equals(meta.getEnchantmentGlintOverride());
                    meta.setEnchantmentGlintOverride(!current);
                    held.setItemMeta(meta);
                    send(player, prefix + " " + plugin.getConfigManager().getMessage(current ? "glow_disabled" : "glow_enabled"));
                }
            }
            case "TOGGLE_UNBREAKABLE" -> {
                player.closeInventory();
                if (!hasItem) { send(player, prefix + " " + plugin.getConfigManager().getMessage("no_item_in_hand")); return; }
                ItemMeta meta = held.getItemMeta();
                if (meta != null) {
                    boolean current = meta.isUnbreakable();
                    meta.setUnbreakable(!current);
                    held.setItemMeta(meta);
                    send(player, prefix + " " + plugin.getConfigManager().getMessage(current ? "unbreakable_off" : "unbreakable_on"));
                }
            }
            case "REPAIR_ITEM" -> {
                player.closeInventory();
                if (!hasItem) { send(player, prefix + " " + plugin.getConfigManager().getMessage("no_item_in_hand")); return; }
                if (held.getItemMeta() instanceof org.bukkit.inventory.meta.Damageable d) {
                    d.setDamage(0);
                    held.setItemMeta((ItemMeta) d);
                }
                send(player, prefix + " " + plugin.getConfigManager().getMessage("item_repaired"));
            }
            case "CLEAR_ALL" -> {
                player.closeInventory();
                if (!hasItem) { send(player, prefix + " " + plugin.getConfigManager().getMessage("no_item_in_hand")); return; }
                held.setItemMeta(org.bukkit.Bukkit.getItemFactory().getItemMeta(held.getType()));
                send(player, prefix + " " + plugin.getConfigManager().getMessage("item_cleared"));
            }
            case "RELOAD_PLUGIN" -> {
                player.closeInventory();
                plugin.reload();
                send(player, prefix + " " + plugin.getConfigManager().getMessage("plugin_reloaded"));
            }
            case "VERSION_INFO" -> {
                send(player, prefix + " <gray>AnimaEditor v" + plugin.getDescription().getVersion() + " by WorldOfNormies");
            }

            default -> send(player, prefix + " <red>Unhandled action: " + action);
        }
    }

    /** Maps an OPEN_X_MENU action to its lowercase config section key, e.g. OPEN_EFFECT_MAIN_MENU -> equipment_effect_main_menu. */
    private String lowerMenuKeyFor(String action) {
        Map<String, String> mapping = new HashMap<>();
        mapping.put("OPEN_NAME_FONT_MENU", "name_font_style_menu");
        mapping.put("OPEN_EFFECT_MAIN_MENU", "equipment_effect_main_menu");
        mapping.put("OPEN_PERMANENT_EFFECTS_MENU", "equipment_effect_permanent_menu");
        mapping.put("OPEN_INTERVAL_EFFECTS_MENU", "equipment_effect_interval_menu");
        mapping.put("OPEN_SPREAD_EFFECTS_MENU", "equipment_effect_spread_menu");
        mapping.put("OPEN_SHIELD_MENU", "open_shield_menu"); // Added placeholder
        mapping.put("OPEN_PARTICLE_MENU", "open_particle_menu"); // Added placeholder
        mapping.put("OPEN_COLOR_MENU", "set_color_menu");
        mapping.put("OPEN_ARMOR_TRIM_MENU", "set_armor_trim_main_menu");
        return mapping.getOrDefault(action, "main_menu");
    }

    // ── Kits GUI (dynamic) ───────────────────────────────────────

    private void handleKitsClick(Player player, int slot, ItemStack clicked, org.bukkit.event.inventory.ClickType click, int invSize) {
        String prefix = plugin.getConfigManager().getPrefix();

        ConfigurationSection itemSec = plugin.getConfigManager().getGuiConfig()
                .getConfigurationSection(AnimaEditorGUI.KITS_MENU_KEY + ".items." + slot);

        // Static button defined in config (e.g. "Create New Kit") takes priority.
        if (itemSec != null && itemSec.getString("action", null) != null
                && clicked.getType() != Material.GLASS_PANE) {
            String action = itemSec.getString("action").toUpperCase();
            handleAction(player, action);
            return;
        }

        if (clicked.getType() == Material.GLASS_PANE
                || clicked.getType() == Material.WHITE_STAINED_GLASS_PANE
                || clicked.getType() == Material.BLACK_STAINED_GLASS_PANE) {
            return; // empty decorative slot
        }

        // Otherwise this is a dynamic kit slot — find the matching kit by name (overlay order).
        AnimaKit[] kitArr = plugin.getKitManager().getAllKits().toArray(new AnimaKit[0]);
        int kitIndex = dynamicSlotIndex(slot);
        if (kitIndex < 0 || kitIndex >= kitArr.length) return;
        AnimaKit kit = kitArr[kitIndex];

        if (click.isRightClick()) {
            plugin.getKitManager().deleteKit(kit.getId());
            send(player, prefix + " <red>Kit <white>" + kit.getDisplayName() + " <red>deleted.");
            player.closeInventory();
            new AnimaEditorGUI(plugin).openKits(player);
        } else if (click.isLeftClick()) {
            player.closeInventory();
            for (ItemStack item : kit.getItems()) {
                player.getInventory().addItem(item.clone());
            }
            send(player, prefix + " " + plugin.getConfigManager().getMessage("kit_loaded"));
        } else if (click == org.bukkit.event.inventory.ClickType.MIDDLE) {
            // Edit kit - load items back into editor
            player.closeInventory();
            new AnimaEditorGUI(plugin).openMenu(player, "new_kit_menu");
            int[] grid = getKitGridSlots();
            for (int i = 0; i < Math.min(kit.getItems().size(), grid.length); i++) {
                player.getOpenInventory().setItem(grid[i], kit.getItems().get(i).clone());
            }
            send(player, prefix + " <gray>Loaded kit into editor.");
        }
    }

    /**
     * Counts how many GLASS_PANE slots precede the given slot in the
     * kits_main_menu config, to translate "slot clicked" -> "Nth kit".
     * Must mirror AnimaEditorGUI#overlayKits ordering exactly.
     */
    private int dynamicSlotIndex(int clickedSlot) {
        ConfigurationSection items = plugin.getConfigManager().getGuiConfig()
                .getConfigurationSection(AnimaEditorGUI.KITS_MENU_KEY + ".items");
        if (items == null) return -1;

        int index = -1;
        for (int slot = 0; slot <= clickedSlot; slot++) {
            ConfigurationSection sec = items.getConfigurationSection(String.valueOf(slot));
            if (sec == null) continue;
            String mat = sec.getString("material", "");
            if (mat.equalsIgnoreCase("GLASS_PANE")) {
                index++;
                if (slot == clickedSlot) return index;
            }
        }
        return -1;
    }

    // ── Helper ─────────────────────────────────────────────────

    private int[] getKitGridSlots() {
        return new int[]{
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        };
    }

    private void send(Player player, String miniMessage) {
        player.sendMessage(mm.deserialize(miniMessage));
    }
}