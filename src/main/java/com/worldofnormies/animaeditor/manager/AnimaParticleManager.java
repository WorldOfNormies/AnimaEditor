package com.worldofnormies.animaeditor.manager;

import com.worldofnormies.animaeditor.AnimaEditorPlugin;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;

/**
 * AnimaParticleManager — handles particle effects for equipped items.
 * Uses NBT (PersistentDataContainer) to store particle settings.
 */
public class AnimaParticleManager {

    private final AnimaEditorPlugin plugin;
    private final NamespacedKey particleTypeKey;
    private final NamespacedKey amountKey;
    private final NamespacedKey radiusKey;
    private final NamespacedKey colorKey;
    private BukkitTask task;

    public AnimaParticleManager(AnimaEditorPlugin plugin) {
        this.plugin = plugin;
        this.particleTypeKey = new NamespacedKey(plugin, "particle_type");
        this.amountKey = new NamespacedKey(plugin, "particle_amount");
        this.radiusKey = new NamespacedKey(plugin, "particle_radius");
        this.colorKey = new NamespacedKey(plugin, "particle_color");
    }

    public void start() {
        if (task != null) task.cancel();
        task = plugin.getServer().getScheduler().runTaskTimer(plugin, this::tick, 10L, 10L);
    }

    public void stop() {
        if (task != null) task.cancel();
        task = null;
    }

    private void tick() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            EntityEquipment eq = player.getEquipment();
            if (eq == null) continue;
            checkAndSpawn(player, eq.getHelmet());
            checkAndSpawn(player, eq.getChestplate());
            checkAndSpawn(player, eq.getLeggings());
            checkAndSpawn(player, eq.getBoots());
            checkAndSpawn(player, eq.getItemInMainHand());
            checkAndSpawn(player, eq.getItemInOffHand());
        }
    }

    private void checkAndSpawn(Player player, ItemStack item) {
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        var pdc = meta.getPersistentDataContainer();

        String typeStr = pdc.get(particleTypeKey, PersistentDataType.STRING);
        if (typeStr == null) return;

        try {
            Particle particle = Particle.valueOf(typeStr.toUpperCase());
            int amount = pdc.getOrDefault(amountKey, PersistentDataType.INTEGER, 5);
            double radius = pdc.getOrDefault(radiusKey, PersistentDataType.DOUBLE, 0.5);
            String colorStr = pdc.get(colorKey, PersistentDataType.STRING);

            Location loc = player.getLocation().add(0, 1, 0);
            if (colorStr != null && particle == Particle.DUST) {
                // Example HEX color: #FF0000
                Color color = Color.fromRGB(
                        Integer.parseInt(colorStr.substring(1, 3), 16),
                        Integer.parseInt(colorStr.substring(3, 5), 16),
                        Integer.parseInt(colorStr.substring(5, 7), 16)
                );
                Particle.DustOptions options = new Particle.DustOptions(color, 1.0f);
                player.getWorld().spawnParticle(particle, loc, amount, radius, radius, radius, options);
            } else {
                player.getWorld().spawnParticle(particle, loc, amount, radius, radius, radius, 0.02);
            }
        } catch (Exception ignored) {}
    }

    public void setParticle(ItemStack item, String type, int amount, double radius, String colorHex) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        var pdc = meta.getPersistentDataContainer();
        pdc.set(particleTypeKey, PersistentDataType.STRING, type.toUpperCase());
        pdc.set(amountKey, PersistentDataType.INTEGER, amount);
        pdc.set(radiusKey, PersistentDataType.DOUBLE, radius);
        if (colorHex != null) pdc.set(colorKey, PersistentDataType.STRING, colorHex);
        item.setItemMeta(meta);
    }
}
