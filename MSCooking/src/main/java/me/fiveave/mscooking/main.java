package me.fiveave.mscooking;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Item;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Objects;

public final class main extends JavaPlugin implements Listener {
    public static main plugin;

    static String mscktitle = ChatColor.WHITE + "[" + ChatColor.AQUA + "MS" + ChatColor.YELLOW + "Cooking" + ChatColor.WHITE + "] ";
    static HashMap<Location, hotpot> hotpotlist = new HashMap<>();

    public void onEnable() {
        plugin = this;
        PluginManager pm = this.getServer().getPluginManager();
        pm.registerEvents(new events(), this);
    }

    public void onDisable() {
        // Remove all armor stands in list
        for (Location loc : hotpotlist.keySet()) {
            for (ArmorStand ams : hotpotlist.get(loc).getAmsstore()) {
                if (ams != null) {
                    ams.remove();
                }
            }
        }
        // Remove block display
        for (Location loc : hotpotlist.keySet()) {
            for (BlockDisplay bld :hotpotlist.get(loc).getBldstore()) {
                if (bld != null) {
                    bld.remove();
                }
            }
        }
        // Pop out all items existing in hotpot
        for (Location loc : hotpotlist.keySet()) {
            for (ItemStack item : hotpotlist.get(loc).getFoodstore()) {
                if (item != null && !item.getType().equals(Material.AIR)) {
                    Item ie = Objects.requireNonNull(loc.getWorld()).spawn(loc, Item.class);
                    ie.setItemStack(item);
                }
            }
        }
    }
}
