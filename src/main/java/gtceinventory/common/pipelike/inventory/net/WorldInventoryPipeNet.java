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

import gregtech.api.pipenet.block.simple.EmptyNodeData;
import gtceinventory.api.pipenet.tickable.TickableWorldPipeNet;
import net.minecraft.world.World;

public class WorldInventoryPipeNet extends TickableWorldPipeNet<EmptyNodeData, InventoryPipeNet> {

    private static final String DATA_ID_BASE = "gregtech.inventory_pipe_net";

    public static String getDataID(final String baseID, final World world) {
        if (world == null || world.isRemote)
            throw new RuntimeException("WorldPipeNet should only be created on the server!");
        final int dimension = world.provider.getDimension();
        return baseID + '.' + dimension;
    }

    // Review: HACK. Used to make the world available during initial loading of the pipe nets
    //         Without it you get an NPE when isChunkLoaded is invoked in the parent class during pipenet deserialization
    //         i.e. before setWorldAndInit is done.
    //         A proper (more complicated) fix would be to defer some of the PipeNet.addNodeSilently processing in deserializeAllNodes 
    //         to setWorldAndInit
    static final ThreadLocal<World> loadingWorld = new ThreadLocal<World>();

    public static WorldInventoryPipeNet getWorldPipeNet(World world) {
        final String DATA_ID = getDataID(DATA_ID_BASE, world);
        WorldInventoryPipeNet netWorldData;
        loadingWorld.set(world);
        try {
            netWorldData = (WorldInventoryPipeNet) world.loadData(WorldInventoryPipeNet.class, DATA_ID);
            if (netWorldData == null) {
                netWorldData = new WorldInventoryPipeNet(DATA_ID);
                world.setData(DATA_ID, netWorldData);
            }
        }
        finally
        {
            loadingWorld.set(null);
        }
        netWorldData.setWorldAndInit(world);
        return netWorldData;
    }

    public WorldInventoryPipeNet(String name) {
        super(name);
    }

    @Override
    public World getWorld()
    {
        final World result = loadingWorld.get();
        return result != null ? result : super.getWorld();
    }

    @Override
    protected int getUpdateRate() {
        return 20;
    }

    @Override
    protected InventoryPipeNet createNetInstance() {
        return new InventoryPipeNet(this);
    }
}
