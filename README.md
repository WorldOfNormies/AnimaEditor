# 🎨 AnimaEditor

**AnimaEditor** is a powerful Minecraft plugin (targeting 26.1.2 and above) that provides a comprehensive in-game GUI for editing items. It supports advanced features like gradient-styled names, custom lore, glow control, and unrestricted enchantment levels.

---

## 🚀 Features

*   **Gradient Names**: Create beautiful item names with 1, 2, or 3 color stops.
*   **Custom Lore & Descriptions**: Easily add multi-line lore and descriptions directly from chat.
*   **Glow Control**: Toggle the enchantment glint on any item, regardless of its enchants.
*   **Unrestricted Enchantments**: Add any enchantment at any level (bypassing vanilla limits).
*   **Kits System**: Save and load custom item kits via a dedicated GUI.
*   **Unbreakable Toggle**: Make items unbreakable with a single click.
*   **Cross-Platform**: Optimized for **Paper**, **Spigot**, and **Bukkit**. Fully compatible with **Geyser**.

---

## 🎮 How to Use

Type `/anima` while holding an item to open the main editor menu.

### Main Menu Layout
- **Name Tag**: Opens the Name submenu (Bold, Underline, Edit Text).
- **Book (Lore)**: Set item lore via chat. Type `done` when finished.
- **Writable Book (Description)**: Set item description via chat.
- **Item Frames**: Toggle enchantment glow on/off.
- **Enchanting Table**: Add any enchantment + level (e.g., `sharpness 15`).
- **Hanging Sign**: Cycle between 1, 2, or 3 gradient color stops.
- **Dye Swatches**: Choose colors for your name gradient.
- **Netherite Ingot**: Toggle the Unbreakable attribute.
- **Lime Pane**: **Apply changes** to the item in your hand.
- **Red Pane**: Discard all changes.

---

## 🛠️ Commands & Permissions

### Commands
*   `/anima` - Opens the editor for the held item.
*   `/anima kits` - Opens the kit storage GUI.
*   `/anima enchant <type> <level>` - Directly add an enchantment.
*   `/anima name <text>` - Quickly set a name using current gradient settings.
*   `/anima clear <color|name|enchant|all>` - Clear specific item properties.

### Permissions
*   `anima.use`: Allows use of the editor GUI (Default: OP).
*   `anima.admin`: Allows management of kits (Default: OP).
*   `anima.enchant`: Allows adding unrestricted enchantments (Default: OP).

---

## 📦 Compatibility & Versions

This plugin is designed to run on Minecraft version **26.1.2 and higher**. It is built for:
- **Paper**: Native support for Adventure API.
- **Spigot**: Includes shaded dependencies for full feature support.
- **Bukkit**: Compatible with classic Bukkit environments.
- **Geyser**: Fully tested and optimized for Bedrock players via Geyser.

---

## 🏗️ Build & Installation

### Download
You can download the latest builds from the [Actions](https://github.com/Anima-SMP/AnimaEditor/actions) tab:
*   `AnimaEditor-Paper-26.1.2.jar`
*   `AnimaEditor-Spigot-26.1.2.jar`
*   `AnimaEditor-Bukkit-26.1.2.jar`

### Manual Build
If you wish to build the plugin yourself, run:
```bash
./gradlew assemble
```
The JAR files will be located in `build/libs/`.

### Installation
1.  Download the JAR corresponding to your server platform.
2.  Drop the JAR into your server's `plugins/` folder.
3.  Restart your server.
4.  The plugin will automatically create an `AnimaEditor` folder with `config.yml` and `kits.yml`.
