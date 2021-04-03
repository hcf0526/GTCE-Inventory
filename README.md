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
* [Contributors](CONTRIBUTORS)

## About

A resurrection of Gregtech Community Edition's Inventory Pipes.

It is planned that these features eventually end up in Gregtech Community Edition (GTCE) itself.

# Networks

By joining inventory pipes together and placing them next to inventories you can create inventory pipe networks.

These inventory pipe networks (or "storage network"s) form one big inventory, but you need to use the items in this mod to make use of it.

# Power

Each storage network requires power. This can be achieved in the usual ways;

* Putting a cable next to one of the inventory pipes
* Pointing a battery buffer at an inventory pipe.
* Pointing a generator's output at an inventory pipe
* Placing a solar panel on top of one of the inventory pipes

TODO: configurable power costs

# Crafting

There is a modified version of the GTCE crafting station that lets you craft using the full inventory of a storage network.

Simply place the crafting station next to one of the inventory pipes.

A tab within the crafting station let's you see (and use) the consolidated inventories of the connected storage network(s).

# Interface

A new cover called a "Storage Network Interface" lets you move items between your storage network and machines.

This cover works exactly like a GTCE conveyor, except you place it on an inventory pipe next to the machine, rather than on the machine.

# KeepInStock

A new "Keep In Stock" cover lets you configure stock levels for items made in the adjacent machine.

The cover checks the configured item's stock level within the storage network.

It automatically uses the recipes of the machine to make new items when there is not enough in stock.

TODO: limitations 

# Multiblocks

The covers also work with multiblocks. You place the cover on an inventory pipe next to the relevant input or output item bus.

## Translations
To make your own translation, add a resource pack with an assets/gtceinventory/lang/xx_yy.lang
<br>Please feel free to contribute back any translations you make.

[English](src/main/resources/assets/gtceinventory/lang/en_us.lang)