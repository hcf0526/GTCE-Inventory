# GTCE Inventory
[Curseforge](https://www.curseforge.com/minecraft/mc-mods/gtce-inventory)

Not yet released to curseforge.

[Changelog](CHANGELOG.md)

## Table of Contents
* [Overview](#about)
* [Networks](#networks)
* [Power](#power)
* [Crafting](#crafting)
* [Interface](#interface)
* [KeepInStock](#keepinstock)
* [Multiblocks](#multiblocks)
* [Translations](#translations)
* [License](LICENSE)
* [Minecraft EULA](https://www.minecraft.net/en-us/eula/)
* [Forge License](https://github.com/MinecraftForge/MinecraftForge/blob/1.16.x/LICENSE.txt)
* [GTCE License](https://github.com/GregTechCE/GregTech/blob/master/LICENSE)
* [Contributors](CONTRIBUTORS)

## About

A resurrection of Gregtech Community Edition's Inventory Pipes.

It is planned that these features eventually end up in Gregtech Community Edition [GTCE](https://github.com/GregTechCE/GregTech) itself.

# Networks

By joining inventory pipes together and placing them next to inventories you can create inventory pipe networks.

These inventory pipe networks (or "storage network"s) form one big inventory, but you need to use the items in this mod to make use of it.

# Power

Each storage network requires power. This can be achieved in the usual ways;

* Putting a cable next to one of the inventory pipes
* Pointing a battery buffer at an inventory pipe.
* Pointing a generator's output at an inventory pipe
* Placing a solar panel on top of one of the inventory pipes

In config/gtceinventory.cfg you can set the energy cost for each network, pipe, move operation or individual item moved.
<br> The default is 1 EU per second per network and 1 EU per item inserted or extracted.

# Crafting

There is a modified version of the GTCE crafting station that lets you craft using the full inventory of a storage network.

Simply place the crafting station next to one of the inventory pipes.

A additional tab within the crafting station let's you see (and use) the consolidated inventories of the connected storage network(s).

# Interface

A new cover called a "Storage Network Interface" lets you move items between your storage network and machines.

This cover works exactly like a GTCE conveyor, except you place it on an inventory pipe next to the machine, rather than on the machine.

# KeepInStock

A new "Keep In Stock" cover lets you configure stock levels for items made in the adjacent machine.

The cover checks the configured item's stock level within the storage network.

It automatically uses the recipes of the machine to make new items when there is not enough in stock.

Limitations:
* To be consistent with covers and robot arms, the keep in stock cover can't handle recipes that have more items than it can move per second
* The maximum amount to keep in stock is 8 items per tier, i.e. 8 for LV, 16 for MV, etc.
* The cover can only handle recipes according to its tier. e.g. an MV cover is required for MV recipes.

# Multiblocks

The covers also work with multiblocks. You place the cover on an inventory pipe next to the relevant input or output item bus.

## Translations
To make your own translation, add a resource pack with an assets/gtceinventory/lang/xx_yy.lang
<br>Please feel free to contribute back any translations you make.

[English](src/main/resources/assets/gtceinventory/lang/en_us.lang)