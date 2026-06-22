================================================================================
                                README.txt
                       Plugin Name: Anima Editor GUI
================================================================================

1. OVERVIEW & PLATFORM CAPABILITIES
--------------------------------------------------------------------------------
Anima Editor GUI is an advanced, comprehensive, cross-platform item manipulation 
and management suite designed to run seamlessly across standard environments:
- Fabric
- Bukkit 
- Paper
- Spigot
- GeyserMC Bridging Support (Fully optimized layout formatting for Bedrock users)

Target Minecraft Engine Compatibility Check: Version 26.1.2

This plugin grants absolute power over item metadata, stacking modifiers, visual 
attributes, custom equipment behaviors, kit ecosystems, and dynamic particle matrices 
directly through highly custom internal Graphical User Interfaces (GUIs).

2. CORE DEPENDENCIES
--------------------------------------------------------------------------------
To run the full scope of Anima Editor GUI features, ensure your server has the 
following system dependencies installed:

* PlaceholderAPI (PAPI) - Required for resolving structural gradients, variable string descriptions, and user tracking layouts.
* ProtocolLib or Equivalent Bridge Utilities - Required for dealing with custom unrestricted item enchantments and high-frequency equipment effects packets.

3. MASTER COMMAND LIST
--------------------------------------------------------------------------------
/anima
  - Action: Open the Main Editor Menu GUI layout screen.
/anima help
  - Action: Displays an exhaustive read-out string layout of all valid commands.
/anima name <set|edit|remove>
  - Action: Renames the item currently in-hand with full native MiniMessage support.
/anima lore <set|remove>
  - Action: Appends or clears lines inside the custom meta lore profiles.
/anima description <addline|clear>
  - Action: Manages structural item description components.
/anima enchant <add|remove|edit|clear>
  - Action: Sets unrestricted/over-leveled vanilla and custom enchants.
/anima trim <set|remove>
  - Action: Applies or strips modern smithing templates and custom armor trims.
/anima glow <true|false>
  - Action: Toggles the enchanted item glow effect without applying an explicit aura enchantment.
/anima unbreakable <true|false>
  - Action: Flags an item to mathematically ignore durability degradation calculations.
/anima gradient <name|lore|desc> <1-4> <colors...>
  - Action: Sets multi-stop color transitions over text zones using standard hex inputs.
/anima effect <add|set|edit|remove|clear>
  - Action: Controls advanced status potion layers (Permanent, Interval, Spread matrix options).
/anima particle <add|edit|display|remove|clear>
  - Action: Configures complex stacked spatial positioning particles locked onto item frames or active entities.
/anima kits
  - Action: Opens the central Kits Management and editing system UI directly.
/anima repair
  - Action: Restores structural durability states back to peak item index limits.
/anima permission
  - Action: Grants real-time runtime manipulation properties over administrative group rulesets.
/anima give
  - Action: Directly spawns copies of custom items or loaded configuration kits.
/anima clearall
  - Action: Resets an item completely, purging all custom configurations and custom metadata values.
/anima destroy
  - Action: Instantly vaporizes the physical target item stack directly out of the game world environment.

4. EXHAUSTIVE PERMISSIONS INDEX
--------------------------------------------------------------------------------
Assign these permissions node values to your target player files or structural 
LuckPerms groups to establish control parameters:

  anima.use            - Base permission node to invoke access profiles over the interface layer.
  anima.kits           - Grants management authority within the custom Kit framework systems.
  anima.setenchant     - Unlocks modification flags for applying out-of-bounds item enchantments.
  anima.setcolor       - Allows full custom Hex and palette color choices over item states.
  anima.setname        - Gives full naming and standard styling access options over active items.
  anima.setlore        - Allows standard lines manipulation over item-held Lore arrays.
  anima.setdescription - Unlocks deeper text formatting descriptions within item blocks.
  anima.setnbt         - Grants raw editing access over custom raw NBT metadata structural compounds.
  anima.setglow        - Toggles the cosmetic weapon and gear fake glowing matrix flag.
  anima.setunbreakable - Toggles custom item absolute durability locks.
  anima.armortrim      - Permits altering patterns via modern cosmic Armor Trims.
  anima.banner         - Controls deep pattern combinations on custom banners and Player shields.
  anima.effect         - Dictates creation/assignment powers over custom Equipment Potion effects.
  anima.particle       - Enables access to spatial vector stacked system item particles.
  anima.repair         - Grants item recovery execution protocols.
  anima.permission     - Unlocks in-game runtime permission override structures.
  anima.reload         - Allows forcing config cache flushes and rebuilding values from the file systems.
  anima.import         - Permits structural file parsing to pull external kit template nodes.
  anima.remove         - Allows data removal parameters inside the kit registry databases.
  anima.clear          - Unlocks total destruction and wiping of modified structural attributes.
  anima.give           - Allows distributing custom built layout templates across server instances.
  anima.take           - Permits administrative seizure of structural item strings.
  anima.destroy        - Grants absolute clearing capabilities over in-world entities.
  anima.*              - The total master node root, declaring bypasses across every operational sub-perm profile.

================================================================================
                        END OF DOCUMENTATION FILE
================================================================================