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
package gtceinventory.common.items;

import static gtceinventory.common.items.GTCEInventoryMetaItems.*;

import gregtech.api.items.materialitem.MaterialMetaItem;
import gregtech.api.items.metaitem.MetaItem;
import gtceinventory.GTCEInventory;
import net.minecraft.util.ResourceLocation;

public class GTCEInventoryMetaItem extends MaterialMetaItem {

    public GTCEInventoryMetaItem() {
    }

    @Override
    public void registerSubItems() {

        INTERFACE_MODULE_LV = addItem(1, "interface.module.lv");
        INTERFACE_MODULE_MV = addItem(2, "interface.module.mv");
        INTERFACE_MODULE_HV = addItem(3, "interface.module.hv");
        INTERFACE_MODULE_EV = addItem(4, "interface.module.ev");
        INTERFACE_MODULE_IV = addItem(5, "interface.module.iv");
        INTERFACE_MODULE_LUV = addItem(6, "interface.module.luv");
        INTERFACE_MODULE_ZPM = addItem(7, "interface.module.zpm");
        INTERFACE_MODULE_UV = addItem(8, "interface.module.uv");

        STOCK_MODULE_LV = addItem(21, "stock.module.lv");
        STOCK_MODULE_MV = addItem(22, "stock.module.mv");
        STOCK_MODULE_HV = addItem(23, "stock.module.hv");
        STOCK_MODULE_EV = addItem(24, "stock.module.ev");
        STOCK_MODULE_IV = addItem(25, "stock.module.iv");
        STOCK_MODULE_LUV = addItem(26, "stock.module.luv");
        STOCK_MODULE_ZPM = addItem(27, "stock.module.zpm");
        STOCK_MODULE_UV = addItem(28, "stock.module.uv");
    }

    @Override
    public ResourceLocation createItemModelPath(MetaItem<?>.MetaValueItem metaValueItem, String postfix) {
        return new ResourceLocation(GTCEInventory.MODID, formatModelPath(metaValueItem) + postfix);
    }
}