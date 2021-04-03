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

import static gtceinventory.common.items.GTCEInventoryMetaItems.INTERFACE_MODULE_LV;
import static gtceinventory.common.items.GTCEInventoryMetaItems.KEEP_IN_STOCK_MODULE_LV;

import gregtech.api.items.materialitem.MaterialMetaItem;
import gregtech.api.items.metaitem.MetaItem;
import gtceinventory.GTCEInventory;
import net.minecraft.util.ResourceLocation;

public class GTCEInventoryMetaItem extends MaterialMetaItem {

    public GTCEInventoryMetaItem() {
    }

    @Override
    public void registerSubItems() {

        INTERFACE_MODULE_LV = addItem(001, "interface.module.lv");

        KEEP_IN_STOCK_MODULE_LV = addItem(021, "keep_in_stock.module.lv");
    }

    @Override
    public ResourceLocation createItemModelPath(MetaItem<?>.MetaValueItem metaValueItem, String postfix) {
        return new ResourceLocation(GTCEInventory.MODID, formatModelPath(metaValueItem) + postfix);
    }
}