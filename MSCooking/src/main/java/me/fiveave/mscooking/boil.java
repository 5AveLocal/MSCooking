package me.fiveave.mscooking;

import org.bukkit.*;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

import static me.fiveave.mscooking.events.*;
import static me.fiveave.mscooking.main.plugin;
import static org.bukkit.Material.getMaterial;

class boil {
    static double[] getTotalEnergyReq(ItemStack[] foodlist) {
        double[] retvals = new double[foodlist.length];
        for (int i = 0; i < foodlist.length; i++) {
            ItemStack food = foodlist[i];
            switch (food.getType()) {
                case POTATO:
                case BAKED_POTATO:
                    retvals[i] = 5.0;
                    break;
                case BEEF:
                case COOKED_BEEF:
                    retvals[i] = 2.6666666666666665;
                    break;
                case PORKCHOP:
                case COOKED_PORKCHOP:
                    retvals[i] = 2.6666666666666665;
                    break;
                case MUTTON:
                case COOKED_MUTTON:
                    retvals[i] = 3.0;
                    break;
                case CHICKEN:
                case COOKED_CHICKEN:
                    retvals[i] = 3.0;
                    break;
                case RABBIT:
                case COOKED_RABBIT:
                    retvals[i] = 1.6666666666666667;
                    break;
                case COD:
                case COOKED_COD:
                    retvals[i] = 1.6666666666666667;
                    break;
                case SALMON:
                case COOKED_SALMON:
                    retvals[i] = 3.0;
                    break;
                default:
                    retvals[i] = Integer.MAX_VALUE;
                    break;
            }
        }
        return retvals;
    }

    static String getCookedFoodName(ItemStack food) {
        String retval = food.getType().name();
        switch (retval) {
            case "POTATO":
                retval = "BAKED_POTATO";
                break;
            case "BEEF":
            case "SALMON":
            case "COD":
            case "RABBIT":
            case "CHICKEN":
            case "MUTTON":
            case "PORKCHOP":
                retval = "COOKED_" + retval;
                break;
        }
        return retval;
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
                    Material newfoodmaterial = cookedPercent > 1000 ? Material.AIR : getMaterial(getCookedFoodName(rawfood));
                    // Create cooked item
                    if (newfoodmaterial != null) {
                        foodstore[i] = new ItemStack(newfoodmaterial, rawfood.getAmount());
                    }
                    // Update for armor stands as well
                    if (!rawfood.getType().equals(newfoodmaterial)) {
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
