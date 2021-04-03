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

import com.google.common.base.Preconditions;

import gregtech.api.capability.IEnergyContainer;
import gregtech.api.pipenet.Node;
import gregtech.api.pipenet.PipeNet;
import gregtech.api.pipenet.WorldPipeNet;
import gregtech.api.pipenet.block.simple.EmptyNodeData;
import gtceinventory.common.pipelike.inventory.network.ItemStorageNetwork;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

import java.util.Map;

public class InventoryPipeNet extends PipeNet<EmptyNodeData> implements ITickable {

    private ItemStorageNetwork storageNetwork;

    private InventoryPipeNetEnergyContainer energyContainer;
    
    @SuppressWarnings("rawtypes")
    public InventoryPipeNet(WorldPipeNet<EmptyNodeData, ? extends PipeNet> world) {
        super(world);
        this.energyContainer = new InventoryPipeNetEnergyContainer();
    }

    @Override
    public void update() {
        ItemStorageNetwork storageNetwork = getStorageNetwork();
        storageNetwork.update();
    }

    @Override
    protected void updateBlockedConnections(BlockPos nodePos, EnumFacing facing, boolean isBlocked) {
        super.updateBlockedConnections(nodePos, facing, isBlocked);
        getStorageNetwork().handleBlockedConnectionChange(nodePos, facing, isBlocked);
    }

    public void nodeNeighbourChanged(BlockPos nodePos) {
        if (containsNode(nodePos)) {
            int blockedConnections = getNodeAt(nodePos).blockedConnections;
            getStorageNetwork().checkForItemHandlers(nodePos, blockedConnections);
        }
    }

    @Override
    protected void onConnectionsUpdate() {
        super.onConnectionsUpdate();
        final long newEnergyCapacity = InventoryPipeNetEnergyContainer.PER_PIPE_CAPACITY * getAllNodes().size();
        this.energyContainer.updateEnergyCapacity(newEnergyCapacity);
    }

    @Override
    protected void transferNodeData(final Map<BlockPos, Node<EmptyNodeData>> transferredNodes, final PipeNet<EmptyNodeData> parentNet) {
        super.transferNodeData(transferredNodes, parentNet);
        final InventoryPipeNet parentInventoryNet = (InventoryPipeNet) parentNet;
        final InventoryPipeNetEnergyContainer parentEnergyContainer = parentInventoryNet.energyContainer;
        final long parentEnergy = parentEnergyContainer.getEnergyStored();
        if (parentEnergy > 0) {
            if (parentNet.getAllNodes().isEmpty()) {
                //if this is a merge of pipe nets, just add all the energy
                this.energyContainer.addEnergy(parentEnergy);
            } else {
                //otherwise, it is donating of some nodes to our net in result of split
                //so, we should estabilish equal amount of energy in networks
                long firstNetCapacity = this.energyContainer.getEnergyCapacity();
                long secondNetCapacity = parentInventoryNet.energyContainer.getEnergyCapacity();
                long totalEnergy = this.energyContainer.getEnergyStored() + parentEnergy;
                long energy1 = totalEnergy * firstNetCapacity / (firstNetCapacity + secondNetCapacity);
                long energy2 = totalEnergy - energy1;

                energyContainer.setEnergyStored(energy1);
                parentEnergyContainer.setEnergyStored(energy2);
            }
        }
        if (parentInventoryNet.storageNetwork != null) {
            parentInventoryNet.storageNetwork.transferItemHandlers(transferredNodes.keySet(), getStorageNetwork());
        }
    }

    @Override
    protected void addNodeSilently(BlockPos nodePos, Node<EmptyNodeData> node) {
        super.addNodeSilently(nodePos, node);
        // Review: Correct place? Looks for adjacent inventories during deserialisation 
        getStorageNetwork().checkForItemHandlers(nodePos, node.blockedConnections);
    }

    @Override
    protected Node<EmptyNodeData> removeNodeWithoutRebuilding(BlockPos nodePos) {
        getStorageNetwork().removeItemHandlers(nodePos);
        return super.removeNodeWithoutRebuilding(nodePos);
    }

    public ItemStorageNetwork getStorageNetwork() {
        if (storageNetwork == null) {
            Preconditions.checkNotNull(getWorldData(), "World is null at the time getStorageNetwork is called!");
            this.storageNetwork = new ItemStorageNetwork(this);
        }
        return storageNetwork;
    }

    public IEnergyContainer getEnergyContainer() {
        return energyContainer;
    }

    @Override
    protected void writeNodeData(EmptyNodeData nodeData, NBTTagCompound tagCompound) {
    }

    @Override
    protected EmptyNodeData readNodeData(NBTTagCompound tagCompound) {
        return EmptyNodeData.INSTANCE;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        final NBTTagCompound nbt = super.serializeNBT();
        nbt.setTag("EnergyContainer", this.energyContainer.serializeNBT());
        return nbt;
    }

    @Override
    public void deserializeNBT(final NBTTagCompound nbt) {
        super.deserializeNBT(nbt);
        final NBTTagCompound energyData = nbt.getCompoundTag("EnergyContainer");
        this.energyContainer.deserializeNBT(energyData);
    }
}
