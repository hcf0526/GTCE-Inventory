/*
 * This file is part of GTCE Inventory.
 * Copyright (c) 2021, warjort and others, All rights reserved.
 *
 * It is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * It is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License.
 * If not, see <http://www.gnu.org/licenses/lgpl>.
 */
package gtceinventory.common;

import static gtceinventory.common.blocks.GTCEInventoryMetaBlocks.INVENTORY_PIPE;

import java.util.function.Function;

import gregtech.api.items.OreDictNames;
import gregtech.api.pipenet.block.ItemBlockPipe;
import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.common.blocks.BlockMachineCasing.MachineCasingType;
import gregtech.common.blocks.MetaBlocks;
import gtceinventory.GTCEInventory;
import gtceinventory.common.metatileentities.GTCEInventoryMetaTileEntities;
import gtceinventory.loaders.recipe.GTCEInventoryRecipeLoader;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(modid = GTCEInventory.MODID)
public class CommonProxy {

    @SubscribeEvent
    public static void registerBlocks(final RegistryEvent.Register<Block> event) {
        GTCEInventory.LOGGER.info("Registering Blocks...");
        final IForgeRegistry<Block> registry = event.getRegistry();

        registry.register(INVENTORY_PIPE);
    }

    @SubscribeEvent
    public static void registerItems(final RegistryEvent.Register<Item> event) {
        GTCEInventory.LOGGER.info("Registering Items...");
        final IForgeRegistry<Item> registry = event.getRegistry();

        registry.register(createItemBlock(INVENTORY_PIPE, ItemBlockPipe::new));
    }

    @SubscribeEvent
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        GTCEInventory.LOGGER.info("Registering Recipes...");

        ModHandler.addShapedRecipe("inventory_pipe",
            new ItemStack(ItemBlock.getItemFromBlock(INVENTORY_PIPE), 4), "XXX", " C ", "XXX",
            'C', OreDictNames.chestWood,
            'X', new UnificationEntry(OrePrefix.plate, Materials.Lead));

        ModHandler.addShapedRecipe("gtceinventory_workbench_bronze", GTCEInventoryMetaTileEntities.WORKBENCH.getStackForm(), "ChC", "PHP", "PWP", 'C', OreDictNames.chestWood, 'W', new ItemStack(Blocks.CRAFTING_TABLE), 'P', new UnificationEntry(OrePrefix.plate, Materials.Bronze), 'H', MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.BRONZE_HULL));

        GTCEInventoryRecipeLoader.init();
    }

    private static <T extends Block> ItemBlock createItemBlock(final T block, final Function<T, ItemBlock> producer) {
        final ItemBlock itemBlock = producer.apply(block);
        itemBlock.setRegistryName(block.getRegistryName());
        return itemBlock;
    }

    public void onPreLoad() {
    }

    public void onLoad() {
    }

    public void onPostLoad() {
    }
}