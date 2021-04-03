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
package gtceinventory.common.pipelike.inventory.tile;

import javax.annotation.Nullable;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.pipenet.block.simple.EmptyNodeData;
import gregtech.api.pipenet.tile.TileEntityPipeBase;
import gtceinventory.api.capability.GTCEInventoryCapabilities;
import gtceinventory.api.capability.IStorageNetwork;
import gtceinventory.common.pipelike.inventory.InventoryPipeType;
import gtceinventory.common.pipelike.inventory.net.InventoryPipeNet;
import gtceinventory.common.pipelike.inventory.net.WorldInventoryPipeNet;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

public class TileEntityInventoryPipe extends TileEntityPipeBase<InventoryPipeType, EmptyNodeData> {

    @Override
    public Class<InventoryPipeType> getPipeTypeClass() {
        return InventoryPipeType.class;
    }

    @Override
    public boolean supportsTicking() {
        return false;
    }

    InventoryPipeNet getPipeNet() {
        World world = getPipeWorld();
        if (world == null || world.isRemote)
            return null;
        return WorldInventoryPipeNet.getWorldPipeNet(world).getNetFromPos(getPos());
    }

    IStorageNetwork getStorageNetwork() {
        InventoryPipeNet pipeNet = getPipeNet();
        if (pipeNet == null)
            return null;
        return pipeNet.getStorageNetwork();
    }

    IEnergyContainer getEnergyContainer() {
        InventoryPipeNet pipeNet = getPipeNet();
        if (pipeNet == null)
            return null;
        return pipeNet.getEnergyContainer();
    }
    
    @Nullable
    @Override
    public <T> T getCapabilityInternal(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == GTCEInventoryCapabilities.CAPABILITY_STORAGE_NETWORK) {
            return GTCEInventoryCapabilities.CAPABILITY_STORAGE_NETWORK.cast(getStorageNetwork());
        }
        if (capability == GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER) {
            return GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER.cast(getEnergyContainer());
        }
        return super.getCapabilityInternal(capability, facing);
    }
}
