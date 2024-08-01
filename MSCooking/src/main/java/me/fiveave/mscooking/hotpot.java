package me.fiveave.mscooking;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

class hotpot {

    private ItemStack[] foodstore;
    private ArmorStand[] amsstore;
    private BlockDisplay[] bldstore;
    private Inventory guiinv;
    private Location loc;
    private double[] energygain;
    private int power;
    private double epppt; // Energy per power level per tick


    hotpot() {
        this.setPower(0);
        this.setEpppt(0.005);
        // Set air itemstack
        ItemStack air = new ItemStack(Material.AIR);
        ItemStack[] airstack = new ItemStack[12];
        Arrays.fill(airstack, air);
        this.setFoodstore(airstack);
        this.setEnergygain(new double[12]);
        this.setAmsstore(new ArmorStand[12]);
        this.setBldstore(new BlockDisplay[16]);
    }

    public ItemStack[] getFoodstore() {
        return this.foodstore;
    }

    public void setFoodstore(ItemStack[] foodstore) {
        this.foodstore = foodstore;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public double[] getEnergygain() {
        return energygain;
    }

    public void setEnergygain(double[] energygain) {
        this.energygain = energygain;
    }

    public Inventory getGuiinv() {
        return guiinv;
    }

    public void setGuiinv(Inventory guiinv) {
        this.guiinv = guiinv;
    }

    public double getEpppt() {
        return epppt;
    }

    public void setEpppt(double epppt) {
        this.epppt = epppt;
    }

    public ArmorStand[] getAmsstore() {
        return amsstore;
    }

    public void setAmsstore(ArmorStand[] amsstore) {
        this.amsstore = amsstore;
    }

    public Location getLoc() {
        return loc;
    }

    public void setLoc(Location loc) {
        this.loc = loc;
    }

    public BlockDisplay[] getBldstore() {
        return bldstore;
    }

    public void setBldstore(BlockDisplay[] bldstore) {
        this.bldstore = bldstore;
    }
}
