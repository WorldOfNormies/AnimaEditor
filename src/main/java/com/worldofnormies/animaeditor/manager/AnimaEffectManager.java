package com.worldofnormies.animaeditor.manager;

import com.worldofnormies.animaeditor.AnimaEditorPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * AnimaEffectManager — applies potion effects from equipped items in
 * three modes, matching the GUI's "Equipment Effect" menus:
 *
 *   PERMANENT — effect is kept active continuously while the item is worn/held.
 *   INTERVAL  — effect re-applies in a single burst every N ticks.
 *   SPREAD    — like a lingering/area cloud: every N ticks, applies the
 *               effect to the wearer AND every player within a radius.
 *
 * All effect data is stored directly on the item via PersistentDataContainer,
 * so it travels with the item (kits, /anima give, dropping it, etc).
 */
public class AnimaEffectManager {

    public enum Mode { PERMANENT, INTERVAL, SPREAD }

    private final AnimaEditorPlugin plugin;

    private final NamespacedKey modeKey;
    private final NamespacedKey potionKey;
    private final NamespacedKey amplifierKey;
    private final NamespacedKey durationKey;   // ticks applied per pulse
    private final NamespacedKey intervalKey;   // ticks between pulses (INTERVAL/SPREAD)
    private final NamespacedKey radiusKey;     // blocks (SPREAD only)

    private BukkitTask task;
    /** tracks last-pulse tick per (player uuid + item identity) for INTERVAL/SPREAD timing */
    private final Map<String, Long> lastPulse = new HashMap<>();
    private long serverTick = 0;

    public AnimaEffectManager(AnimaEditorPlugin plugin) {
        this.plugin = plugin;
        this.modeKey = new NamespacedKey(plugin, "anima_effect_mode");
        this.potionKey = new NamespacedKey(plugin, "anima_effect_potion");
        this.amplifierKey = new NamespacedKey(plugin, "anima_effect_amplifier");
        this.durationKey = new NamespacedKey(plugin, "anima_effect_duration");
        this.intervalKey = new NamespacedKey(plugin, "anima_effect_interval");
        this.radiusKey = new NamespacedKey(plugin, "anima_effect_radius");
    }

    public PotionEffectType resolve(String name) {
        return PotionEffectType.getByKey(NamespacedKey.minecraft(name.toLowerCase().replace(" ", "_")));
    }

    /**
     * Tag an item with an effect definition.
     * durationTicks: how long each application lasts (should be >= the
     *                 ticker rate so PERMANENT effects never visibly expire).
     * intervalTicks: ignored for PERMANENT; how often INTERVAL/SPREAD pulse.
     * radius: ignored unless mode == SPREAD.
     */
    public boolean apply(ItemStack item, Mode mode, PotionEffectType type, int amplifier,
                          int durationTicks, int intervalTicks, double radius) {
        if (item == null || mode == null || type == null) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        var pdc = meta.getPersistentDataContainer();
        pdc.set(modeKey, PersistentDataType.STRING, mode.name());
        pdc.set(potionKey, PersistentDataType.STRING, type.getKey().getKey());
        pdc.set(amplifierKey, PersistentDataType.INTEGER, Math.max(0, amplifier));
        pdc.set(durationKey, PersistentDataType.INTEGER, Math.max(1, durationTicks));
        pdc.set(intervalKey, PersistentDataType.INTEGER, Math.max(1, intervalTicks));
        pdc.set(radiusKey, PersistentDataType.DOUBLE, Math.max(0.5, radius));

        item.setItemMeta(meta);
        return true;
    }

    public boolean clear(ItemStack item) {
        if (item == null) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        var pdc = meta.getPersistentDataContainer();
        boolean had = pdc.has(modeKey, PersistentDataType.STRING);
        pdc.remove(modeKey);
        pdc.remove(potionKey);
        pdc.remove(amplifierKey);
        pdc.remove(durationKey);
        pdc.remove(intervalKey);
        pdc.remove(radiusKey);
        item.setItemMeta(meta);
        return had;
    }

    // ── Repeating ticker ─────────────────────────────────────────

    public void start() {
        stop();
        long rate = plugin.getConfig().getLong("particles.update_rate_ticks", 5);
        task = plugin.getServer().getScheduler().runTaskTimer(plugin, this::tick, rate, rate);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        lastPulse.clear();
        serverTick = 0;
    }

    private void tick() {
        long rate = plugin.getConfig().getLong("particles.update_rate_ticks", 5);
        serverTick += rate;
        int maxPerItem = plugin.getConfig().getInt("effects.max_per_item", 8); // reserved for future multi-effect stacking

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            EntityEquipment eq = player.getEquipment();
            if (eq == null) continue;
            pulse(player, eq.getHelmet());
            pulse(player, eq.getChestplate());
            pulse(player, eq.getLeggings());
            pulse(player, eq.getBoots());
            pulse(player, eq.getItemInMainHand());
            pulse(player, eq.getItemInOffHand());
        }
    }

    private void pulse(Player wearer, ItemStack item) {
        if (item == null || item.getItemMeta() == null) return;
        var pdc = item.getItemMeta().getPersistentDataContainer();
        String modeName = pdc.get(modeKey, PersistentDataType.STRING);
        if (modeName == null) return;

        Mode mode;
        try {
            mode = Mode.valueOf(modeName);
        } catch (IllegalArgumentException e) {
            return;
        }

        String potionName = pdc.get(potionKey, PersistentDataType.STRING);
        PotionEffectType type = potionName != null ? resolve(potionName) : null;
        if (type == null) return;

        int amplifier = pdc.getOrDefault(amplifierKey, PersistentDataType.INTEGER, 0);
        int duration = pdc.getOrDefault(durationKey, PersistentDataType.INTEGER, 100);
        int interval = pdc.getOrDefault(intervalKey, PersistentDataType.INTEGER, 100);
        double radius = pdc.getOrDefault(radiusKey, PersistentDataType.DOUBLE, 3.0);

        long rate = plugin.getConfig().getLong("particles.update_rate_ticks", 5);

        switch (mode) {
            case PERMANENT -> {
                // refresh every tick-cycle so it never visibly runs out while worn
                wearer.addPotionEffect(new PotionEffect(type, (int) (duration), amplifier, true, false, true));
            }
            case INTERVAL -> {
                String pulseKey = wearer.getUniqueId() + ":" + System.identityHashCode(item);
                long last = lastPulse.getOrDefault(pulseKey, 0L);
                if (serverTick - last >= interval) {
                    wearer.addPotionEffect(new PotionEffect(type, duration, amplifier, true, true, true));
                    lastPulse.put(pulseKey, serverTick);
                }
            }
            case SPREAD -> {
                String pulseKey = wearer.getUniqueId() + ":" + System.identityHashCode(item) + ":spread";
                long last = lastPulse.getOrDefault(pulseKey, 0L);
                if (serverTick - last >= interval) {
                    wearer.getWorld().getNearbyEntities(wearer.getLocation(), radius, radius, radius)
                            .forEach(entity -> {
                                if (entity instanceof Player target) {
                                    target.addPotionEffect(new PotionEffect(type, duration, amplifier, true, true, true));
                                }
                            });
                    wearer.addPotionEffect(new PotionEffect(type, duration, amplifier, true, true, true));
                    // simple visual cue so "spread" reads as an area effect
                    wearer.getWorld().spawnParticle(org.bukkit.Particle.valueOf(
                            org.bukkit.Particle.values().length > 0 ? "EFFECT" : "EFFECT"
                    ), wearer.getLocation(), 20, radius / 2, 0.5, radius / 2);
                    lastPulse.put(pulseKey, serverTick);
                }
            }
        }
    }
}
