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

        registerBehavior(701, new ResourceLocation(GTCEInventory.MODID, "interface.lv"), GTCEInventoryMetaItems.INTERFACE_MODULE_LV, (tile, side) -> new CoverStorageNetworkInterface(tile, side, GTValues.LV, 8));
        registerBehavior(702, new ResourceLocation(GTCEInventory.MODID, "interface.mv"), GTCEInventoryMetaItems.INTERFACE_MODULE_MV, (tile, side) -> new CoverStorageNetworkInterface(tile, side, GTValues.MV, 32));
        registerBehavior(703, new ResourceLocation(GTCEInventory.MODID, "interface.hv"), GTCEInventoryMetaItems.INTERFACE_MODULE_HV, (tile, side) -> new CoverStorageNetworkInterface(tile, side, GTValues.MV, 64));
        registerBehavior(704, new ResourceLocation(GTCEInventory.MODID, "interface.ev"), GTCEInventoryMetaItems.INTERFACE_MODULE_EV, (tile, side) -> new CoverStorageNetworkInterface(tile, side, GTValues.MV, 3*64));
        registerBehavior(705, new ResourceLocation(GTCEInventory.MODID, "interface.iv"), GTCEInventoryMetaItems.INTERFACE_MODULE_IV, (tile, side) -> new CoverStorageNetworkInterface(tile, side, GTValues.MV, 8*64));
        registerBehavior(706, new ResourceLocation(GTCEInventory.MODID, "interface.luv"), GTCEInventoryMetaItems.INTERFACE_MODULE_LUV, (tile, side) -> new CoverStorageNetworkInterface(tile, side, GTValues.MV, 16*64));
        registerBehavior(707, new ResourceLocation(GTCEInventory.MODID, "interface.zpm"), GTCEInventoryMetaItems.INTERFACE_MODULE_ZPM, (tile, side) -> new CoverStorageNetworkInterface(tile, side, GTValues.MV, 16*64));
        registerBehavior(708, new ResourceLocation(GTCEInventory.MODID, "interface.uv"), GTCEInventoryMetaItems.INTERFACE_MODULE_UV, (tile, side) -> new CoverStorageNetworkInterface(tile, side, GTValues.MV, 16*64));

        registerBehavior(721, new ResourceLocation(GTCEInventory.MODID, "stock.lv"), GTCEInventoryMetaItems.STOCK_MODULE_LV, (tile, side) -> new CoverKeepInStock(tile, side, GTValues.LV, 8));
        registerBehavior(722, new ResourceLocation(GTCEInventory.MODID, "stock.mv"), GTCEInventoryMetaItems.STOCK_MODULE_MV, (tile, side) -> new CoverKeepInStock(tile, side, GTValues.LV, 32));
        registerBehavior(723, new ResourceLocation(GTCEInventory.MODID, "stock.hv"), GTCEInventoryMetaItems.STOCK_MODULE_HV, (tile, side) -> new CoverKeepInStock(tile, side, GTValues.LV, 64));
        registerBehavior(724, new ResourceLocation(GTCEInventory.MODID, "stock.ev"), GTCEInventoryMetaItems.STOCK_MODULE_EV, (tile, side) -> new CoverKeepInStock(tile, side, GTValues.LV, 3*64));
        registerBehavior(725, new ResourceLocation(GTCEInventory.MODID, "stock.iv"), GTCEInventoryMetaItems.STOCK_MODULE_IV, (tile, side) -> new CoverKeepInStock(tile, side, GTValues.LV, 8*64));
        registerBehavior(726, new ResourceLocation(GTCEInventory.MODID, "stock.luv"), GTCEInventoryMetaItems.STOCK_MODULE_LUV, (tile, side) -> new CoverKeepInStock(tile, side, GTValues.LV, 16*64));
        registerBehavior(727, new ResourceLocation(GTCEInventory.MODID, "stock.zpm"), GTCEInventoryMetaItems.STOCK_MODULE_ZPM, (tile, side) -> new CoverKeepInStock(tile, side, GTValues.LV, 16*64));
        registerBehavior(728, new ResourceLocation(GTCEInventory.MODID, "stock.uv"), GTCEInventoryMetaItems.STOCK_MODULE_UV, (tile, side) -> new CoverKeepInStock(tile, side, GTValues.LV, 16*64));
    }

    @SuppressWarnings("rawtypes")
    public static void registerBehavior(int coverNetworkId, ResourceLocation coverId, MetaValueItem placerItem, BiFunction<ICoverable, EnumFacing, CoverBehavior> behaviorCreator) {
        gregtech.common.covers.CoverBehaviors.registerBehavior(coverNetworkId, coverId, placerItem, behaviorCreator);
    }
}
