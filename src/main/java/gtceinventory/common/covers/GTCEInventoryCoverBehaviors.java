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
package gtceinventory.common.covers;

import java.util.function.BiFunction;

import gregtech.api.GTValues;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.ICoverable;
import gregtech.api.items.metaitem.MetaItem.MetaValueItem;
import gtceinventory.GTCEInventory;
import gtceinventory.common.items.GTCEInventoryMetaItems;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

public class GTCEInventoryCoverBehaviors {

    public static void init() {
        GTCEInventory.LOGGER.info("Registering cover behaviors...");

        // Review: Add other tiers
        registerBehavior(701, new ResourceLocation(GTCEInventory.MODID, "interface.lv"), GTCEInventoryMetaItems.INTERFACE_MODULE_LV, (tile, side) -> new CoverStorageNetworkInterface(tile, side, GTValues.LV, 8));
        registerBehavior(721, new ResourceLocation(GTCEInventory.MODID, "keep_in_stock.lv"), GTCEInventoryMetaItems.KEEP_IN_STOCK_MODULE_LV, (tile, side) -> new CoverKeepInStock(tile, side, GTValues.LV, 8));
    }

    @SuppressWarnings("rawtypes")
    public static void registerBehavior(int coverNetworkId, ResourceLocation coverId, MetaValueItem placerItem, BiFunction<ICoverable, EnumFacing, CoverBehavior> behaviorCreator) {
        gregtech.common.covers.CoverBehaviors.registerBehavior(coverNetworkId, coverId, placerItem, behaviorCreator);
    }
}
