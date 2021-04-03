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
package gtceinventory.integration.multipart;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import codechicken.lib.data.MCDataInput;
import codechicken.multipart.MultiPartRegistry;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.api.IDynamicPartFactory;
import gtceinventory.GTCEInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public final class GTCEInventoryMultipartFactory implements IDynamicPartFactory {

    public static final ResourceLocation INVENTORY_PIPE_PART_KEY = new ResourceLocation(GTCEInventory.MODID, "inv_pipe");
    public static final ResourceLocation INVENTORY_PIPE_TICKABLE_PART_KEY = new ResourceLocation(GTCEInventory.MODID, "inv_pipe_tickable");

    public static final GTCEInventoryMultipartFactory INSTANCE = new GTCEInventoryMultipartFactory();
    private final Map<ResourceLocation, Supplier<TMultiPart>> partRegistry = new HashMap<>();

    public void registerFactory() {
        registerPart(INVENTORY_PIPE_PART_KEY, InventoryPipeMultiPart::new);
        registerPart(INVENTORY_PIPE_TICKABLE_PART_KEY, InventoryPipeMultiPartTickable::new);
        MultiPartRegistry.registerParts(this, partRegistry.keySet());
    }

    private void registerPart(ResourceLocation identifier, Supplier<TMultiPart> supplier) {
        partRegistry.put(identifier, supplier);
    }

    @Override
    public TMultiPart createPartServer(ResourceLocation identifier, NBTTagCompound compound) {
        return createPart(identifier);
    }

    @Override
    public TMultiPart createPartClient(ResourceLocation identifier, MCDataInput packet) {
        return createPart(identifier);
    }

    public TMultiPart createPart(ResourceLocation identifier) {
        if (partRegistry.containsKey(identifier)) {
            Supplier<TMultiPart> supplier = partRegistry.get(identifier);
            return supplier.get();

        }
        return null;
    }
}
