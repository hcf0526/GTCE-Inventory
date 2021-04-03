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
package gtceinventory.common.pipelike.inventory;

import gregtech.api.pipenet.block.IPipeType;
import gregtech.api.pipenet.block.simple.EmptyNodeData;

public enum InventoryPipeType implements IPipeType<EmptyNodeData> {
    NORMAL("normal", 0.4f, true);

    private final String name;
    private final float thickness;
    private final boolean isPaintable;

    InventoryPipeType(String name, float thickness, boolean isPaintable) {
        this.name = name;
        this.thickness = thickness;
        this.isPaintable = isPaintable;
    }

    @Override
    public float getThickness() {
        return thickness;
    }

    @Override
    public EmptyNodeData modifyProperties(EmptyNodeData baseProperties) {
        return baseProperties;
    }

    @Override
    public boolean isPaintable() {
        return isPaintable;
    }

    @Override
    public String getName() {
        return name;
    }
}
