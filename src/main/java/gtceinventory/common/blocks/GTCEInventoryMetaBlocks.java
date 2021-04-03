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
package gtceinventory.common.blocks;

import gtceinventory.GTCEInventory;
import gtceinventory.common.pipelike.inventory.BlockInventoryPipe;
import gtceinventory.common.pipelike.inventory.tile.TileEntityInventoryPipe;
import gtceinventory.common.pipelike.inventory.tile.TileEntityInventoryPipeTickable;
import gtceinventory.common.render.InventoryPipeRenderer;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class GTCEInventoryMetaBlocks {

    public static BlockInventoryPipe INVENTORY_PIPE;

    private GTCEInventoryMetaBlocks() {
    }

    public static void init() {
        INVENTORY_PIPE = new BlockInventoryPipe();
        INVENTORY_PIPE.setRegistryName("inventory_pipe");
        registerTileEntity();
    }

    public static void registerTileEntity() {
        GameRegistry.registerTileEntity(TileEntityInventoryPipe.class, new ResourceLocation(GTCEInventory.MODID, "inventory_pipe"));
        GameRegistry.registerTileEntity(TileEntityInventoryPipeTickable.class, new ResourceLocation(GTCEInventory.MODID, "inventory_pipe_tickable"));
    }

    @SideOnly(Side.CLIENT)
    public static void registerItemModels() {
        ModelLoader.setCustomMeshDefinition(Item.getItemFromBlock(INVENTORY_PIPE), stack -> InventoryPipeRenderer.MODEL_LOCATION);
    }
}
