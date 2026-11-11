# Overview

![Main Screenshot](https://pixlcore.com/software/mss/screenshots/v2/main-v2.png)

The **Magic Sorting System** is a free [Data Pack](https://minecraft.gamepedia.com/Data_pack) available for [Minecraft](https://minecraft.net/) Java Edition 26.3.  It provides an easy way to craft an automatic, extensible item sorting system, which does not require redstone, nor console commands, nor command blocks.  Items are teleported to matching item frames, where they can be routed via hoppers into chests or furnaces (so it still requires considerable resources and building).  The whole system can be entirely built in survival mode, and supports both single and multiplayer (local or server install).

The sorting system works by providing a special magic drop off area, where you can dump any number of unsorted items (for e.g. when you come back from a mining trip), and they will be automatically sorted into [43 different categories](#groups), each with its own item frame.  Note that you do not need all 43 categories to start out -- you can progressively build your storage system over time, and have it look however you want.  You can also provide a "misc" (catch-all) category, which catches all items which don't have their own group built.

Items are teleported directly to their matching [item frames](https://minecraft.gamepedia.com/Item_Frame) for the category.  The idea is that you can place a [hopper](https://minecraft.gamepedia.com/Hopper) directly under each item frame, which then routes the items into a connected chest (or multiple chests), or furnaces for auto-smelting.  Item routing and storing is left up to the player, which can be simple or quite complex.

## Why

It is already possible to build a complete item sorting and storage system using built-in vanilla Minecraft features.  I've seen some [incredible systems](https://www.youtube.com/watch?v=wsNV9Mo00Gw), and my hat is off to those amazing builders.  However in practice, actually doing it is very difficult and tedious in my experience.  It requires a large amount of hoppers and complex redstone contraptions for filtering.  For example, to filter and store every item in the game, it would take at least 2 hoppers per unique item, or 1,550 hoppers total (3,875 iron ingots).  Doing this in survival mode would take a very long time, and use up an enormous amount of space just for the machinery.

The magic sorting system is designed to make all this much easier to build.  It requires only 1 hopper per category (43 categories total for sorting all items in the game), and handles all the item filtering and routing "magically" (i.e. by automatic item teleportation).  It checks controllers once per second and keeps survival gameplay balanced by requiring some expensive resources to start out.

Players can still build out custom storage systems using hoppers, chests, furnaces and more.  They can be as simple or complex as they want.  The magic sorting system *only* teleports items to special item frames.  The rest is up to the player.

## Features at a Glance

- Build and setup everything in pure survival mode
- Design your storage system to look however you like
- Progressively add new categories over time
- Sorts 1,657 registered item types into 43 categories
- Unsorted items automatically go into a "misc" group
- Can have multiple sorting systems in same world (128+ blocks apart)
- Items are distributed across matching item frames within 128 blocks
- Checks for items on controllers once per second
- No redstone, no command blocks, and no console commands required
- Costs 1 gold block and 1 lapis block for controller
- Each sort group costs at least 1 item frame, 1 hopper and 1 chest
- Can extend sort groups into infinite chests with chained hoppers
- Can dump entire stacks quickly using `Ctrl-Q` keybind (Windows only)
- Items are sorted within about one second of landing on the pickup area
- Many categories have fallback groups (i.e. sandstone falls back to sand, etc.)
- Automatically distributes items across multiple item frames in same group
- Support for automatic smelting, and automatic food cooking
- Create your own custom sort groups by modifying config file

## Upgrading from v1.0

Are you upgrading from Magic Sorting System v1.0?  Version 2.0 has been completely redesigned, and no longer uses a chest (well, it still can if you want one), and there is no need to "activate" a controller by standing on it.  Please see the [Creating the Controller](#creating-the-controller) section for updated instructions.  If you still want to use a drop chest, see the [Advanced](#advanced) section for instructions.

## Table of Contents

<!-- toc -->
- [Installation](#installation)
	* [Single Player](#single-player)
	* [Multiplayer](#multiplayer)
- [Usage](#usage)
	* [Building](#building)
	* [Creating the Controller](#creating-the-controller)
	* [Creating an Item Receiver](#creating-an-item-receiver)
		+ [Multiple Chests](#multiple-chests)
		+ [Multiple Item Frames](#multiple-item-frames)
		+ [Automatic Smelting or Cooking](#automatic-smelting-or-cooking)
		+ [Automatic Coal Fuel](#automatic-coal-fuel)
		+ [Fallback Groups](#fallback-groups)
		+ [Misc Catch-All Group](#misc-catch-all-group)
	* [Controller for Farms](#controller-for-farms)
	* [Advanced](#advanced)
		+ [Double Speed Auto-Drop Chest](#double-speed-auto-drop-chest)
	* [Groups](#groups)
- [Spigot and Folia plugin](#spigot-and-folia-plugin)
- [Development](#development)
- [License](#license)

# Installation

Choose one ZIP and put it directly in the world’s `datapacks` folder. Do not enable both variants, as they use the same namespace and receivers.

| Variant | Fresh kelp | Dried kelp | Dried kelp block |
| --- | --- | --- | --- |
| Standard | Ocean | Ocean | Ocean |
| Kelp as fuel | Raw | Food | Coal |

### Single Player

Select your world, choose **Edit → Open World Folder**, copy the ZIP into `datapacks`, and reopen the world.

### Multiplayer

Stop the server, copy the ZIP into `<level-name>/datapacks`, and start the server. On an existing installation, remove the previous Magic Sorting System pack first. Your gold and lapis controllers and framed receiver items can stay in place.

# Usage

In a nutshell, the magic sorting system is an item teleporter.  It will teleport unsorted items dropped onto a special pickup area, to their respective item frames nearby, and from there they simply fall into hoppers and into your storage chests.  So you need a special magic drop-off block, and one or more item groups containing item frames, hoppers and chests.  The item frames require special items displayed which identify the groups.  See [Groups](#groups) below to learn which groups require which framed items.

## Building

To build the magic sorting system, assuming survival mode, you will have to play your way to gold level first.  You will need some gold, lapis, wood, and plenty of iron to build a storage system.  At the bare minimum to get things started, you will need the following raw materials:

| Item | Name | Amount | Purpose |
|------|------|--------|---------|
| <img src="https://pixlcore.com/software/mss/images/gold_ingot.png" width="32" height="32"> | [Gold Ingot](https://minecraft.gamepedia.com/Gold_Ingot) | 9 | To build the lower base controller block. |
| <img src="https://pixlcore.com/software/mss/images/lapis_lazuli.png" width="32" height="32"> | [Lapis Lazuli](https://minecraft.gamepedia.com/Lapis_Lazuli) | 9 | To build the upper base controller block. |
| <img src="https://pixlcore.com/software/mss/images/iron_ingot.png" width="32" height="32"> | [Iron Ingot](https://minecraft.gamepedia.com/Iron_Ingot) | 5 | To build the first [hopper](https://minecraft.gamepedia.com/Hopper). |
| <img src="https://pixlcore.com/software/mss/images/oak_log.png" width="32" height="32"> | [Wood Log](https://minecraft.gamepedia.com/Log) | 4 | To build the first [chests](https://minecraft.gamepedia.com/Chest) and [item frame](https://minecraft.gamepedia.com/Item_Frame). |
| <img src="https://pixlcore.com/software/mss/images/leather.png" width="32" height="32"> | [Leather](https://minecraft.gamepedia.com/Leather) | 1 | To build the first item frame. |

As you add more groups to your sorting system, you will need additional chests, item frames (plus one special target item to put in the item frame), and a hopper.  For larger groups, multiple chests and chained hoppers are recommended (instructions below).

## Creating the Controller

The first thing you will need is a storage "controller" to deposit your unsorted items onto.  This is created by placing down a [gold block](https://minecraft.gamepedia.com/Block_of_Gold), with a [lapis block](https://minecraft.gamepedia.com/Lapis_Lazuli_Block) on top of it.

Typically the two blocks are submerged underground, so you could start with a 2-block-deep hole like this:

![Controller Build 1](https://pixlcore.com/software/mss/screenshots/v2/step-1.png)

Place the gold block on the bottom:

![Controller Build 2](https://pixlcore.com/software/mss/screenshots/v2/step-2.png)

And then place the lapis block on top of that:

![Controller Build 3](https://pixlcore.com/software/mss/screenshots/v2/step-3.png)

Fill in the rest of the hole:

![Controller Build 4](https://pixlcore.com/software/mss/screenshots/v2/step-4.png)

And you're done:

![Controller Build 5](https://pixlcore.com/software/mss/screenshots/v2/step-5.png)

If you have more lapis and gold blocks, a 3x3 grid is recommended, so it's easier to drop items onto:

![Controller Build 6](https://pixlcore.com/software/mss/screenshots/v2/step-6.png)

Now the controller is ready to go!  All you have to do now is drop your items to be sorted onto any lapis block, and they will be teleported to their respective item frames.  For Windows PC users, you can drop entire stacks at a time from your inventory using the `Ctrl-Q` keybind.  For Mac users, you can press `Q` to drop single items, or click and drag each stack out of your inventory to drop it.

For a more advanced auto-controller, see the [Advanced](#advanced) section below.  It requires a tiny bit of redstone, but it allows you to design a dropoff chest that automatically sorts all items placed into it.

## Creating an Item Receiver

An "item receiver" is just an [item frame](https://minecraft.gamepedia.com/Item_Frame) that contains a special item which represents one of the [43 groups](#groups).  Then, all items to be sorted within that group will teleport to the item frame.  What happens then?  Well, it's actually entirely up to you!  But the most common thing is to simply place a [hopper](https://minecraft.gamepedia.com/Hopper) underneath the item frame, and route that to a nearby chest.  Example:

![Single Chest Receiver](https://pixlcore.com/software/mss/screenshots/single-chest.png)

In this example, the item frame contains a [block of cobblestone](https://minecraft.gamepedia.com/Cobblestone), which is the item representing the [Stone](#groups) group, so all stone items will be routed here.  They'll teleport to the item frame, drop into hopper and be delivered to the chest.

**Tip:** Make sure you shift-click on the chest when placing the hopper, so it directs the output accordingly.  By default hoppers drop straight down.

**Tip:** Place the item frames directly on top of the hoppers. This helps to ensure the items don't fall to random areas, missing the hopper mouth.

### Multiple Chests

To handle item overflow, the best way is to simply build downward by adding more chests and hoppers directly beneath.  The hopper system in Minecraft seems to handle distribution by filling the topmost and bottommost chests evenly, working inwards as things fill up.  Example:

![Double Chest Receiver](https://pixlcore.com/software/mss/screenshots/double-chest.png)

There are, of course, a million different ways to solve this problem -- this is only one simple example.  But the idea with the magic sorting system is that it leaves things up to you!  It only teleports items to appropriate frames, but *you* can decide how to handle routing and storage.

### Multiple Item Frames

For high traffic groups such as [Stone](#groups), where a player might drop off a large number of item stacks at once, it is recommended that you use multiple item frames.  This is because a single hopper can only handle up to 5 stacks of items at one time.  If you drop off more all at once, your hopper might overflow and dump some items onto the floor before it can route them.  To handle this situation better, consider the following setup:

![Double Chest and Item Frame](https://pixlcore.com/software/mss/screenshots/double-chest-double-item-frame.png)

Here we have two item frames with identical items inside them, pointing to hoppers that route to the same chest.  This works because the magic sorting system will pick *random item frame targets* for teleporting each item, if they both display the correct group target item, effectively balancing the drop-off load between the matching frames.  So in this case the group could handle up to 10 stacks of items dropped off at once before it overflows.

There are other ways to handle item drop-off overflow, including building walls all around your item frames, so the items get "trapped" after teleporting, and just sit and wait until the hopper can service them.  However, this means your item frame would have to be hidden in a wall, or in the ceiling, and thus you lose the dual-purpose nature of having the item frame be both a visual label, and a teleportation target.

Also see the [Advanced](#advanced) section for another way to solve item overflow, which is to build an automatic drop chest.

### Automatic Smelting or Cooking

You can route your items into things other than chests -- you can also setup an easy auto-smelting system, for [ores](#groups) and/or [raw food](#groups).  Simply add an additional hopper under the item chest, and route it to a furnace on its top side:

![Ore Chest and Furnace](https://pixlcore.com/software/mss/screenshots/single-ore-single-furnace.png)

As you can see in the above example, you can also route the *output* of the furnace to another chest out the bottom, to hold your completed ingots or cooked food.

### Automatic Coal Fuel

Furnaces need fuel as well.  So taking this a step further, you can route the [Coal](#groups) item group into the *sides* of the furnace, to automatically supply it with fuel.  Here is a more complete example, with double item frames for handling many items at once:

![Full Auto-Smelter Setup](https://pixlcore.com/software/mss/screenshots/double-everything.png)

Here we have [Ores](#groups) routed into a double-chest at the top, which is then routed into two furnaces, which are auto supplied with fuel from the [Coal](#groups) group into the sides.  Finally, the furnaces deposit smelted ingots into a double chest at the bottom, which is also setup as the [Precious](#groups) group (with the [gold ingots](https://minecraft.gamepedia.com/Gold_Ingot) displayed in the bottom item frames).

So let's say you come home from a mining trip and have some raw ores, some coal, and some ingots you found in a treasure chest.  Simply drop off *all* the items at once, and the ores will get smelted, the coal fuel will power the furnaces, and all the ingots (including the ones you brought with you, *and* the smelted ones from the ore), will all end up in the [Precious](#groups) chest.

Remember that the magic sorting system will randomly pick item frames if multiple are available for a group.  So you can *also* have a [Coal](#groups) group with additional item frames routed to plain chests, and it'll balance between storing the coal in your chests, and fueling your auto-smelting system.

### Fallback Groups

Several of the item groups have sub-groups, which are optional and fall back to the main group if a more specific item frame isn't found.  For example, consider the following simple item receiver for the [Ocean](#groups) group:

![Ocean Group Simple](https://pixlcore.com/software/mss/screenshots/ocean-group-1.png)

By default, this item frame with the [kelp](https://minecraft.gamepedia.com/Kelp) leaf on display would receive *all* items that come from the ocean.  However, the sorting system defines two sub-groups under ocean, [Prismarine](#groups) and [Coral](#groups).  These optional sub-groups can further sort the ocean's many items into 3 categories, if you want:

![Ocean Group Complex](https://pixlcore.com/software/mss/screenshots/ocean-group-2.png)

So in this case, all prismarine related items would teleport to the item frame with the [prismarine](https://minecraft.gamepedia.com/Prismarine) block, all coral items would teleport to the item frame with the [bubble coral](https://minecraft.gamepedia.com/Coral_Block) block, and all the *other* non-prismarine non-coral ocean items would teleport to the item frame with the [Kelp](https://minecraft.gamepedia.com/Kelp) leaf.  The magic sorting system automatically detects which item frames are on display, and sorts accordingly.

See the [Groups](#groups) section below for a list of all 43 groups and to see how the sub-group hierarchy works.

### Misc Catch-All Group

All item groups ultimately fall back to a special [Misc](#groups) group, which is the universal "catch-all".  That is, if no suitable item frame can be found for an item, it will teleport to the miscellaneous group, which uses the [carrot on a stick](https://minecraft.gamepedia.com/Carrot_on_a_Stick) display item:

![Miscellaneous Group](https://pixlcore.com/software/mss/screenshots/misc-group.png)

This is a great way to start out your storage system, so you don't have to build everything at once.  Just build the main groups you need (dirt, sand, stone, ores, tools, etc.), and everything else will route to the misc group, until you have the time and resources to build all the specific groups.

Note that if the misc group item frame itself cannot be found, items will teleport back to the player.

## Controller for Farms

You can have as many controllers as you want in a Minecraft world.  All you need is another [lapis block](https://minecraft.gamepedia.com/Lapis_Lazuli_Block) on top of a [gold block](https://minecraft.gamepedia.com/Block_of_Gold).  One neat trick is to use one of these for a water-harvested plant farms or a nearby [dungeon](https://minecraft.gamepedia.com/Dungeon).  Any loose items detected on top of the lapis block will be sorted and teleported just like the drop-off chest.  For example, consider a simple mob dungeon like this:

![Mob Spawner Setup](https://pixlcore.com/software/mss/screenshots/mob-spawner.png)

That's a lapis block in the bottom right corner there, just outside of the water range, with a gold block under it.  If you were to setup a mob kill system here, either by trapping the mobs and killing them for XP, or devising a long fall for them, their item drops would fall onto the lapis and be instantly teleported into the sorting system.

This same kind of thing can work for an automated food farm as well, assuming you have a water-flush-harvest system in place.  If all the ripe food gets flushed into a canal and routed to a single block, have it be a lapis block, with gold underneath, and you have an automated harvest sorting system!

## Advanced

One of the features of the Magic Sorting System is that no redstone is required.  That is true, however with a simple redstone clock you can build a drop-off chest that automatically sorts all items placed inside it.  It is much easier to dump your entire inventory into a chest versus tossing it out onto the floor.  Also, this has another nice side effect in that it "throttles" item sorting so your hoppers do not overload.  Here is how to build it:

![Auto-Drop Chest 1](https://pixlcore.com/software/mss/screenshots/v2/auto-chest-redstone.png)

This requires a one [chest](https://minecraft.gamepedia.com/Chest), one [hopper](https://minecraft.gamepedia.com/Hopper), one [dropper](https://minecraft.gamepedia.com/Dropper) facing downward, one [comparator](https://minecraft.gamepedia.com/Redstone_Comparator), one [repeater](https://minecraft.gamepedia.com/Redstone_Repeater) and 5 [redstone dust](https://minecraft.gamepedia.com/Redstone_Dust).  The redstone circuit is copied verbatim from [this video tutorial](https://www.youtube.com/watch?v=-oUSS5jTHps), so please watch that to see how to orient the blocks.  In particular, the comparator must be facing *away* from the dropper.  Here is a view from above:

![Auto-Drop Chest 2](https://pixlcore.com/software/mss/screenshots/v2/auto-chest-above.png)

The key here is to face the dropper downward, leave one block of empty space below it, and then place your [lapis block](https://minecraft.gamepedia.com/Lapis_Lazuli_Block) on top of a [gold block](https://minecraft.gamepedia.com/Block_of_Gold) underneath all that.  Here is a cross section view:

![Auto-Drop Chest 3](https://pixlcore.com/software/mss/screenshots/v2/auto-chest-cross-section.png)

Then, simply place items or entire stacks into the chest and they'll automatically start sorting themselves.  This redstone clock only ticks if there are items in the chest, so it makes no noise unless items are dropped into it.

**Tip:** If the dropper ticking noise bothers you, consider using a resource pack.  Check out [Vanilla Tweaks](https://vanillatweaks.net/picker/resource-packs/), click to expand the "**Peace and Quiet**" group and then download the "**Quieter Dispensers and Droppers**" pack.

**Tip:** If you use a [trapped chest](https://minecraft.gamepedia.com/Trapped_Chest) instead of a normal one, the system will not start sorting items until you close the chest lid.  This is a nice safeguard in case you shift-click on the wrong item in your inventory.

### Double Speed Auto-Drop Chest

If the auto-drop chest is too slow for your taste, you can build a much faster one with some more materials.  For this you will need two [chests](https://minecraft.gamepedia.com/Chest), two [hoppers](https://minecraft.gamepedia.com/Hopper), two [droppers](https://minecraft.gamepedia.com/Dropper) facing downward, two [comparators](https://minecraft.gamepedia.com/Redstone_Comparator), two [repeaters](https://minecraft.gamepedia.com/Redstone_Repeater) and 10 [redstone dust](https://minecraft.gamepedia.com/Redstone_Dust).  The double-speed redstone circuit is copied verbatim from [this video tutorial](https://www.youtube.com/watch?v=w5tiFl74cSI), so please watch that to see how to orient the blocks.  Here is how it should look:

![Double Auto-Drop Chest 1](https://pixlcore.com/software/mss/screenshots/v2/double-auto-chest.png)

Note that each comparator faces away from the droppers, but the redstone is mirrored on each side.  The [video](https://www.youtube.com/watch?v=w5tiFl74cSI) does a much better job of explaining this, but here is a view from above:

![Double Auto-Drop Chest 2](https://pixlcore.com/software/mss/screenshots/v2/double-auto-chest-above.png)

Just like the single drop chest, you should face both droppers downward, leave one block of empty space below them, and then place your [lapis blocks](https://minecraft.gamepedia.com/Lapis_Lazuli_Block) on top of [gold blocks](https://minecraft.gamepedia.com/Block_of_Gold) underneath all that.  Here is a cross section view:

![Double Auto-Drop Chest 3](https://pixlcore.com/software/mss/screenshots/v2/double-auto-chest-cross-section.png)

Feel free to bury the entire thing (leave some air for the redstone to breathe), and just have the drop chest sticking up above ground.

## Groups

Use the listed item in a normal or glow item frame. A matching receiver must be loaded, in the same dimension, and within 128 blocks of the controller by default. If no receiver is available, the sorter follows the fallback column. The Misc group also catches unlisted items. With no Misc receiver, the pack returns the item to the nearest player in that dimension, if one is present.

| Group | Item in frame | Fallback | Item types (standard) |
| --- | --- | --- | ---: |
| dirt | `dirt` | misc | 10 |
| mud | `mud` | dirt | 2 |
| mudwork | `mud_bricks` | mud | 4 |
| gravel | `gravel` | dirt | 2 |
| sand | `sand` | misc | 2 |
| sandstone | `sandstone` | sand | 21 |
| wood | `oak_log` | misc | 67 |
| woodwork | `oak_stairs` | wood | 202 |
| office | `paper` | woodwork | 23 |
| stone | `cobblestone` | misc | 24 |
| stonework | `cobblestone_stairs` | stone | 149 |
| coal | `coal` | misc | 3 |
| tools | `wooden_pickaxe` | misc | 102 |
| storage | `chest` | tools | 57 |
| armor | `leather_chestplate` | tools | 79 |
| rails | `rail` | tools | 10 |
| plants | `oak_sapling` | misc | 89 |
| flowers | `poppy` | plants | 24 |
| food | `cooked_beef` | misc | 20 |
| raw | `beef` | food | 8 |
| mobs | `bone` | misc | 135 |
| precious | `gold_ingot` | misc | 32 |
| lapis | `lapis_lazuli` | precious | 2 |
| magic | `glass_bottle` | misc | 16 |
| potions | `potion` | magic | 4 |
| books | `book` | magic | 3 |
| wool | `white_wool` | misc | 106 |
| concrete | `gray_concrete_powder` | misc | 64 |
| terracotta | `terracotta` | misc | 33 |
| glass | `glass` | misc | 35 |
| ice | `ice` | misc | 6 |
| ocean | `kelp` | misc | 18 |
| coral | `bubble_coral_block` | ocean | 30 |
| prismarine | `prismarine` | ocean | 12 |
| ink | `ink_sac` | misc | 18 |
| nether | `netherrack` | misc | 33 |
| smelt | `iron_ingot` | ores | 4 |
| ores | `raw_iron` | misc | 21 |
| copper | `raw_copper` | misc | 107 |
| redstone | `redstone` | misc | 32 |
| end | `end_stone` | misc | 12 |
| music | `note_block` | misc | 25 |
| misc | `carrot_on_a_stick` | Nearest player | 11 |

See [the standard config](source/config.json) and [the kelp-as-fuel config](source/config-kelp-as-fuel.json) for the exact item lists.

# Spigot and Folia plugin

Use the plugin on a **Minecraft 26.3 Spigot or Folia server with Java 25**. Put the plugin JAR in `plugins/` and restart. Disable the datapack in that world before enabling the plugin, so only one sorter handles each drop. Your existing controllers, receiver labels, and storage can stay in place.

Drop items onto the same lapis-over-gold controller. In the default `hopper` mode, the plugin inserts the stack directly into the hopper underneath the matching normal or glow frame. It also recognizes a hopper under the block a side-mounted frame is attached to, so a full glass block can sit on top of the hopper. The frame labels, range, fallback groups, sound, and particles work as in the datapack. If the matching hopper is full, overflow stays at the controller and can be retried when space opens.

Settings are in `plugins/MagicSortingSystem/config.yml`. Restart to apply changes.

| Setting | Default | Effect |
| --- | --- | --- |
| `variant` | `standard` | Use `kelp-as-fuel` to route fresh kelp to Raw, dried kelp to Food, and dried kelp blocks to Coal. |
| `delivery` | `hopper` | Use `frame` for the original item teleport to the labeled frame. |
| `virtual-glass` | `true` | In hopper mode, labeled receiver hoppers skip searching for loose items and pulling from above to minimize performance impact on the server. They still push items to their output. |
| `max-distance` | `128` | Search radius for loaded receivers in the same world. |
| `sort-interval-ticks` | `20` | Delay between checks on a dropped item. |
| `effects` | `true` | Play the controller sound and particles when sorting succeeds. |

Virtual glass affects only recognized receivers while their frames are labeled. A physical glass cap also works with `virtual-glass: false`. Vanilla hopper ticks still handle output, so keep the hopper unpowered when it needs to move items into a chest or furnace.

The plugin tracks dropped items and frame changes in loaded chunks and looks up receivers by label and area. Folia runs world and inventory work on the owning region; a deposit across regions uses an asynchronous handoff before inserting the stack. `/mss status` shows the active variant and delivery mode, receiver counts, and deposit totals.

# Development

Edit the appropriate config in `source/` to change assignments, receiver items, fallbacks, distance, or effects. Use namespaced IDs, such as `minecraft:poplar_log`. An empty `effects` array disables the sound and particles.

Regenerate the repository’s standard pack from the project root with Node.js:

```sh
node source/generate.js
```

Use `node source/generate-kelp-as-fuel.js` to generate the kelp-as-fuel variant in place, or build separate installable ZIPs without changing the checked-in pack:

```sh
python3 source/build.py
```

The build checks both configurations against the Minecraft 26.3 server’s item registry, then writes the ZIPs to `dist/`. Each has `pack.mcmeta` at its root.

Build the plugin with JDK 25 and Maven:

```sh
mvn -f plugin/pom.xml package
```

The JAR is written to `plugin/target/magic-sorting-system-1.0.0.jar`. It bundles the two datapack sorting configurations, so the rules come from the same source. The live server probe in `plugin/test/` checks receiver layouts, preserved item components, overflow, virtual glass, and Folia cross-region delivery.

# License

[MIT](LICENSE), originally by Joseph Huckaby, with updates by G.Reaper.

Not an official Minecraft product. Not approved by or associated with Mojang.
