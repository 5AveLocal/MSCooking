package me.fiveave.mscooking;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Objects;

import static me.fiveave.mscooking.events.*;
import static me.fiveave.mscooking.main.itemdata;
import static me.fiveave.mscooking.main.plugin;
import static org.bukkit.Material.getMaterial;

class boil {
    static double[] getTotalEnergyReq(ItemStack[] foodlist) {
        double[] retvals = new double[foodlist.length];
        for (int i = 0; i < foodlist.length; i++) {
            ItemStack food = foodlist[i];
            String retkey = findKeyFromItem(food);
            if (retkey != null) {
                double val = getItemDataEnergyReq(retkey);
                // If is -1 means cannot be cooked, set value to infinity
                if (val == -1) {
                    val = Double.MAX_VALUE;
                }
                retvals[i] = val;
            } else {
                retvals[i] = Double.MAX_VALUE;
            }
        }
        return retvals;
    }

    static double getItemDataEnergyReq(String key) {
        return Double.parseDouble(Objects.requireNonNull(itemdata.dataconfig.getString(key + ".total-energy-req")));
    }

    static String findKeyFromItem(ItemStack food) {
        // Return key from itemstack
        for (String key : itemdata.dataconfig.getKeys(false)) {
            ItemMeta newitemmeta = getItemDataMeta(key);
            if (newitemmeta != null) {
                // Create item for testing
                ItemStack newitem = getItemDataItem(getItemDataMaterial(key));
                newitem.setItemMeta(newitemmeta);
                if (food.equals(newitem) && Objects.equals(food.getItemMeta(), newitemmeta)) {
                    return key;
                }
            }
        }
        return null;
    }

    static Material getItemDataMaterial(String key) {
        // Return material value inside key
        return getMaterial(Objects.requireNonNull(itemdata.dataconfig.getString(key + ".material")));
    }

    static ItemStack getItemDataItem(Material dataconfig) {
        // Return itemstack value inside key
        return new ItemStack(Objects.requireNonNull(dataconfig), 1);
    }

    static ItemStack getCookedFood(ItemStack food) {
        String retkey = findKeyFromItem(food);
        if (retkey != null) {
            // Create item for testing
            // Path for cooked item is different from uncooked item!
            String newkey = itemdata.dataconfig.getString(retkey + ".cooked-to");
            ItemMeta newitemmeta = getItemDataMeta(newkey);
            Material cookedfoodmat;
            try {
                cookedfoodmat = getItemDataMaterial(newkey);
            } catch (Exception e) {
                // If cannot cook then return original item, no change
                return food;
            }
            ItemStack newitem = getItemDataItem(cookedfoodmat);
            newitem.setItemMeta(newitemmeta);
            return newitem;
        }
        // If cannot cook then return original item, no change
        return food;
    }

    static ItemMeta getItemDataMeta(String key) {
        return itemdata.dataconfig.get(key + ".meta") instanceof ItemMeta ? (ItemMeta) itemdata.dataconfig.get(key + ".meta") : null;
    }

    static void boilLoop(hotpot pot) {
        if (pot.getPower() > 0) {
            double[] energygain = pot.getEnergygain();
            for (int i = 0; i < energygain.length; i++) {
                energygain[i] += pot.getEpppt() * pot.getPower();
                int cookedPercent = getFloorCookedPercent(pot, i);
                // If cooked
                if (cookedPercent > 75) {
                    // Get raw item
                    ItemStack[] foodstore = pot.getFoodstore();
                    ItemStack rawfood = foodstore[i];
                    // If super-overcooked then delete item (set as air), else just make it cooked
                    ItemStack newitemstack = cookedPercent > 1000 ? new ItemStack(Material.AIR) : getCookedFood(rawfood);
                    // Create cooked item
                    if (newitemstack != null) {
                        foodstore[i] = newitemstack;
                    }
                    // Update for armor stands as well
                    if (!rawfood.equals(newitemstack)) {
                        setArmorStand(pot, i, pot.getAmsstore()[i].getWorld(), foodstore[i]);
                    }
                    pot.setFoodstore(foodstore);
                }
            }

            // Update hotpot status
            pot.setEnergygain(energygain);
            setPanels(pot, pot.getGuiinv());
            setFoods(pot, pot.getGuiinv());
            Bukkit.getScheduler().runTaskLater(plugin, () -> boilLoop(pot), 1);
        }
    }

    static void effectLoop(hotpot pot) {
        if (pot.getPower() > 0) {
            // Sound effects
            Objects.requireNonNull(pot.getLoc().getWorld()).playSound(pot.getLoc(), Sound.BLOCK_LAVA_AMBIENT, 0.5f, 1);
            // Particle effects (pos, no of particles, offset, velocity)
            Objects.requireNonNull(pot.getLoc().getWorld()).spawnParticle(Particle.WHITE_SMOKE, pot.getLoc().getBlockX() + 0.5, pot.getLoc().getBlockY() + 0.95, pot.getLoc().getBlockZ() + 0.5, 10, 0, 0, 0, 0.01);
            // Loop
            Bukkit.getScheduler().runTaskLater(plugin, () -> effectLoop(pot), 20);
        }
    }
}
