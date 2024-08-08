package me.fiveave.mscooking;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static me.fiveave.mscooking.boil.*;
import static me.fiveave.mscooking.main.itemdata;
import static me.fiveave.mscooking.main.mscktitle;

@SuppressWarnings("NullableProblems")
public class cmds implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (p.isOp()) {
                if (args.length == 0) {
                    p.sendMessage(mscktitle + ChatColor.YELLOW + "/msck additem/delitem/getitem");
                    return true;
                }
                switch (args[0].toLowerCase()) {
                    case "additem":
                        if (args.length == 3 || args.length == 4) {
                            ItemStack item = p.getInventory().getItemInMainHand();
                            ItemMeta itemmeta = item.getItemMeta();
                            // Set config
                            itemdata.dataconfig.set(args[1] + ".meta", itemmeta);
                            itemdata.dataconfig.set(args[1] + ".material", item.getType().toString());
                            itemdata.dataconfig.set(args[1] + ".total-energy-req", args[2]);
                            if (args.length == 4) {
                                itemdata.dataconfig.set(args[1] + ".cooked-to", args[3]);
                            }
                            // Save config
                            itemdata.save();
                            // Message to player
                            String msg = mscktitle + ChatColor.YELLOW + "Item " + ChatColor.AQUA + args[1] + ChatColor.YELLOW + " has been added with required energy of "+ ChatColor.AQUA + args[2] + ChatColor.YELLOW;
                            if (args.length == 4) {
                                msg += " and is " + ChatColor.AQUA + args[3] + ChatColor.YELLOW + " when cooked";
                            }
                            p.sendMessage(msg);
                        } else {
                            p.sendMessage(mscktitle + ChatColor.YELLOW + "/msck additem <item_name> <total_energy_req> [cooked_to]");
                        }
                        break;
                    case "delitem":
                        if (args.length == 2) {
                            // Set as null will clear lines
                            itemdata.dataconfig.set(args[1], null);
                            itemdata.save();
                            // Message to player
                            p.sendMessage(mscktitle + ChatColor.YELLOW + "Item " + ChatColor.AQUA + args[1] + ChatColor.YELLOW + " has been deleted");
                        } else {
                            p.sendMessage(mscktitle + ChatColor.YELLOW + "/msck delitem <item_name>");
                        }
                        break;
                    case "getitem":
                        if (args.length == 2) {
                            ItemMeta newitemmeta = getItemDataMeta(args[1]);
                            if (newitemmeta != null) {
                                ItemStack newitem = getItemDataItem(getItemDataMaterial(args[1]));
                                newitem.setItemMeta(newitemmeta);
                                Inventory inv = p.getInventory();
                                // See if item exists
                                int successcount = 0;
                                for (int i = 0; i < 36; i++) {
                                    // If slot is blank
                                    if (inv.getItem(i) == null) {
                                        p.getInventory().setItem(i, newitem);
                                        // Message to player
                                        p.sendMessage(mscktitle + ChatColor.YELLOW + "Item " + ChatColor.AQUA + args[1] + ChatColor.YELLOW + " has been added to your inventory.");
                                        successcount++;
                                        break;
                                    }
                                }
                                if (successcount == 0) {
                                    p.sendMessage(mscktitle + ChatColor.RED + "Your inventory is full!");
                                }
                            } else {
                                p.sendMessage(mscktitle + ChatColor.RED + "Item does not exist!");
                            }
                        } else {
                            p.sendMessage(mscktitle + ChatColor.YELLOW + "/msck getitem <item_name>");
                        }
                        break;
                }
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> result = new ArrayList<>();
        int arglength = args.length;
        if (arglength == 1) {
            List<String> ta = new ArrayList<>(Arrays.asList("additem", "delitem", "getitem"));
            for (String a : ta) {
                if (a.toLowerCase().startsWith(args[0].toLowerCase())) {
                    result.add(a);
                }
            }
            return result;
        }
        if (arglength == 2) {
            switch (args[0]) {
                case "delitem":
                case "getitem":
                    result.addAll(itemdata.dataconfig.getKeys(false));
                    break;
                default:
                    result.add("");
            }
            return result;
        }
        result.add("");
        return result;
    }
}