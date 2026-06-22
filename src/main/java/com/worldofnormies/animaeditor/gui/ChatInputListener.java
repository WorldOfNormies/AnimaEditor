package com.worldofnormies.animaeditor.gui;

import com.worldofnormies.animaeditor.AnimaEditorPlugin;
import com.worldofnormies.animaeditor.kit.AnimaKit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * ChatInputListener — a one-shot listener that captures the next chat
 * message from a specific player and uses it as input for a GUI action.
 *
 * Supported modes: "name", "lore", "kit_save"
 */
public class ChatInputListener implements Listener {

    private final AnimaEditorPlugin plugin;
    private final Player player;
    private final String mode;
    private final MiniMessage mm = MiniMessage.miniMessage();

    /** For "lore" mode — accumulates lines until "done" */
    private final List<Component> pendingLore = new ArrayList<>();

    /** For "kit_save_multi" mode */
    private List<ItemStack> pendingItems = new ArrayList<>();

    public void setPendingItems(List<ItemStack> items) { this.pendingItems = items; }

    public ChatInputListener(AnimaEditorPlugin plugin, Player player, String mode) {
        this.plugin = plugin;
        this.player = player;
        this.mode = mode;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        if (!event.getPlayer().equals(player)) return;
        event.setCancelled(true);

        String msg = event.getMessage().trim();
        String prefix = plugin.getConfigManager().getPrefix();

        if (msg.equalsIgnoreCase("cancel")) {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                player.sendMessage(mm.deserialize(prefix + " <gray>Cancelled."));
                HandlerList.unregisterAll(this);
            });
            return;
        }

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            switch (mode) {
                case "name" -> handleName(msg, prefix);
                case "lore" -> handleLore(msg, prefix);
                case "kit_save" -> handleKitSave(msg, prefix);
                case "kit_save_multi" -> handleKitSaveMulti(msg, prefix);
                case "enchant" -> handleEnchant(msg, prefix);
                case "gradient" -> handleGradient(msg, prefix);
            }
        });
    }

    private void handleName(String msg, String prefix) {
        ItemStack held = player.getInventory().getItemInMainHand();
        if (held.getType() == org.bukkit.Material.AIR) {
            player.sendMessage(mm.deserialize(prefix + " " + plugin.getConfigManager().getMessage("no_item_in_hand")));
            HandlerList.unregisterAll(this);
            return;
        }
        ItemMeta meta = held.getItemMeta();
        if (meta != null) {
            meta.displayName(plugin.getGradientManager().parse(msg));
            held.setItemMeta(meta);
        }
        player.sendMessage(mm.deserialize(prefix + " " + plugin.getConfigManager().getMessage("item_renamed")));
        HandlerList.unregisterAll(this);
    }

    private void handleLore(String msg, String prefix) {
        if (msg.equalsIgnoreCase("done")) {
            ItemStack held = player.getInventory().getItemInMainHand();
            if (held.getType() != org.bukkit.Material.AIR) {
                ItemMeta meta = held.getItemMeta();
                if (meta != null) {
                    List<Component> current = meta.lore() != null ? new ArrayList<>(meta.lore()) : new ArrayList<>();
                    current.addAll(pendingLore);
                    meta.lore(current);
                    held.setItemMeta(meta);
                }
            }
            player.sendMessage(mm.deserialize(prefix + " " + plugin.getConfigManager().getMessage("lore_updated")));
            HandlerList.unregisterAll(this);
            return;
        }

        pendingLore.add(plugin.getGradientManager().parse(msg));
        player.sendMessage(mm.deserialize(prefix + " <gray>Line added. Type another line, <white>done <gray>to save, or <white>cancel<gray>."));
    }

    private void handleKitSave(String msg, String prefix) {
        ItemStack held = player.getInventory().getItemInMainHand();
        if (held.getType() == org.bukkit.Material.AIR) {
            player.sendMessage(mm.deserialize(prefix + " " + plugin.getConfigManager().getMessage("no_item_in_hand")));
            HandlerList.unregisterAll(this);
            return;
        }

        String kitId = msg.toLowerCase().replace(" ", "_");
        AnimaKit kit = new AnimaKit();
        kit.setId(kitId);
        kit.setDisplayName(msg);
        kit.setCreator(player.getName());
        kit.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        kit.getItems().add(held.clone());

        plugin.getKitManager().addKit(kit);
        player.sendMessage(mm.deserialize(prefix + " " + plugin.getConfigManager().getMessage("kit_saved") +
                " <gray>(" + kitId + ")"));
        HandlerList.unregisterAll(this);
    }

    private void handleKitSaveMulti(String msg, String prefix) {
        String kitId = msg.toLowerCase().replace(" ", "_");
        AnimaKit kit = new AnimaKit();
        kit.setId(kitId);
        kit.setDisplayName(msg);
        kit.setCreator(player.getName());
        kit.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        kit.setItems(new ArrayList<>(pendingItems));

        plugin.getKitManager().addKit(kit);
        player.sendMessage(mm.deserialize(prefix + " " + plugin.getConfigManager().getMessage("kit_saved") +
                " <gray>(" + kitId + ")"));
        HandlerList.unregisterAll(this);
    }

    private void handleEnchant(String msg, String prefix) {
        String[] parts = msg.split(" ");
        if (parts.length < 2) {
            player.sendMessage(mm.deserialize(prefix + " <red>Usage: <enchantment> <level>"));
            return;
        }
        com.worldofnormies.animaeditor.manager.AnimaEnchantManager em = new com.worldofnormies.animaeditor.manager.AnimaEnchantManager(plugin);
        org.bukkit.enchantments.Enchantment ench = em.resolve(parts[0]);
        if (ench == null) {
            player.sendMessage(mm.deserialize(prefix + " <red>Unknown enchantment: " + parts[0]));
            return;
        }
        int level;
        try {
            level = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(mm.deserialize(prefix + " <red>Invalid level: " + parts[1]));
            return;
        }
        ItemStack held = player.getInventory().getItemInMainHand();
        if (held.getType() != org.bukkit.Material.AIR) {
            em.apply(held, ench, level);
            player.sendMessage(mm.deserialize(prefix + " <green>Applied " + parts[0] + " level " + level));
        } else {
            player.sendMessage(mm.deserialize(prefix + " " + plugin.getConfigManager().getMessage("no_item_in_hand")));
        }
        HandlerList.unregisterAll(this);
    }

    private void handleGradient(String msg, String prefix) {
        String[] parts = msg.split(" ");
        if (parts.length < 2) {
            player.sendMessage(mm.deserialize(prefix + " <red>Usage: <color1> <color2>... <text>"));
            return;
        }
        String text = parts[parts.length - 1];
        String[] colors = java.util.Arrays.copyOfRange(parts, 0, parts.length - 1);

        String miniMessage = plugin.getGradientManager().buildGradient(colors, text);
        ItemStack held = player.getInventory().getItemInMainHand();
        if (held.getType() != org.bukkit.Material.AIR) {
            ItemMeta meta = held.getItemMeta();
            if (meta != null) {
                meta.displayName(plugin.getGradientManager().parse(miniMessage));
                held.setItemMeta(meta);
                player.sendMessage(mm.deserialize(prefix + " <green>Gradient name applied!"));
            }
        } else {
            player.sendMessage(mm.deserialize(prefix + " " + plugin.getConfigManager().getMessage("no_item_in_hand")));
        }
        HandlerList.unregisterAll(this);
    }
}
