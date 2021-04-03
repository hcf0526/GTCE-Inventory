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

import gregtech.api.pipenet.PipeNet;
import gregtech.api.pipenet.WorldPipeNet;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public abstract class TickableWorldPipeNet<NodeDataType, T extends PipeNet<NodeDataType> & ITickable> extends WorldPipeNet<NodeDataType, T> {

    // Review: Protected against CCME (iteration/modification conflict)
    private Map<T, List<ChunkPos>> loadedChunksByPipeNet = new ConcurrentHashMap<>();
    private List<ITickable> tickingPipeNets = new CopyOnWriteArrayList<>();

    public TickableWorldPipeNet(String name) {
        super(name);
    }

    private boolean isChunkLoaded(ChunkPos chunkPos) {
        WorldServer worldServer = (WorldServer) getWorld();
        return worldServer.getChunkProvider().chunkExists(chunkPos.x, chunkPos.z);
    }

    protected abstract int getUpdateRate();

    public void update() {
        if (getWorld().getTotalWorldTime() % getUpdateRate() == 0L) {
            tickingPipeNets.forEach(ITickable::update);
        }
    }

    public void onChunkLoaded(Chunk chunk) {
        ChunkPos chunkPos = chunk.getPos();
        // Review: NPE
        List<T> pipeNetsInThisChunk = this.pipeNetsByChunk.getOrDefault(chunkPos, Collections.emptyList());
        for (T pipeNet : pipeNetsInThisChunk) {
            List<ChunkPos> loadedChunks = getOrCreateChunkListForPipeNet(pipeNet);
            if (loadedChunks.isEmpty()) {
                this.tickingPipeNets.add(pipeNet);
            }
            loadedChunks.add(chunkPos);
        }
    }

    public void onChunkUnloaded(Chunk chunk) {
        ChunkPos chunkPos = chunk.getPos();
        // Review: NPE
        List<T> pipeNetsInThisChunk = this.pipeNetsByChunk.getOrDefault(chunkPos, Collections.emptyList());
        for (T pipeNet : pipeNetsInThisChunk) {
            List<ChunkPos> loadedChunks = this.loadedChunksByPipeNet.get(pipeNet);
            if (loadedChunks != null && loadedChunks.contains(chunkPos)) {
                loadedChunks.remove(chunkPos);
                if (loadedChunks.isEmpty()) {
                    removeFromTicking(pipeNet);
                }
            }
        }
    }

    @Override
    protected void onWorldSet() {
        super.onWorldSet();
        Map<T, List<ChunkPos>> pipeNetByLoadedChunks = pipeNets.stream()
            .map(pipeNet -> Pair.of(pipeNet, getPipeNetLoadedChunks(pipeNet)))
            .filter(pair -> !pair.getRight().isEmpty())
            .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
        if (!pipeNetByLoadedChunks.isEmpty()) {
            this.tickingPipeNets.addAll(pipeNetByLoadedChunks.keySet());
            this.loadedChunksByPipeNet.putAll(pipeNetByLoadedChunks);
        }
    }

    @Override
    protected void addPipeNet(T pipeNet) {
        super.addPipeNet(pipeNet);
        List<ChunkPos> loadedChunks = getPipeNetLoadedChunks(pipeNet);
        if (!loadedChunks.isEmpty()) {
            this.loadedChunksByPipeNet.put(pipeNet, loadedChunks);
            this.tickingPipeNets.add(pipeNet);
        }
    }

    private List<ChunkPos> getPipeNetLoadedChunks(T pipeNet) {
        return pipeNet.getContainedChunks().stream()
            .filter(this::isChunkLoaded)
            .collect(Collectors.toList());
    }

    @Override
    protected void removePipeNet(T pipeNet) {
        super.removePipeNet(pipeNet);
        if (loadedChunksByPipeNet.containsKey(pipeNet)) {
            removeFromTicking(pipeNet);
        }
    }

    @SuppressWarnings("unlikely-arg-type")
    private void removeFromTicking(T pipeNet) {
        this.loadedChunksByPipeNet.remove(pipeNet);
        this.tickingPipeNets.remove(pipeNet);
    }

    private List<ChunkPos> getOrCreateChunkListForPipeNet(T pipeNet) {
        return this.loadedChunksByPipeNet.computeIfAbsent(pipeNet, k -> new ArrayList<>());
    }

    @Override
    protected void addPipeNetToChunk(ChunkPos chunkPos, T pipeNet) {
        super.addPipeNetToChunk(chunkPos, pipeNet);
        if (isChunkLoaded(chunkPos)) {
            List<ChunkPos> loadedChunks = getOrCreateChunkListForPipeNet(pipeNet);
            if (loadedChunks.isEmpty()) {
                this.tickingPipeNets.add(pipeNet);
            }
            loadedChunks.add(chunkPos);
        }
    }

    @Override
    protected void removePipeNetFromChunk(ChunkPos chunkPos, T pipeNet) {
        super.removePipeNetFromChunk(chunkPos, pipeNet);
        List<ChunkPos> loadedChunks = this.loadedChunksByPipeNet.get(pipeNet);
        if (loadedChunks != null && loadedChunks.contains(chunkPos)) {
            loadedChunks.remove(chunkPos);
            if (loadedChunks.isEmpty()) {
                removeFromTicking(pipeNet);
            }
        }
    }
}
