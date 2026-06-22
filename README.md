# AnimaEditor

Advanced Item Editor Plugin for Minecraft (Paper/Spigot 1.21.4+)

## Features

- **Professional GUI**: 54-slot standard layout with intuitive navigation.
- **Advanced Renaming**: Support for MiniMessage, Hex colors, and multi-step gradients.
- **Lore Management**: Add and manage lore lines with ease.
- **Unrestricted Enchantments**: Apply any enchantment at any level to any item.
- **Equipment Effects**: Permanent, Interval, and Spread potion effects on equipped items.
- **Kits Manager**: Save, load, and manage custom item kits with a dedicated GUI.
- **Armor Trims & Shields**: Customize armor trims and shield patterns.
- **Particle Effects**: Add custom particles to equipped items via NBT.
- **Glow & Unbreakable**: Toggle item glow and unbreakable status.
- **Repair**: Instantly repair items in hand.

## Commands

- `/anima` — Open the main editor GUI.
- `/anima name set <text>` — Rename the held item.
- `/anima lore set <text>` — Add a lore line.
- `/anima color name|lore <colors...> <text>` — Apply gradients or colors.
- `/anima enchant add <enchantment> <level>` — Add an enchantment (no caps!).
- `/anima enchant remove <enchantment>` — Remove a specific enchantment.
- `/anima glow true|false` — Toggle enchantment glint.
- `/anima unbreakable true|false` — Toggle unbreakable status.
- `/anima repair` — Repair held item.
- `/anima clearall` — Remove all modifications.
- `/anima kits save|load|list|delete <name>` — Manage item kits.
- `/anima give <player> <kit>` — Give a kit to a player.
- `/anima reload` — Fully reload the plugin and its configs.

## Permissions

- `anima.use` — Basic access to /anima and main GUI.
- `anima.setname` — Permission to rename items.
- `anima.setlore` — Permission to edit lore.
- `anima.setcolor` — Permission to apply colors and gradients.
- `anima.setenchant` — Permission to manage enchantments.
- `anima.setglow` — Permission to toggle glow.
- `anima.setunbreakable` — Permission to toggle unbreakable status.
- `anima.repair` — Permission to repair items.
- `anima.clear` — Permission to clear modifications.
- `anima.kits` — Permission to use the kits system.
- `anima.effect` — Permission to manage equipment effects.
- `anima.particle` — Permission to manage particle effects.
- `anima.banner` — Permission to manage shield patterns.
- `anima.reload` — Permission to reload the plugin.
- `anima.*` — Grant all plugin permissions.

## Version 1.1.0 - What's New

- **Rebranded to AnimaEditor**.
- **Refactored structure**: Cleaner package organization.
- **Improved GUI**: Better centering, updated materials, and action sounds.
- **Enhanced Commands**: Rename `/anima gradient` to `/anima color`.
- **Bug Fixes**: Fixed Gradle build issues and configuration reloading.
