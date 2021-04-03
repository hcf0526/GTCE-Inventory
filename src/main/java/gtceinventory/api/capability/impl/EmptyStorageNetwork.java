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
package gtceinventory.api.capability.impl;

import java.util.Collections;
import java.util.Set;

import gregtech.api.util.ItemStackKey;
import gregtech.common.inventory.IItemInfo;
import gtceinventory.api.capability.IStorageNetwork;

public class EmptyStorageNetwork implements IStorageNetwork {
    
    public static IStorageNetwork INSTANCE = new EmptyStorageNetwork();

    @Override
    public Set<ItemStackKey> getStoredItems() {
        return Collections.emptySet();
    }

    @Override
    public IItemInfo getItemInfo(ItemStackKey stackKey) {
        return null;
    }

    @Override
    public int insertItem(ItemStackKey itemStack, int amount, boolean simulate) {
        return 0;
    }

    @Override
    public int extractItem(ItemStackKey itemStack, int amount, boolean simulate) {
        return 0;
    }
}