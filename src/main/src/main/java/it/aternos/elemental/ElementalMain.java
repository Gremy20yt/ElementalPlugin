package it.aternos.elemental;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.*;
import java.util.*;

public class ElementalMain extends JavaPlugin implements Listener {
    private final Map<UUID, Integer> weaponMode = new HashMap<>();

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        createRecipes();
        Bukkit.getScheduler().runTaskTimer(this, this::elementalTask, 0L, 20L);
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        if (e.getBlock().getType() == Material.END_STONE && Math.random() < 0.05) {
            String[] tipi = {"Acqua", "Fuoco", "Aria", "Terra"};
            String scelto = tipi[new Random().nextInt(4)];
            ItemStack gem = new ItemStack(Material.AMETHYST_SHARD);
            ItemMeta m = gem.getItemMeta();
            m.setDisplayName("§b§lCristallo di " + scelto);
            gem.setItemMeta(m);
            e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), gem);
        }
    }

    private void createRecipes() {
        ItemStack table = new ItemStack(Material.SMITHING_TABLE);
        ItemMeta m = table.getItemMeta();
        m.setDisplayName("§6§lTavolo Elementare");
        table.setItemMeta(m);
        ShapedRecipe r = new ShapedRecipe(new NamespacedKey(this, "elem_table"), table);
        r.shape("DDD", "DSD", "DDD");
        r.setIngredient('D', Material.DIAMOND);
        r.setIngredient('S', Material.SMITHING_TABLE);
        Bukkit.addRecipe(r);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        ItemStack item = p.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR || !item.hasItemMeta()) return;
        if (p.isSneaking() && e.getAction().name().contains("RIGHT_CLICK")) {
            int mode = weaponMode.getOrDefault(p.getUniqueId(), 1);
            mode = (mode >= 5) ? 1 : mode + 1;
            weaponMode.put(p.getUniqueId(), mode);
            p.sendMessage("§b§lELEMENTO ATTIVO: §fPotere " + mode + "/5");
            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
        }
    }

    private void elementalTask() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            ItemStack chest = p.getInventory().getChestplate();
            if (chest == null || !chest.hasItemMeta()) continue;
            String name = chest.getItemMeta().getDisplayName();
            if (name.contains("Acqua")) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 40, 0));
                if (p.getLocation().getBlock().getType() == Material.WATER) p.setVelocity(p.getVelocity().setY(0.1));
            } else if (name.contains("Fuoco")) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 40, 0));
                if (p.getLocation().getBlock().getRelative(0,-1,0).getType() == Material.LAVA) p.setVelocity(p.getVelocity().setY(0.1));
            } else if (name.contains("Terra")) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 40, 4));
                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 0));
            } else if (name.contains("Aria")) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 40, 0));
                if (p.getLocation().getY() > 100) p.setAllowFlight(true);
            }
        }
    }
}
