package com.worldofnormies.animaeditor.commands;

import com.worldofnormies.animaeditor.AnimaEditorPlugin;
import com.worldofnormies.animaeditor.gui.AnimaEditorGUI;
import com.worldofnormies.animaeditor.kit.AnimaKit;
import com.worldofnormies.animaeditor.manager.GradientManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AnimaCommand — handles all /anima sub-commands and tab completion.
 */
public class AnimaCommand implements CommandExecutor, TabCompleter {

    private final AnimaEditorPlugin plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    private static final List<String> SUBCOMMANDS = Arrays.asList(
            "help", "name", "lore", "enchant", "trim",
            "glow", "unbreakable", "color", "effect", "particle",
            "kits", "repair", "permission", "give", "clearall",
            "destroy", "reload"
    );

    public AnimaCommand(AnimaEditorPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        String prefix = plugin.getConfigManager().getPrefix();

        if (!player.hasPermission("anima.use")) {
            player.sendMessage(mm.deserialize(prefix + " " + plugin.getConfigManager().getMessage("no_permission")));
            return true;
        }

        // /anima — open GUI
        if (args.length == 0) {
            new AnimaEditorGUI(plugin).open(player);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "help" -> handleHelp(player, prefix);
            case "name" -> handleName(player, args, prefix);
            case "lore" -> handleLore(player, args, prefix);
            case "color" -> handleColor(player, args, prefix);
            case "enchant" -> handleEnchant(player, args, prefix);
            case "glow" -> handleGlow(player, args, prefix);
            case "unbreakable" -> handleUnbreakable(player, args, prefix);
            case "repair" -> handleRepair(player, prefix);
            case "clearall" -> handleClearAll(player, prefix);
            case "destroy" -> handleDestroy(player, prefix);
            case "reload" -> handleReload(player, prefix);
            case "kits" -> handleKits(player, args, prefix);
            case "give" -> handleGive(player, args, prefix);
            case "permission" -> handlePermission(player, args, prefix);
            default -> player.sendMessage(mm.deserialize(prefix + " <red>Unknown sub-command. Use <white>/anima help</white>."));
        }

        return true;
    }

    // ── Sub-Command Handlers ───────────────────────────────────

    private void handleHelp(Player player, String prefix) {
        player.sendMessage(mm.deserialize(prefix + " <gray>Available commands:"));
        player.sendMessage(mm.deserialize("<dark_gray> /anima <white>— Open the item editor GUI"));
        player.sendMessage(mm.deserialize("<dark_gray> /anima name set <text> <white>— Rename held item"));
        player.sendMessage(mm.deserialize("<dark_gray> /anima lore set <text> <white>— Set lore on held item"));
        player.sendMessage(mm.deserialize("<dark_gray> /anima color name <preset|#HEX> <text> <white>— Apply color/gradient"));
        player.sendMessage(mm.deserialize("<dark_gray> /anima enchant add <enchantment> <level> <white>— Add enchantment"));
        player.sendMessage(mm.deserialize("<dark_gray> /anima enchant remove <enchantment> <white>— Remove enchantment"));
        player.sendMessage(mm.deserialize("<dark_gray> /anima glow true|false <white>— Toggle glow"));
        player.sendMessage(mm.deserialize("<dark_gray> /anima unbreakable true|false <white>— Toggle unbreakable"));
        player.sendMessage(mm.deserialize("<dark_gray> /anima repair <white>— Repair held item"));
        player.sendMessage(mm.deserialize("<dark_gray> /anima clearall <white>— Clear all modifications"));
        player.sendMessage(mm.deserialize("<dark_gray> /anima destroy <white>— Destroy held item"));
        player.sendMessage(mm.deserialize("<dark_gray> /anima kits <save|load|list|delete> <white>— Manage kits"));
        player.sendMessage(mm.deserialize("<dark_gray> /anima give <player> <kit> <white>— Give kit to player"));
        player.sendMessage(mm.deserialize("<dark_gray> /anima reload <white>— Reload config"));
    }

    private void handleName(Player player, String[] args, String prefix) {
        if (!player.hasPermission("anima.setname")) {
            player.sendMessage(mm.deserialize(prefix + " " + plugin.getConfigManager().getMessage("no_permission")));
            return;
        }

        ItemStack item = getHeldItem(player);
        if (item == null) {
            player.sendMessage(mm.deserialize(prefix + " " + plugin.getConfigManager().getMessage("no_item_in_hand")));
            return;
        }

        if (args.length < 3) {
            player.sendMessage(mm.deserialize(prefix + " <red>Usage: /anima name set <text>"));
            return;
        }

        String action = args[1].toLowerCase();
        if (action.equals("remove")) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) { meta.displayName(null); item.setItemMeta(meta); }
            player.sendMessage(mm.deserialize(prefix + " " + plugin.getConfigManager().getMessage("item_renamed")));
            return;
        }

        // Combine remaining args as the name
        String rawName = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        Component nameComp = plugin.getGradientManager().parse(rawName);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(nameComp);
            item.setItemMeta(meta);
        }
        player.sendMessage(mm.deserialize(prefix + " " + plugin.getConfigManager().getMessage("item_renamed")));
    }

    private void handleLore(Player player, String[] args, String prefix) {
        if (!player.hasPermission("anima.setlore")) {
            player.sendMessage(mm.deserialize(prefix + " " + plugin.getConfigManager().getMessage("no_permission")));
            return;
        }

        ItemStack item = getHeldItem(player);
        if (item == null) {
            player.sendMessage(mm.deserialize(prefix + " " + plugin.getConfigManager().getMessage("no_item_in_hand")));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(mm.deserialize(prefix + " <red>Usage: /anima lore <set|remove> <text>"));
            return;
        }

        String action = args[1].toLowerCase();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        if (action.equals("remove")) {
            meta.lore(null);
            item.setItemMeta(meta);
            player.sendMessage(mm.deserialize(prefix + " " + plugin.getConfigManager().getMessage("lore_updated")));
            return;
        }

        if (args.length < 3) {
            player.sendMessage(mm.deserialize(prefix + " <red>Usage: /anima lore set <text>"));
            return;
        }

        String rawLore = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        List<Component> lore = meta.lore() != null ? new ArrayList<>(meta.lore()) : new ArrayList<>();
        lore.add(plugin.getGradientManager().parse(rawLore));
        meta.lore(lore);
        item.setItemMeta(meta);
        player.sendMessage(mm.deserialize(prefix + " " + plugin.getConfigManager().getMessage("lore_updated")));
    }

    private void handleColor(Player player, String[] args, String prefix) {
        if (!player.hasPermission("anima.setcolor")) {
            player.sendMessage(mm.deserialize(prefix + " " + plugin.getConfigManager().getMessage("no_permission")));
            return;
        }

        ItemStack item = getHeldItem(player);
        if (item == null) {
            player.sendMessage(mm.deserialize(prefix + " " + plugin.getConfigManager().getMessage("no_item_in_hand")));
            return;
        }

        if (args.length < 4) {
            player.sendMessage(mm.deserialize(prefix + " <red>Usage: /anima color name|lore <preset|#HEX1 #HEX2...> <text>"));
            player.sendMessage(mm.deserialize("<gray>Available presets: <white>" +
                    String.join(", ", plugin.getGradientManager().getPresetNames())));
            return;
        }

        GradientManager gm = plugin.getGradientManager();
        String target = args[1].toLowerCase(); // name / lore
        String presetOrHex = args[2];
        String text = String.join(" ", Arrays.copyOfRange(args, 3, args.length));

        String miniMessageText;
        if (gm.getPresets().containsKey(presetOrHex.toLowerCase())) {
            miniMessageText = gm.applyPreset(presetOrHex, text);
        } else {
            // Treat remaining as hex colors
            String[] hexColors = Arrays.copyOfRange(args, 2, args.length - 1);
            text = args[args.length - 1];
            miniMessageText = gm.buildGradient(hexColors, text);
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        if (target.equals("name")) {
            meta.displayName(gm.parse(miniMessageText));
        } else if (target.equals("lore")) {
            List<Component> lore = meta.lore() != null ? new ArrayList<>(meta.lore()) : new ArrayList<>();
            lore.add(gm.parse(miniMessageText));
            meta.lore(lore);
        }

        item.setItemMeta(meta);
        player.sendMessage(mm.deserialize(prefix + " " + plugin.getConfigManager().getMessage("gradient_applied")));
    }

    private void handleEnchant(Player player, String[] args, String prefix) {
        if (!player.hasPermission("anima.setenchant")) {
            player.sendMessage(mm.deserialize(prefix + " " + plugin.getConfigManager().getMessage("no_permission")));
            return;
        }

        ItemStack item = getHeldItem(player);
        if (item == null) {
            player.sendMessage(mm.deserialize(prefix + " " + plugin.getConfigManager().getMessage("no_item_in_hand")));
            return;
        }

        if (args.length < 3) {
            player.sendMessage(mm.deserialize(prefix + " <red>Usage: /anima enchant <add|remove|clear> [enchantment] [level]"));
            return;
        }

        String action = args[1].toLowerCase();
        com.worldofnormies.animaeditor.manager.AnimaEnchantManager em = new com.worldofnormies.animaeditor.manager.AnimaEnchantManager(plugin);

        switch (action) {
            case "add" -> {
                if (args.length < 4) {
                    player.sendMessage(mm.deserialize(prefix + " <red>Usage: /anima enchant add <enchantment> <level>"));
                    return;
                }
                org.bukkit.enchantments.Enchantment ench = em.resolve(args[2]);
                if (ench == null) {
                    player.sendMessage(mm.deserialize(prefix + " <red>Unknown enchantment: " + args[2]));
                    return;
                }
                int level;
                try {
                    level = Integer.parseInt(args[3]);
                } catch (NumberFormatException e) {
                    player.sendMessage(mm.deserialize(prefix + " <red>Invalid level: " + args[3]));
                    return;
                }
                em.apply(item, ench, level);
                player.sendMessage(mm.deserialize(prefix + " <green>Applied enchantment!"));
            }
            case "remove" -> {
                org.bukkit.enchantments.Enchantment ench = em.resolve(args[2]);
                if (ench == null) {
                    player.sendMessage(mm.deserialize(prefix + " <red>Unknown enchantment: " + args[2]));
                    return;
                }
                if (em.remove(item, ench)) {
                    player.sendMessage(mm.deserialize(prefix + " <green>Removed enchantment!"));
                } else {
                    player.sendMessage(mm.deserialize(prefix + " <red>Item didn't have that enchantment."));
                }
            }
            case "clear" -> {
                em.clear(item);
                player.sendMessage(mm.deserialize(prefix + " <green>Cleared all enchantments!"));
            }
        }
    }

    private void handleGlow(Player player, String[] args, String prefix) {
        if (!player.hasPermission("anima.setglow")) {
            player.sendMessage(mm.deserialize(prefix + " " + plugin.getConfigManager().getMessage("no_permission")));
            return;
        }

        ItemStack item = getHeldItem(player);
        if (item == null) {
            player.sendMessage(mm.deserialize(prefix + " " + plugin.getConfigManager().getMessage("no_item_in_hand")));
            return;
        }

        boolean enable = args.length < 2 || args[1].equalsIgnoreCase("true");
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        meta.setEnchantmentGlintOverride(enable);
        item.setItemMeta(meta);

        String msg = enable ? plugin.getConfigManager().getMessage("glow_enabled")
                            : plugin.getConfigManager().getMessage("glow_disabled");
        player.sendMessage(mm.deserialize(prefix + " " + msg));
    }

    private void handleUnbreakable(Player player, String[] args, String prefix) {
        if (!player.hasPermission("anima.setunbreakable")) {
            player.sendMessage(mm.deserialize(prefix + " " + plugin.getConfigManager().getMessage("no_permission")));
            return;
        }

        ItemStack item = getHeldItem(player);
        if (item == null) {
            player.sendMessage(mm.deserialize(prefix + " " + plugin.getConfigManager().getMessage("no_item_in_hand")));
            return;
        }

        boolean enable = args.length < 2 || args[1].equalsIgnoreCase("true");
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        meta.setUnbreakable(enable);
        item.setItemMeta(meta);

        String msg = enable ? plugin.getConfigManager().getMessage("unbreakable_on")
                            : plugin.getConfigManager().getMessage("unbreakable_off");
        player.sendMessage(mm.deserialize(prefix + " " + msg));
    }

    private void handleRepair(Player player, String prefix) {
        if (!player.hasPermission("anima.repair")) {
            player.sendMessage(mm.deserialize(prefix + " " + plugin.getConfigManager().getMessage("no_permission")));
            return;
        }

        ItemStack item = getHeldItem(player);
        if (item == null) {
            player.sendMessage(mm.deserialize(prefix + " " + plugin.getConfigManager().getMessage("no_item_in_hand")));
            return;
        }

        if (item.getItemMeta() instanceof org.bukkit.inventory.meta.Damageable damageable) {
            damageable.setDamage(0);
            item.setItemMeta((ItemMeta) damageable);
        }
        player.sendMessage(mm.deserialize(prefix + " " + plugin.getConfigManager().getMessage("item_repaired")));
    }

    private void handleClearAll(Player player, String prefix) {
        if (!player.hasPermission("anima.clear")) {
            player.sendMessage(mm.deserialize(prefix + " " + plugin.getConfigManager().getMessage("no_permission")));
            return;
        }

        ItemStack item = getHeldItem(player);
        if (item == null) {
            player.sendMessage(mm.deserialize(prefix + " " + plugin.getConfigManager().getMessage("no_item_in_hand")));
            return;
        }

        item.setItemMeta(Bukkit.getItemFactory().getItemMeta(item.getType()));
        player.sendMessage(mm.deserialize(prefix + " " + plugin.getConfigManager().getMessage("item_cleared")));
    }

    private void handleDestroy(Player player, String prefix) {
        if (!player.hasPermission("anima.destroy")) {
            player.sendMessage(mm.deserialize(prefix + " " + plugin.getConfigManager().getMessage("no_permission")));
            return;
        }

        ItemStack item = getHeldItem(player);
        if (item == null) {
            player.sendMessage(mm.deserialize(prefix + " " + plugin.getConfigManager().getMessage("no_item_in_hand")));
            return;
        }

        player.getInventory().setItemInMainHand(null);
        player.sendMessage(mm.deserialize(prefix + " " + plugin.getConfigManager().getMessage("item_destroyed")));
    }

    private void handleReload(Player player, String prefix) {
        if (!player.hasPermission("anima.reload")) {
            player.sendMessage(mm.deserialize(prefix + " " + plugin.getConfigManager().getMessage("no_permission")));
            return;
        }
        plugin.reload();
        player.sendMessage(mm.deserialize(prefix + " " + plugin.getConfigManager().getMessage("plugin_reloaded")));
    }

    private void handleKits(Player player, String[] args, String prefix) {
        if (!player.hasPermission("anima.kits")) {
            player.sendMessage(mm.deserialize(prefix + " " + plugin.getConfigManager().getMessage("no_permission")));
            return;
        }

        if (args.length == 1) {
            // Open kits GUI
            new AnimaEditorGUI(plugin).openKits(player);
            return;
        }

        String action = args[1].toLowerCase();

        switch (action) {
            case "list" -> {
                player.sendMessage(mm.deserialize(prefix + " <gray>Saved kits:"));
                plugin.getKitManager().getAllKits().forEach(k ->
                        player.sendMessage(mm.deserialize("  <white>» " + k.getDisplayName() +
                                " <dark_gray>(" + k.getId() + ")")));
            }
            case "save" -> {
                if (args.length < 3) {
                    player.sendMessage(mm.deserialize(prefix + " <red>Usage: /anima kits save <kit-name>"));
                    return;
                }
                ItemStack item = getHeldItem(player);
                if (item == null) {
                    player.sendMessage(mm.deserialize(prefix + " " + plugin.getConfigManager().getMessage("no_item_in_hand")));
                    return;
                }
                String kitId = args[2].toLowerCase().replace(" ", "_");
                AnimaKit kit = new AnimaKit();
                kit.setId(kitId);
                kit.setDisplayName(args[2]);
                kit.setCreator(player.getName());
                kit.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                kit.getItems().add(item.clone());
                plugin.getKitManager().addKit(kit);
                player.sendMessage(mm.deserialize(prefix + " " + plugin.getConfigManager().getMessage("kit_saved")));
            }
            case "delete" -> {
                if (args.length < 3) {
                    player.sendMessage(mm.deserialize(prefix + " <red>Usage: /anima kits delete <kit-name>"));
                    return;
                }
                boolean deleted = plugin.getKitManager().deleteKit(args[2]);
                player.sendMessage(mm.deserialize(prefix + " " +
                        (deleted ? "<green>Kit deleted." : "<red>Kit not found.")));
            }
            default -> player.sendMessage(mm.deserialize(prefix + " <red>Usage: /anima kits <save|load|list|delete> [name]"));
        }
    }

    private void handleGive(Player player, String[] args, String prefix) {
        if (!player.hasPermission("anima.give")) {
            player.sendMessage(mm.deserialize(prefix + " " + plugin.getConfigManager().getMessage("no_permission")));
            return;
        }

        if (args.length < 3) {
            player.sendMessage(mm.deserialize(prefix + " <red>Usage: /anima give <player> <kit-name>"));
            return;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            player.sendMessage(mm.deserialize(prefix + " <red>Player not found."));
            return;
        }

        String kitId = args[2].toLowerCase();
        AnimaKit kit = plugin.getKitManager().getKit(kitId);
        if (kit == null) {
            player.sendMessage(mm.deserialize(prefix + " " + plugin.getConfigManager().getMessage("kit_not_found")));
            return;
        }

        // Give all items in the kit
        for (ItemStack item : kit.getItems()) {
            target.getInventory().addItem(item.clone());
        }
        player.sendMessage(mm.deserialize(prefix + " <green>Gave kit <white>" + kit.getDisplayName() + " <green>to <white>" + target.getName()));
        target.sendMessage(mm.deserialize(prefix + " <green>You received kit " + kit.getDisplayName()));
    }

    private void handlePermission(Player player, String[] args, String prefix) {
        if (!player.hasPermission("anima.permission")) {
            player.sendMessage(mm.deserialize(prefix + " " + plugin.getConfigManager().getMessage("no_permission")));
            return;
        }

        if (args.length < 4) {
            player.sendMessage(mm.deserialize(prefix + " <red>Usage: /anima permission <player> <node|group> <value>"));
            return;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            player.sendMessage(mm.deserialize(prefix + " <red>Player not found or not online."));
            return;
        }

        String nodeOrGroup = args[2];
        String value = args[3];

        if (nodeOrGroup.equalsIgnoreCase("group")) {
            plugin.getPermissionManager().setGroup(target.getUniqueId(), target.getName(), value);
            player.sendMessage(mm.deserialize(prefix + " <green>Set group of <white>" + target.getName() + " <green>to <white>" + value));
        } else {
            boolean boolVal = Boolean.parseBoolean(value);
            plugin.getPermissionManager().setOverride(target.getUniqueId(), target.getName(), nodeOrGroup, boolVal);
            player.sendMessage(mm.deserialize(prefix + " <green>Set permission <white>" + nodeOrGroup + " <green>= <white>" + boolVal + " <green>for <white>" + target.getName()));
        }
    }

    // ── Tab Completion ─────────────────────────────────────────

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();

        if (!(sender instanceof Player player)) return completions;

        if (args.length == 1) {
            return SUBCOMMANDS.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .filter(s -> player.hasPermission("anima." + s) || player.hasPermission("anima.use"))
                    .collect(Collectors.toList());
        }

        String sub = args[0].toLowerCase();

        if (args.length == 2) {
            return switch (sub) {
                case "name" -> List.of("set", "remove");
                case "lore" -> List.of("set", "remove");
                case "enchant" -> List.of("add", "remove", "clear");
                case "glow", "unbreakable" -> List.of("true", "false");
                case "kits" -> List.of("save", "load", "list", "delete");
                case "color" -> List.of("name", "lore");
                default -> completions;
            };
        }

        if (args.length == 3 && sub.equals("color")) {
            List<String> options = new ArrayList<>(plugin.getGradientManager().getPresetNames());
            options.add("#FF0000");
            return options.stream().filter(s -> s.startsWith(args[2])).collect(Collectors.toList());
        }

        if (args.length == 3 && sub.equals("enchant")) {
             return new com.worldofnormies.animaeditor.manager.AnimaEnchantManager(plugin).allKeys().stream()
                     .filter(k -> k.startsWith(args[2].toLowerCase()))
                     .collect(Collectors.toList());
        }

        if (args.length == 2 && (sub.equals("give") || sub.equals("permission"))) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return completions;
    }

    // ── Helper ─────────────────────────────────────────────────

    private ItemStack getHeldItem(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == org.bukkit.Material.AIR) return null;
        return item;
    }
}
