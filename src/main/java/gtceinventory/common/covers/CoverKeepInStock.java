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

import java.util.ArrayList;
import java.util.List;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.IWorkable;
import gregtech.api.capability.impl.AbstractRecipeLogic;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.CoverWithUI;
import gregtech.api.cover.ICoverable;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.gui.widgets.SimpleTextWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.recipes.CountableIngredient;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.render.Textures;
import gregtech.api.unification.stack.ItemAndMetadata;
import gregtech.api.util.ItemStackKey;
import gregtech.common.covers.filter.ItemFilterWrapper;
import gregtech.common.covers.filter.SimpleItemFilter;
import gregtech.common.inventory.IItemInfo;
import gregtech.common.items.MetaItems;
import gregtech.common.metatileentities.electric.multiblockpart.MetaTileEntityMultiblockPart;
import gtceinventory.api.capability.GTCEInventoryCapabilities;
import gtceinventory.api.capability.IStorageNetwork;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

/*
 * This is a cover for something that has the IStorageNetwork capability i.e. an inventory pipe
 * 
 *  It allows the specification of items to keep in stock which will be crafted using recipes from the adjacent machine.
 */
public class CoverKeepInStock extends CoverBehavior implements CoverWithUI, ITickable, IControllable {

    public final int tier;
    public final long EUt;
    public final int maxItemTransferRate;
    protected int transferRate;
    protected int itemsLeftToTransferLastSecond;
    protected boolean isWorkingAllowed = true;
    protected final ItemFilterWrapper itemFilter;

    public CoverKeepInStock(final ICoverable coverable, final EnumFacing attachedSide, final int tier, final int itemsPerSecond) {
        super(coverable, attachedSide);
        this.tier = tier;
        this.EUt = GTValues.V[tier];
        this.maxItemTransferRate = itemsPerSecond;
        this.transferRate = maxItemTransferRate;
        this.itemsLeftToTransferLastSecond = transferRate;
        // Hack: reusing item filter gui for keep in stock config
        this.itemFilter = new ItemFilterWrapper(this);
        this.itemFilter.setItemFilter(new SimpleItemFilter());
        this.itemFilter.setMaxStackSize(tier*8);
    }

    protected void setTransferRate(int transferRate) {
        this.transferRate = transferRate;
        coverHolder.markDirty();
    }

    protected void adjustTransferRate(int amount) {
        setTransferRate(MathHelper.clamp(transferRate + amount, 1, maxItemTransferRate));
    }

    @Override
    public void update() {
        final long timer = coverHolder.getTimer();
        try
        {
            if (timer % 5 != 0 || !isWorkingAllowed || itemsLeftToTransferLastSecond <= 0) {
                return;
            }
            final TileEntity tileEntity = coverHolder.getWorld().getTileEntity(coverHolder.getPos().offset(attachedSide));
            if (tileEntity == null) {
                return;
            }
            final IItemHandler itemHandler = tileEntity == null ? null : tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, attachedSide.getOpposite());
            if (itemHandler == null) {
                return;
            }
            TileEntity workableTileEntity = tileEntity;
            // For a multiblock, lets try to use its controller
            if (tileEntity instanceof MetaTileEntityHolder) {
                final MetaTileEntity metaTileEntity = ((MetaTileEntityHolder) tileEntity).getMetaTileEntity();
                if (metaTileEntity != null && metaTileEntity instanceof MetaTileEntityMultiblockPart)
                {
                    final MultiblockControllerBase controller = ((MetaTileEntityMultiblockPart) metaTileEntity).getController();
                    if (controller != null && controller instanceof RecipeMapMultiblockController && controller.isStructureFormed())
                        workableTileEntity = (TileEntity) controller.getHolder();
                }
            }
            final IWorkable workable = workableTileEntity == null ? null : workableTileEntity.getCapability(GregtechTileCapabilities.CAPABILITY_WORKABLE, attachedSide.getOpposite());
            if (workable == null) {
                return;
            }
            if (workable instanceof AbstractRecipeLogic  == false) {
                return;
            }
            final AbstractRecipeLogic recipeLogic = (AbstractRecipeLogic) workable;
            final IStorageNetwork myStorageNetwork = coverHolder.getCapability(GTCEInventoryCapabilities.CAPABILITY_STORAGE_NETWORK, attachedSide);
            if (myStorageNetwork == null) {
                return;
            }

            // First cleanup the outputs
            this.itemsLeftToTransferLastSecond -= cleanUpOutputs(itemHandler, myStorageNetwork, itemsLeftToTransferLastSecond);
            // Have we finished for this cycle?
            if (this.itemsLeftToTransferLastSecond <= 0)
                return;

            // TODO: Need to keep track of ongoing requests when doing keep in stock
            // For now, don't do keep in stock when the machine is busy
            if (workable.isActive())
                return;

            // Or there is something in the inventory
            // Not including known nonconsumed items
            for (int slot=0; slot < itemHandler.getSlots(); ++slot) {
                final ItemStack stack = itemHandler.getStackInSlot(slot);
                if (stack.isEmpty() || isIgnoredStack(stack)) {
                    continue;
                }
                return;
            }

            // Now go through the keep in stock
            final ItemStackHandler slots = ((SimpleItemFilter) itemFilter.getItemFilter()).getItemFilterSlots();
            for (int i = 0; i < slots.getSlots(); ++i) {
                final ItemStack requested = slots.getStackInSlot(i);
                if (requested == null || requested.isEmpty()) {
                    continue;
                }
                // Do we have enough in stock?
                int inStock = 0;
                IItemInfo itemInfo = myStorageNetwork.getItemInfo(new ItemStackKey(requested));
                if (itemInfo != null)
                    inStock = itemInfo.getTotalItemAmount();
                if (inStock >= requested.getCount()) {
                    continue;
                }
                // Not enough in stock, find a recipe
                final RecipeMap<?> recipeMap = recipeLogic.recipeMap;
                final List<Recipe> recipeList = RecipeMapCache.getRecipeMapCache(recipeMap).getRecipes(requested);
                for (Recipe recipe : recipeList) {
                    if (tryRecipe(requested, recipe, myStorageNetwork, itemHandler, itemsLeftToTransferLastSecond)) {
                        // Only do one keep in stock per cycle
                        return;
                    }
                }
            }
        }
        finally {
            if (timer % 20 == 0) {
                this.itemsLeftToTransferLastSecond = transferRate;
            }
        }
    }

    protected boolean tryRecipe(final ItemStack requested, final Recipe recipe, final IStorageNetwork sourceInventory, final IItemHandler targetInventory, int maxTransferAmount) {
        // Wrong tier
        if (recipe.getEUt() > this.EUt)
            return false;

        final List<CountableIngredient> ingredients = recipe.getInputs();

        // First simulate the movement to make sure we can do it
        int transfered = tryIngredients(ingredients, sourceInventory, targetInventory, maxTransferAmount, true);
        // Seems to work so do it for real
        if (transfered > 0) {
            // TODO don't recalculate again, use the ingredients we found when simulating
            itemsLeftToTransferLastSecond -= tryIngredients(ingredients, sourceInventory, targetInventory, maxTransferAmount, false);
            return true;
        }
        return false;
    }

    protected int tryIngredients(final List<CountableIngredient> ingredients, final IStorageNetwork sourceInventory, final IItemHandler targetInventory, final int maxTransferAmount, boolean simulate) {
        int itemsLeftToTransfer = maxTransferAmount;

        for (CountableIngredient ingredient : ingredients) {
            final int amount = ingredient.getCount();
            // Ingredient is not consumed in recipe
            if (amount == 0) {
                continue;
            }
            // Can't do it this cycle
            if (amount > itemsLeftToTransfer) {
                return 0;
            }
            final int transfered = tryMatchingStacks(ingredient.getIngredient().getMatchingStacks(), amount, sourceInventory, targetInventory, simulate);
            // Didn't work
            if (transfered <= 0) {
                return 0;
            }

            itemsLeftToTransfer -= transfered;
        }
        // All the ingredients worked, if there were any
        return maxTransferAmount - itemsLeftToTransfer;
    }

    protected int tryMatchingStacks(final ItemStack[] matchingStacks, final int amount, final IStorageNetwork sourceInventory, final IItemHandler targetInventory, boolean simulate) {
        for (ItemStack itemStack : matchingStacks) {
            final ItemStackKey key = new ItemStackKey(itemStack);
            final int extracted = sourceInventory.extractItem(key, amount, simulate);
            // Not enough of this ingredient
            if (extracted != amount) {
                continue;
            }
            final ItemStack sourceStack = key.getItemStack();
            sourceStack.setCount(extracted);
            final ItemStack remainder = ItemHandlerHelper.insertItemStacked(targetInventory, sourceStack, simulate);
            // Not accepting this ingredient
            if (remainder.getCount() != 0) {
                continue;
            }

            // This ingredient has enough and will go in the machine
            return extracted;
        }
        // No matching stack worked for this ingredient
        return 0;
    }

    protected int cleanUpOutputs(IItemHandler sourceInventory, IStorageNetwork targetInventory, int maxTransferAmount) {
        int itemsLeftToTransfer = maxTransferAmount;
        for (int srcIndex = 0; srcIndex < sourceInventory.getSlots(); srcIndex++) {
            ItemStack sourceStack = sourceInventory.extractItem(srcIndex, itemsLeftToTransfer, true);
            if (sourceStack.isEmpty()) {
                continue;
            }
            final ItemStackKey sourceStackKey = new ItemStackKey(sourceStack);
            final int amountToInsert = targetInventory.insertItem(sourceStackKey, sourceStack.getCount(), true);

            if (amountToInsert > 0) {
                sourceStack = sourceInventory.extractItem(srcIndex, amountToInsert, false);
                if (!sourceStack.isEmpty()) {
                    targetInventory.insertItem(sourceStackKey, sourceStack.getCount(), false);
                    itemsLeftToTransfer -= sourceStack.getCount();
                    if (itemsLeftToTransfer == 0) {
                        break;
                    }
                }
            }
        }
        return maxTransferAmount - itemsLeftToTransfer;
    }

    // TODO Handle this based on the recipes found
    static List<ItemAndMetadata> IGNORED_STACKS = new ArrayList<>();
    
    {
        IGNORED_STACKS.add(new ItemAndMetadata(MetaItems.INTEGRATED_CIRCUIT.getStackForm()));
        for (MetaItem<?>.MetaValueItem mold : MetaItems.SHAPE_MOLDS) {
            IGNORED_STACKS.add(new ItemAndMetadata(mold.getStackForm()));
        }
        for (MetaItem<?>.MetaValueItem shape : MetaItems.SHAPE_EXTRUDERS) {
            IGNORED_STACKS.add(new ItemAndMetadata(shape.getStackForm()));
        }
    }
    
    static boolean isIgnoredStack(final ItemStack stack) {
        return IGNORED_STACKS.contains(new ItemAndMetadata(stack));
    }

    @Override
    public boolean canAttach() {
        return coverHolder.getCapability(GTCEInventoryCapabilities.CAPABILITY_STORAGE_NETWORK, attachedSide) != null;
    }
    
    @Override
    public boolean shouldCoverInteractWithOutputside() {
        return true;
    }

    @Override
    public void renderCover(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, Cuboid6 plateBox, BlockRenderLayer layer) {
        Textures.CONVEYOR_OVERLAY.renderSided(attachedSide, plateBox, renderState, pipeline, translation);
    }

    @Override
    public EnumActionResult onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, CuboidRayTraceResult hitResult) {
        if (!coverHolder.getWorld().isRemote) {
            openUI((EntityPlayerMP) playerIn);
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, T defaultValue) {
        if(capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        }
        return defaultValue;
    }

    protected String getUITitle() {
        return "cover.conveyor.title";
    }

    protected ModularUI buildUI(ModularUI.Builder builder, EntityPlayer player) {
        return builder.build(this, player);
    }

    @Override
    public ModularUI createUI(EntityPlayer player) {
        final WidgetGroup primaryGroup = new WidgetGroup();
        primaryGroup.addWidget(new LabelWidget(10, 5, getUITitle(), GTValues.VN[tier]));
        primaryGroup.addWidget(new ClickButtonWidget(10, 20, 20, 20, "-10", data -> adjustTransferRate(data.isShiftClick ? -100 : -10)));
        primaryGroup.addWidget(new ClickButtonWidget(146, 20, 20, 20, "+10", data -> adjustTransferRate(data.isShiftClick ? +100 : +10)));
        primaryGroup.addWidget(new ClickButtonWidget(30, 20, 20, 20, "-1", data -> adjustTransferRate(data.isShiftClick ? -5 : -1)));
        primaryGroup.addWidget(new ClickButtonWidget(126, 20, 20, 20, "+1", data -> adjustTransferRate(data.isShiftClick ? +5 : +1)));
        primaryGroup.addWidget(new ImageWidget(50, 20, 76, 20, GuiTextures.DISPLAY));
        primaryGroup.addWidget(new SimpleTextWidget(88, 30, "cover.conveyor.transfer_rate", 0xFFFFFF, () -> Integer.toString(transferRate)));

        this.itemFilter.initUI(70, primaryGroup::addWidget);

        final ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 176, 190 + 82)
            .widget(primaryGroup)
            .bindPlayerInventory(player.inventory, GuiTextures.SLOT, 8, 190);
        return buildUI(builder, player);
    }

    @Override
    public boolean isWorkingEnabled() {
        return isWorkingAllowed;
    }

    @Override
    public void setWorkingEnabled(boolean isActivationAllowed) {
        this.isWorkingAllowed = isActivationAllowed;
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("TransferRate", transferRate);
        tagCompound.setBoolean("WorkingAllowed", isWorkingAllowed);
        final NBTTagCompound filterNBT = new NBTTagCompound();
        this.itemFilter.getItemFilter().writeToNBT(filterNBT);
        tagCompound.setTag("Filter", filterNBT);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.transferRate = tagCompound.getInteger("TransferRate");
        final NBTTagCompound filterNBT = tagCompound.getCompoundTag("Filter");
        this.itemFilter.getItemFilter().readFromNBT(filterNBT);
    }
}
