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
package gtceinventory.common.metatileentities.storage;

import gregtech.common.inventory.itemsource.sources.TileItemSource;
import gregtech.common.metatileentities.storage.CraftingRecipeMemory;
import gtceinventory.common.pipelike.inventory.network.StorageNetworkItemSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemStackHandler;

public class CraftingRecipeResolver extends gregtech.common.metatileentities.storage.CraftingRecipeResolver {

    public CraftingRecipeResolver(World world, ItemStackHandler craftingGrid, CraftingRecipeMemory recipeMemory) {
        super(world, craftingGrid, recipeMemory);
    }

    public void checkNeighbourInventories(BlockPos blockPos) {
        for (EnumFacing side : EnumFacing.VALUES) {
            TileItemSource itemSource = new TileItemSource(getItemSourceList().getWorld(), blockPos, side);
            getItemSourceList().addItemHandler(itemSource);
            StorageNetworkItemSource storageNetworkItemSource = new StorageNetworkItemSource(getItemSourceList().getWorld(), blockPos, side);
            getItemSourceList().addItemHandler(storageNetworkItemSource);
        }
    }
}
