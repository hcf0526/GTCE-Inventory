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
package gtceinventory.common.pipelike.inventory.net;

import gregtech.api.capability.IEnergyContainer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.util.INBTSerializable;

public class InventoryPipeNetEnergyContainer implements IEnergyContainer, INBTSerializable<NBTTagCompound> {

    public static final long PER_PIPE_CAPACITY = 256L;

    private long capacity = 0L;
    private long energyStored = 0L;

    public void setEnergyStored(final long energyStored) {
        this.energyStored = energyStored;
    }

    @Override
    public long acceptEnergyFromNetwork(final EnumFacing side, final long voltage, final long amperage) {
        final long canAccept = getEnergyCapacity() - getEnergyStored();
        if (voltage > 0L && amperage > 0L && (side == null || inputsEnergy(side))) {
            if (canAccept >= voltage) {
                long amperesAccepted = Math.min(canAccept / voltage, Math.min(amperage, getInputAmperage()));
                if (amperesAccepted > 0) {
                    setEnergyStored(getEnergyStored() + voltage * amperesAccepted);
                    return amperesAccepted;
                }
            }
        }
        return 0L;
    }

    @Override
    public boolean inputsEnergy(final EnumFacing side) {
        return true;
    }

    @Override
    public long changeEnergy(final long differenceAmount) {
        final long oldEnergyStored = getEnergyStored();
        long newEnergyStored = (this.capacity - oldEnergyStored < differenceAmount) ? this.capacity : (oldEnergyStored + differenceAmount);
        if (newEnergyStored < 0)
            newEnergyStored = 0;
        setEnergyStored(newEnergyStored);
        return newEnergyStored - oldEnergyStored;
    }

    @Override
    public long getEnergyStored() {
        return this.energyStored;
    }

    @Override
    public long getEnergyCapacity() {
        return this.capacity;
    }

    @Override
    public long getInputAmperage() {
        return 1L;
    }

    @Override
    public long getInputVoltage() {
        // Review: Accept any voltage, really constrained by number of pipes?
        return Long.MAX_VALUE;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        final NBTTagCompound compound = new NBTTagCompound();
        compound.setLong("Capacity", this.capacity);
        compound.setLong("EnergyStored", this.energyStored);
        return compound;
    }

    @Override
    public void deserializeNBT(final NBTTagCompound compound) {
        this.capacity = compound.getLong("Capacity");
        this.energyStored = compound.getLong("EnergyStored");
    }

    public void updateEnergyCapacity(final long newEnergyCapacity) {
        this.capacity = newEnergyCapacity;
        this.energyStored = Math.min(this.energyStored, this.capacity);
    }
}
