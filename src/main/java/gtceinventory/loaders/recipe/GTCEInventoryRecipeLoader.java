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
package gtceinventory.loaders.recipe;

import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.material.MarkerMaterials.Tier;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.common.items.MetaItems;
import gtceinventory.common.blocks.GTCEInventoryMetaBlocks;
import gtceinventory.common.items.GTCEInventoryMetaItems;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class GTCEInventoryRecipeLoader{

    public static void init() {
        loadCraftingRecipes();
    }

    private static void loadCraftingRecipes() {
        ItemStack inventoryPipe = new ItemStack(Item.getItemFromBlock(GTCEInventoryMetaBlocks.INVENTORY_PIPE), 1);
        ModHandler.addShapelessRecipe("interface/interface_module_lv", GTCEInventoryMetaItems.INTERFACE_MODULE_LV.getStackForm(), MetaItems.CONVEYOR_MODULE_LV, inventoryPipe);
        ModHandler.addShapelessRecipe("interface/interface_module_mv", GTCEInventoryMetaItems.INTERFACE_MODULE_MV.getStackForm(), MetaItems.CONVEYOR_MODULE_MV, inventoryPipe);
        ModHandler.addShapelessRecipe("interface/interface_module_hv", GTCEInventoryMetaItems.INTERFACE_MODULE_HV.getStackForm(), MetaItems.CONVEYOR_MODULE_HV, inventoryPipe);
        ModHandler.addShapelessRecipe("interface/interface_module_ev", GTCEInventoryMetaItems.INTERFACE_MODULE_EV.getStackForm(), MetaItems.CONVEYOR_MODULE_EV, inventoryPipe);
        ModHandler.addShapelessRecipe("interface/interface_module_iv", GTCEInventoryMetaItems.INTERFACE_MODULE_IV.getStackForm(), MetaItems.CONVEYOR_MODULE_IV, inventoryPipe);
        ModHandler.addShapelessRecipe("interface/interface_module_luv", GTCEInventoryMetaItems.INTERFACE_MODULE_LUV.getStackForm(), MetaItems.CONVEYOR_MODULE_LUV, inventoryPipe);
        ModHandler.addShapelessRecipe("interface/interface_module_zpm", GTCEInventoryMetaItems.INTERFACE_MODULE_ZPM.getStackForm(), MetaItems.CONVEYOR_MODULE_ZPM, inventoryPipe);
        ModHandler.addShapelessRecipe("interface/interface_module_uv", GTCEInventoryMetaItems.INTERFACE_MODULE_UV.getStackForm(), MetaItems.CONVEYOR_MODULE_UV, inventoryPipe);

        ModHandler.addShapelessRecipe("stock/stock_module_lv", GTCEInventoryMetaItems.STOCK_MODULE_LV.getStackForm(), GTCEInventoryMetaItems.INTERFACE_MODULE_LV, new UnificationEntry(OrePrefix.circuit, Tier.Basic));
        ModHandler.addShapelessRecipe("stock/stock_module_mv", GTCEInventoryMetaItems.STOCK_MODULE_MV.getStackForm(), GTCEInventoryMetaItems.INTERFACE_MODULE_MV, new UnificationEntry(OrePrefix.circuit, Tier.Good));
        ModHandler.addShapelessRecipe("stock/stock_module_hv", GTCEInventoryMetaItems.STOCK_MODULE_HV.getStackForm(), GTCEInventoryMetaItems.INTERFACE_MODULE_HV, new UnificationEntry(OrePrefix.circuit, Tier.Advanced));
        ModHandler.addShapelessRecipe("stock/stock_module_ev", GTCEInventoryMetaItems.STOCK_MODULE_EV.getStackForm(), GTCEInventoryMetaItems.INTERFACE_MODULE_EV, new UnificationEntry(OrePrefix.circuit, Tier.Extreme));
        ModHandler.addShapelessRecipe("stock/stock_module_iv", GTCEInventoryMetaItems.STOCK_MODULE_IV.getStackForm(), GTCEInventoryMetaItems.INTERFACE_MODULE_IV, new UnificationEntry(OrePrefix.circuit, Tier.Elite));
        ModHandler.addShapelessRecipe("stock/stock_module_luv", GTCEInventoryMetaItems.STOCK_MODULE_LUV.getStackForm(), GTCEInventoryMetaItems.INTERFACE_MODULE_LUV, new UnificationEntry(OrePrefix.circuit, Tier.Master));
        ModHandler.addShapelessRecipe("stock/stock_module_zpm", GTCEInventoryMetaItems.STOCK_MODULE_ZPM.getStackForm(), GTCEInventoryMetaItems.INTERFACE_MODULE_ZPM, new UnificationEntry(OrePrefix.circuit, Tier.Ultimate));
        ModHandler.addShapelessRecipe("stock/stock_module_uv", GTCEInventoryMetaItems.STOCK_MODULE_UV.getStackForm(), GTCEInventoryMetaItems.INTERFACE_MODULE_UV, new UnificationEntry(OrePrefix.circuit, Tier.Superconductor));
    }
}
