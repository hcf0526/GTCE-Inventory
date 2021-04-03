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
package gtceinventory.common.metatileentities;

import gregtech.api.GregTechAPI;
import gtceinventory.GTCEInventory;
import gtceinventory.common.metatileentities.storage.MetaTileEntityWorkbench;
import net.minecraft.util.ResourceLocation;

public class GTCEInventoryMetaTileEntities {

    public static MetaTileEntityWorkbench WORKBENCH;


    public static void init() {
        GTCEInventory.LOGGER.info("Registering MetaTileEntities");

        // Id should be temporary?
        WORKBENCH = GregTechAPI.registerMetaTileEntity(7890, new MetaTileEntityWorkbench(new ResourceLocation(GTCEInventory.MODID, "workbench")));
    }
}
