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
package gtceinventory.common.pipelike.inventory.network;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import gregtech.api.util.ItemStackKey;
import gregtech.common.inventory.itemsource.ItemSource;
import gregtech.common.inventory.itemsource.ItemSourceList;
import gregtech.common.inventory.itemsource.sources.TileItemSource;
import gregtech.common.pipelike.inventory.network.UpdateResult;
import gtceinventory.api.capability.IStorageNetwork;
import gtceinventory.common.GTCEInventoryConfig;
import gtceinventory.common.pipelike.inventory.net.InventoryPipeNet;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class ItemStorageNetwork extends ItemSourceList implements IStorageNetwork {

    private final Map<SidedBlockPos, TileItemSource> handlerInfoMap = new ConcurrentHashMap<>();

    private final InventoryPipeNet pipeNet;
    
    public ItemStorageNetwork(InventoryPipeNet pipeNet) {
        super(pipeNet.getWorldData());
        this.pipeNet = pipeNet;
    }

    // Review: Exposure for TOP debugging
    public Collection<TileItemSource> getHandlerInfos()
    {
        return Collections.unmodifiableCollection(handlerInfoMap.values());
    }

    @Override
    public boolean hasItemStored(ItemStackKey itemStackKey) {
        return getItemInfo(itemStackKey) != null;
    }

    @Override
    public int insertItem(ItemStackKey itemStack, int amount, boolean simulate) {
        return insertItem(itemStack, amount, simulate, InsertMode.LOWEST_PRIORITY);
    }

    @Override
    public int extractItem(final ItemStackKey itemStack, final int amount, final boolean simulate) {
        if (!checkEnergy(amount)) {
            return 0;
        }
        final int result = super.extractItem(itemStack, amount, simulate);
        if (!simulate) {
            drainEnergy(result);
        }
        return result;
    }

    @Override
    public int insertItem(final ItemStackKey itemStack, final int amount, final boolean simulate, final InsertMode mode) {
        if (!checkEnergy(amount)) {
            return 0;
        }
        final int result = super.insertItem(itemStack, amount, simulate, mode);
        if (!simulate) {
            drainEnergy(result);
        }
        return result;
    }

    public void transferItemHandlers(Collection<BlockPos> nodePositions, ItemStorageNetwork destNetwork) {
        List<ItemSource> movedHandlerInfo = handlerInfoList.stream()
            .filter(handlerInfo -> handlerInfo instanceof TileItemSource)
            .filter(handlerInfo -> nodePositions.contains(((TileItemSource) handlerInfo).getBlockPos()))
            .collect(Collectors.toList());
        movedHandlerInfo.forEach(this::removeItemHandler);
        movedHandlerInfo.forEach(destNetwork::addItemHandler);
    }

    public void handleBlockedConnectionChange(BlockPos nodePos, EnumFacing side, boolean isBlockedNow) {
        if (isBlockedNow) {
            SidedBlockPos blockPos = new SidedBlockPos(nodePos, side);
            TileItemSource handlerInfo = handlerInfoMap.get(blockPos);
            if (handlerInfo != null) {
                removeItemHandler(handlerInfo);
            }
        } else {
            TileItemSource handlerInfo = new TileItemSource(getWorld(), nodePos, side);
            //just add unchecked item handler, addItemHandler will refuse
            //to add item handler if it's updateCache will return UpdateResult.INVALID
            //avoids duplicating logic here
            addItemHandler(handlerInfo);
        }
    }

    public void checkForItemHandlers(BlockPos nodePos, int blockedConnections) {
        for (EnumFacing accessSide : EnumFacing.VALUES) {
            //skip sides reported as blocked by pipe network
            if ((blockedConnections & 1 << accessSide.getIndex()) > 0) continue;
            //check for existing item handler
            SidedBlockPos blockPos = new SidedBlockPos(nodePos, accessSide);
            if (handlerInfoMap.containsKey(blockPos)) {
                TileItemSource handlerInfo = handlerInfoMap.get(blockPos);
                if (handlerInfo.update() == UpdateResult.INVALID) {
                    removeItemHandler(handlerInfo);
                }
            } else {
                TileItemSource handlerInfo = new TileItemSource(getWorld(), nodePos, accessSide);
                //just add unchecked item handler, addItemHandler will refuse
                //to add item handler if it's updateCache will return UpdateResult.INVALID
                //avoids duplicating logic here
                addItemHandler(handlerInfo);
            }
        }
    }

    public void removeItemHandlers(BlockPos nodePos) {
        for (EnumFacing accessSide : EnumFacing.VALUES) {
            ItemSource handlerInfo = handlerInfoMap.get(new SidedBlockPos(nodePos, accessSide));
            if (handlerInfo != null)
                removeItemHandler(handlerInfo);
        }
    }

    @Override
    protected void addItemHandlerPost(ItemSource handlerInfo) {
        if (handlerInfo instanceof TileItemSource) {
            this.handlerInfoMap.put(handlerPosition((TileItemSource) handlerInfo), (TileItemSource) handlerInfo);
        }
    }

    @Override
    protected void removeItemHandlerPost(ItemSource handlerInfo) {
        if (handlerInfo instanceof TileItemSource) {
            this.handlerInfoMap.remove(handlerPosition((TileItemSource) handlerInfo));
        }
    }

    protected long calculateEnergy(final long amount) {
        if (amount > 0 && (GTCEInventoryConfig.energyPerOperation > 0 || GTCEInventoryConfig.energyPerItem > 0)) {
           return GTCEInventoryConfig.energyPerOperation + amount * GTCEInventoryConfig.energyPerItem;
        }
        return 0;
    }

    protected boolean checkEnergy(final int amount) {
        return this.pipeNet.getEnergyContainer().getEnergyStored() >= calculateEnergy(amount);
    }

    protected void drainEnergy(final int amount) {
        this.pipeNet.getEnergyContainer().removeEnergy(calculateEnergy(amount));
    }

    private static SidedBlockPos handlerPosition(TileItemSource handlerInfo) {
        return new SidedBlockPos(handlerInfo.getBlockPos(), handlerInfo.getAccessSide());
    }

    private static class SidedBlockPos {
        private final BlockPos blockPos;
        private final EnumFacing accessSide;

        public SidedBlockPos(BlockPos blockPos, EnumFacing accessSide) {
            this.blockPos = blockPos;
            this.accessSide = accessSide;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SidedBlockPos)) return false;
            SidedBlockPos that = (SidedBlockPos) o;
            return Objects.equals(blockPos, that.blockPos) &&
                accessSide == that.accessSide;
        }

        @Override
        public int hashCode() {
            return Objects.hash(blockPos, accessSide);
        }
    }
}
