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

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.items.ItemStackHandler;

public class KeepInStockInfo extends ItemStackHandler{

    public static final String BUSY = "keep.in.stock.status.busy";
    public static final String CLEAN_OUTPUTS = "keep.in.stock.status.cleaning";
    public static final String IN_STOCK = "keep.in.stock.status.instock";
    public static final String INVENTORY = "keep.in.stock.status.inventory";
    public static final String NA = "keep.in.stock.status.na";
    public static final String NO_ACCEPT = "keep.in.stock.status.noaccept";
    public static final String NO_INGREDIENTS = "keep.in.stock.status.noingredients";
    public static final String NO_MACHINE = "keep.in.stock.status.nomachine";
    public static final String NO_RECIPE = "keep.in.stock.status.norecipe";
    public static final String PROCESSING = "keep.in.stock.status.processing";
    public static final String TOO_BIG = "keep.in.stock.status.toobig";
    public static final String WRONG_TIER = "keep.in.stock.status.wrongtier";

    private final int slotLimit;

    private String status = NA;

    public KeepInStockInfo(final int slotLimit) {
        this.slotLimit = slotLimit;
    }

    public ItemStack getItemStack() {
        return getStackInSlot(0);
    }

    public void setItemStack(final ItemStack itemStack) {
        setStackInSlot(0, itemStack);
    }

    public void displayStatus(final List<ITextComponent> text) {
        text.add((ITextComponent) new TextComponentTranslation(this.status));
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(final String status) {
        // TODO fix kludges
        if (this.status == PROCESSING && (status == BUSY || status == INVENTORY)) {
            return;
        }
        this.status = status;
    }

    @Override
    public int getSlotLimit(final int slot) {
        return this.slotLimit;
    }
}
