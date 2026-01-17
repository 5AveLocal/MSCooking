# MSCooking

Cooking plugin for Mounsand Server

## 🔗 Requirement

Must be installed in Minecraft 1.20.6 or above.

## 📁 Setup

You need to set up items which can be cooked, and their cooked product.<br/>
Their data are stored in `itemdata.yml` under the plugin's folder.

### Adding an item

`/msck additem <item_name> <total_energy_req> [cooked_to]`, where

- `<item_name>` is the name of the item
- `<total_energy_req>` is the required energy to fully cook the item
- `[cooked_to]` is the name of the cooked product, leave it empty if it cannot be cooked

### Deleting an item

`/msck delitem <item_name>`, where

- `<item_name>` is the name of the item

### Getting an item

`/msck getitem <item_name>`, where

- `<item_name>` is the name of the item

## ⚙️ Commands

`/msck` for list of commands.

## 🪧 Signs

### hotpot

Set up a hotpot with this sign under a cauldron fully filled with water.

```
[MSCooking]
hotpot
<soupbase>
```

where

- `<soupbase>` is the type of soup base (clear / tomato / mala / mushroom / test, omit for water)

## 🍳 Cooking

### Adding items

There are 12 slots in the pot. Each slot can hold 1 item only.<br/>
Use left click, right click, or number keys to add items to the pot, but not shift click.<br/>
Note that only uncooked items that are in the food list (`itemdata.yml`) can be added.

### Power controls

Use the red "Increase Power" button to increase power.<br/>
Use the green "Decrease Power" button to decrease power.<br/>
You can view the power level from the gray display between those buttons.<br/>
The higher the power, the faster the items are cooked.<br/>

### Collecting items

Use left click, right click, shift click, or number keys to collect items from the pot.<br/>
However, if an item is overcooked (≥ 125%), it will be disposed right away.

## 🛑 Known issues

None<br/>

## ⚠️ Warnings

Any misuse of the plugin may cause unexpected behaviour.
