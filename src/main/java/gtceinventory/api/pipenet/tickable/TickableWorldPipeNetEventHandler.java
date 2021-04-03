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
package gtceinventory.api.pipenet.tickable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import gtceinventory.GTCEInventory;
import net.minecraft.world.World;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;

@EventBusSubscriber(modid = GTCEInventory.MODID)
public class TickableWorldPipeNetEventHandler {

    private static final List<Function<World, TickableWorldPipeNet<?, ?>>> pipeNetAccessors = new ArrayList<>();

    public static void registerTickablePipeNet(Function<World, TickableWorldPipeNet<?, ?>> pipeNetAccessor) {
        pipeNetAccessors.add(pipeNetAccessor);
    }

    private static Stream<TickableWorldPipeNet<?, ?>> getPipeNetsForWorld(World world) {
        return pipeNetAccessors.stream().map(accessor -> accessor.apply(world));
    }

    @SubscribeEvent
    public static void onWorldTick(WorldTickEvent event) {
        final World world = event.world;
        if (world == null || world.isRemote)
            return;
        getPipeNetsForWorld(world).forEach(TickableWorldPipeNet::update);
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        final World world = event.getWorld();
        if (world == null || world.isRemote)
            return;
        getPipeNetsForWorld(world).forEach(it -> it.onChunkLoaded(event.getChunk()));
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        final World world = event.getWorld();
        if (world == null || world.isRemote)
            return;
        getPipeNetsForWorld(world).forEach(it -> it.onChunkUnloaded(event.getChunk()));
    }
}
