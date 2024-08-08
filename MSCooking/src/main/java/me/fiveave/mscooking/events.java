package me.fiveave.mscooking;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.Objects;

import static me.fiveave.mscooking.boil.*;
import static me.fiveave.mscooking.main.*;
import static org.bukkit.Material.*;

class events implements Listener {
    static final int[] foodslots = new int[]{2, 4, 6, 8, 11, 13, 15, 17, 20, 22, 24, 26};
    static final ItemStack incp = getItem(Material.RED_WOOL, ChatColor.RED + "Increase Power", 1);
    static final ItemStack decp = getItem(Material.GREEN_WOOL, ChatColor.GREEN + "Decrease Power", 1);
    static ItemStack curp;

    private static void setCurp(hotpot pot) {
        // Color change for no power and up
        curp = pot.getPower() > 0 ? getItem(Material.WHITE_WOOL, ChatColor.YELLOW + "Current Power: " + ChatColor.WHITE + pot.getPower(), pot.getPower()) : getItem(Material.LIGHT_GRAY_WOOL, ChatColor.YELLOW + "Current Power: " + ChatColor.GRAY + "OFF", 1);
    }

    static void setPanels(hotpot pot, Inventory guiinv) {
        for (int count = 0; count < 12; count++) {
            Material tofill;
            String tofillname;
            int floorCookedPercent = getFloorCookedPercent(pot, count);
            // Fill panels according to food status
            if (pot.getFoodstore()[count].getType().equals(Material.AIR)) {
                tofill = Material.WHITE_STAINED_GLASS_PANE; // If no food
                tofillname = ChatColor.WHITE + "Add food ->";
            } else {
                if (floorCookedPercent >= 0 && floorCookedPercent < 50) {
                    tofill = Material.RED_STAINED_GLASS_PANE; // 0 - 50% (Raw)
                    tofillname = ChatColor.RED + String.format("Raw (%d%%)", floorCookedPercent);
                } else if (floorCookedPercent >= 50 && floorCookedPercent < 75) {
                    tofill = Material.YELLOW_STAINED_GLASS_PANE; // 50 - 75% (Halfway cooked)
                    tofillname = ChatColor.YELLOW + String.format("Halfway cooked (%d%%)", floorCookedPercent);
                } else if (floorCookedPercent >= 75 && floorCookedPercent < 125) {
                    tofill = Material.GREEN_STAINED_GLASS_PANE; // 75 - 125% (Cooked)
                    tofillname = ChatColor.GREEN + String.format("Cooked (%d%%)", floorCookedPercent);
                } else {
                    tofill = Material.BLACK_STAINED_GLASS_PANE; // Overcooked
                    tofillname = ChatColor.GRAY + String.format("Overcooked (%d%%)", floorCookedPercent);
                }
            }
            // At least 1 to let item appear
            ItemStack item = getItem(tofill, tofillname, Math.max(1, floorCookedPercent / 10));
            createItem(guiinv, foodslots[count] - 1, item);
        }
    }

    static int getFloorCookedPercent(hotpot pot, int index) {
        return (int) (pot.getEnergygain()[index] / boil.getTotalEnergyReq(pot.getFoodstore())[index] * 100);
    }

    static void setFoods(hotpot pot, Inventory guiinv) {
        for (int count = 0; count < 12; count++) {
            ItemStack item = pot.getFoodstore()[count];
            createItem(guiinv, foodslots[count], item);
        }
    }

    static void openGui(Player p, Location loc) {
        hotpot pot = hotpotlist.get(loc);
        setCurp(pot);
        Inventory guiinv;
        if (pot.getGuiinv() == null) {
            guiinv = Bukkit.createInventory(null, 27, String.format("MSCooking (%d %d %d)", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
        } else {
            guiinv = pot.getGuiinv();
        }
        setPanels(pot, guiinv);
        setFoods(pot, guiinv);
        setPowerControls(guiinv);
        p.openInventory(guiinv);
        pot.setGuiinv(guiinv);
    }

    private static void setPowerControls(Inventory guiinv) {
        createItem(guiinv, 0, incp);
        createItem(guiinv, 9, curp);
        createItem(guiinv, 18, decp);
    }

    static void createItem(Inventory inv, int pos, ItemStack item) {
        inv.setItem(pos, item);
    }

    static ItemStack getItem(Material material, String name, int amt) {
        ItemStack item = new ItemStack(material, amt);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    static void setArmorStand(hotpot pot, int conrawval, World world, ItemStack selitem) {
        ArmorStand[] amsstore = pot.getAmsstore();
        // Kill armor stand if already exists to avoid overlapping
        if (amsstore[conrawval] != null) {
            amsstore[conrawval].remove();
        }
        if (!selitem.getType().equals(Material.AIR)) {
            double dx = 0.75 - conrawval % 3 * 0.25;
            double dz = 0.25 - (int) (conrawval / 3.0) * 0.15;
            // Spawn armor stand
            ArmorStand ams = world.spawn(new Location(world, pot.getLoc().getX() + dx, pot.getLoc().getY() + 0.025, pot.getLoc().getZ() + dz), ArmorStand.class);
            ams.setSmall(true);
            ams.setVisible(false);
            ams.setGravity(false);
            ams.setRotation(0, 1);
            ams.setInvulnerable(true);
            ams.setBasePlate(false);
            ams.setHeadPose(new EulerAngle(Math.PI / 2, 0, 0));
            EntityEquipment ete = ams.getEquipment();
            assert ete != null;
            ete.setHelmet(selitem);
            // Store armor stand into hotpot
            amsstore[conrawval] = ams;
        }
        pot.setAmsstore(amsstore);
    }

    static void setBlockDisplay(hotpot pot, int index, World world, Material material) {
        BlockDisplay[] bldstore = pot.getBldstore();
        // Kill block display if already exists to avoid overlapping
        if (bldstore[index] != null) {
            bldstore[index].remove();
        }
        if (!material.equals(Material.AIR)) {
            // Spawn block display
            BlockDisplay bld = world.spawn(new Location(world, pot.getLoc().getX() + 0.05, pot.getLoc().getY() + 0.94 - index * 0.0001, pot.getLoc().getZ() + 0.05), BlockDisplay.class);
            bld.setGravity(false);
            // Adjust size
            bld.setTransformation(new Transformation(new Vector3f(0), new AxisAngle4f(0, 0, 0, 0), new Vector3f(0.9f, 0.0001f, 0.9f), new AxisAngle4f(0, 0, 0, 0)));
            BlockData bd = Bukkit.createBlockData(material);
            bld.setBlock(bd);
            // Store block display into hotpot
            bldstore[index] = bld;
        }
        pot.setBldstore(bldstore);
    }

    static void setBlockDisplayList(hotpot pot, World world, Material... materials) {
        // Clear old soup
        for (int i = 0; i < 16; i++) {
            setBlockDisplay(pot, i, world, AIR);
        }
        // Set new soup according to list
        int index = 0;
        for (Material material : materials) {
            setBlockDisplay(pot, index, world, material);
            index++;
        }
    }

    private static void setSoupBase(SignSide indsignside, hotpot pot, World world) {
        // Block display
        switch (indsignside.getLine(2)) {
            case "clear":
                setBlockDisplayList(pot, world, ORANGE_STAINED_GLASS, YELLOW_STAINED_GLASS);
                break;
            case "tomato":
                setBlockDisplayList(pot, world, RED_STAINED_GLASS, ORANGE_STAINED_GLASS, ORANGE_STAINED_GLASS, ORANGE_STAINED_GLASS, ORANGE_STAINED_GLASS, ORANGE_STAINED_GLASS);
                break;
            case "mala":
                setBlockDisplayList(pot, world, RED_STAINED_GLASS, RED_STAINED_GLASS, RED_STAINED_GLASS, RED_STAINED_GLASS, ORANGE_STAINED_GLASS, BLACK_STAINED_GLASS, BLACK_STAINED_GLASS);
                break;
            case "mushroom":
                setBlockDisplayList(pot, world, WHITE_STAINED_GLASS, ORANGE_STAINED_GLASS, YELLOW_STAINED_GLASS, YELLOW_STAINED_GLASS);
                break;
            case "test":
                setBlockDisplayList(pot, world, ORANGE_STAINED_GLASS, ORANGE_STAINED_GLASS, ORANGE_STAINED_GLASS, ORANGE_STAINED_GLASS, ORANGE_STAINED_GLASS, ORANGE_STAINED_GLASS, ORANGE_STAINED_GLASS, ORANGE_STAINED_GLASS, ORANGE_STAINED_GLASS, ORANGE_STAINED_GLASS, ORANGE_STAINED_GLASS, ORANGE_STAINED_GLASS, ORANGE_STAINED_GLASS, ORANGE_STAINED_GLASS, ORANGE_STAINED_GLASS);
                break;
            default:
                setBlockDisplayList(pot, world, AIR);
                break;
        }
    }

    @EventHandler
    void onClickUtensil(PlayerInteractEvent event) {
        Block blk = event.getClickedBlock();
        Player p = event.getPlayer();
        if (blk != null && !p.isSneaking()) {
            Location loc = blk.getLocation();
            hotpotlist.putIfAbsent(loc, new hotpot());
            hotpot pot = hotpotlist.get(loc);
            pot.setLoc(loc);
            if (blk.getType() == WATER_CAULDRON) {
                World world = blk.getWorld();
                Block indblk = world.getBlockAt(blk.getX(), blk.getY() - 1, blk.getZ());
                BlockState indstate = indblk.getState();
                if (indstate instanceof Sign) {
                    Sign indsign = (Sign) indstate;
                    SignSide indsignside = indsign.getSide(Side.FRONT);
                    if (indsignside.getLine(0).equals("[MSCooking]")) {
                        if (indsignside.getLine(1).equals("hotpot")) {
                            openGui(p, blk.getLocation());
                            // Soup base (block display)
                            setSoupBase(indsignside, pot, world);
                            event.setCancelled(true);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    void onClickInv(InventoryClickEvent event) {
        InventoryView view = event.getView();
        Player p = (Player) event.getWhoClicked();
        String title = view.getTitle();
        String[] titlesep = title.replace("MSCooking (", "").replace(")", "").split(" ");
        // Is valid pot
        if (title.contains("MSCooking (")) {
            int ex = Integer.parseInt(titlesep[0]);
            int ey = Integer.parseInt(titlesep[1]);
            int ez = Integer.parseInt(titlesep[2]);
            World world = event.getWhoClicked().getWorld();
            Location loc = new Location(world, ex, ey, ez);
            hotpotlist.putIfAbsent(loc, new hotpot());
            hotpot pot = hotpotlist.get(loc);
            int oldpower = pot.getPower();
            // Power controls
            if (Objects.equals(event.getCurrentItem(), incp) && pot.getPower() < 5) {
                pot.setPower(oldpower + 1);
            }
            if (Objects.equals(event.getCurrentItem(), decp) && pot.getPower() > 0) {
                pot.setPower(oldpower - 1);
            }
            setCurp(pot);
            // Food boiling loop if power on
            if (oldpower == 0 && pot.getPower() > 0) {
                boil.boilLoop(pot);
                boil.effectLoop(pot);
            }
            // Food slots
            int orirawval = event.getRawSlot();
            int orival = event.getSlot();
            int conrawval = findIndexFromInv(orirawval);
            // If true means slot is at upper inventory (chest), else is at lower (player)
            if (orival == orirawval) {
                // Anti invalid click types
                if (event.getClick().equals(ClickType.LEFT)) {
                    // Take food from hotpot or receive food from player (need to refine this part? kinda buggy but still works)
                    if (conrawval >= 0) {
                        ItemStack[] inv = pot.getFoodstore();
                        // Dispose overcooked food
                        if (getFloorCookedPercent(pot, conrawval) > 125) {
                            event.setCurrentItem(new ItemStack(Material.AIR));
                            p.sendMessage(mscktitle + ChatColor.RED + "Your food was disposed due to overcooking.");
                        }
                        // Valid item check
                        // If food not match or not single then cancel
                        int count = 0;
                        ItemStack cursorfood = event.getCursor();
                        ItemStack currentfood = event.getCurrentItem();
                        for (String key : itemdata.dataconfig.getKeys(false)) {
                            // Create item for testing
                            ItemMeta newitemmeta = getItemDataMeta(key);
                            ItemStack food = getItemDataItem(getItemDataMaterial(key));
                            food.setItemMeta(newitemmeta);
                            ItemStack cookedfood = getCookedFood(food);
                            if (cookedfood != null && cursorfood != null && (cursorfood.equals(food) || cursorfood.getType().equals(AIR))) {
                                count++;
                            }
                        }
                        if (count == 0 || cursorfood.getAmount() > 1 || (currentfood != null && cursorfood.getType().equals(currentfood.getType()) && currentfood.getAmount() > 0)) {
                            event.setCancelled(true);
                            p.sendMessage(mscktitle + ChatColor.RED + "Please insert valid and singular items only.");
                            return;

                        }
                        // Anti null
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            final ItemStack selitem = (event.getCurrentItem() == null) ? new ItemStack(Material.AIR) : event.getCurrentItem();
                            // Armor stand display item
                            setArmorStand(pot, conrawval, world, selitem);
                            // Store food into pot
                            inv[conrawval] = selitem;
                            pot.setFoodstore(inv);
                            // Uncook item (slot) if taken out
                            double[] energygainlist = pot.getEnergygain();
                            energygainlist[conrawval] = 0;
                            pot.setEnergygain(energygainlist);
                            // Update GUI
                            Bukkit.getScheduler().runTask(plugin, () -> {
                                setPanels(pot, pot.getGuiinv());
                                setFoods(pot, pot.getGuiinv());
                            });
                        });
                    }
                    // If not food slots in GUI
                    if (conrawval == -1) {
                        openGui(p, loc);
                        event.setCancelled(true);
                    }
                } else {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    void onClickAms(PlayerInteractAtEntityEvent event) {
        Entity e = event.getRightClicked();
        // Cancel hotpot armor stand interaction (click)
        if (e instanceof ArmorStand) {
            ArmorStand ams1 = (ArmorStand) e;
            for (Location loc : hotpotlist.keySet()) {
                for (ArmorStand ams2 : hotpotlist.get(loc).getAmsstore()) {
                    if (ams1.equals(ams2)) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    int findIndexFromInv(int input) {
        for (int i = 0; i < 12; i++) {
            if (events.foodslots[i] == input) {
                return i;
            }
        }
        return -1; // Failed
    }
}
