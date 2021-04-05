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

import com.google.common.collect.Lists;

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
import gregtech.api.gui.widgets.AdvancedTextWidget;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.gui.widgets.PhantomSlotWidget;
import gregtech.api.gui.widgets.ScrollableListWidget;
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
import gregtech.api.util.Position;
import gregtech.api.util.Size;
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

/*
 * This is a cover for something that has the IStorageNetwork capability i.e. an inventory pipe
 * 
 *  It allows the specification of items to keep in stock which will be crafted using recipes from the adjacent machine.
 */
public class CoverKeepInStock extends CoverBehavior implements CoverWithUI, ITickable, IControllable {

    private final int KEEP_IN_STOCK_SIZE = 16;

    public final int tier;
    private final int slotLimit;
    private final long EUt;
    private final int maxItemTransferRate;
    protected int transferRate;
    protected int itemsLeftToTransferLastSecond;
    protected boolean isWorkingAllowed = true;
    protected final List<KeepInStockInfo> keepInStockInfos;

    public CoverKeepInStock(final ICoverable coverable, final EnumFacing attachedSide, final int tier, final int itemsPerSecond) {
        super(coverable, attachedSide);
        this.tier = tier;
        this.slotLimit = tier * 8;
        this.EUt = GTValues.V[tier];
        this.maxItemTransferRate = itemsPerSecond;
        this.transferRate = maxItemTransferRate;
        this.itemsLeftToTransferLastSecond = transferRate;
        this.keepInStockInfos = Lists.newArrayList();
        for (int i = 0; i < KEEP_IN_STOCK_SIZE; ++i) {
            keepInStockInfos.add(new KeepInStockInfo(slotLimit));
        }
    }

    protected void setTransferRate(int transferRate) {
        this.transferRate = transferRate;
        coverHolder.markDirty();
    }

    protected void adjustTransferRate(int amount) {
        setTransferRate(MathHelper.clamp(transferRate + amount, 1, maxItemTransferRate));
    }

    private ItemStack canProcess(final IStorageNetwork storageNetwork, final KeepInStockInfo keepInStockInfo) {
        final ItemStack requested = keepInStockInfo.getItemStack();
        if (requested == null || requested.isEmpty()) {
            keepInStockInfo.setStatus(KeepInStockInfo.NA);
            return null;
        }
        // Do we have enough in stock?
        int inStock = 0;
        IItemInfo itemInfo = storageNetwork.getItemInfo(new ItemStackKey(requested));
        if (itemInfo != null)
            inStock = itemInfo.getTotalItemAmount();
        if (inStock >= requested.getCount()) {
            keepInStockInfo.setStatus(KeepInStockInfo.IN_STOCK);
            return null;
        }
        return requested;
    }

    private void generalStatus(final IStorageNetwork storageNetwork, final String status) {
        for (KeepInStockInfo keepInStockInfo : keepInStockInfos) {
            keepInStockInfo.setStatus(status);
            // Don't override obvious status with general status
            canProcess(storageNetwork, keepInStockInfo);
        }
    }

    @Override
    public void update() {
        final long timer = this.coverHolder.getTimer();
        try
        {
            if (timer % 5 != 0 || !this.isWorkingAllowed || this.itemsLeftToTransferLastSecond <= 0) {
                return;
            }
            final IStorageNetwork myStorageNetwork = this.coverHolder.getCapability(GTCEInventoryCapabilities.CAPABILITY_STORAGE_NETWORK, this.attachedSide);
            if (myStorageNetwork == null) {
                // Shouldn't happen?
                return;
            }

            final TileEntity tileEntity = this.coverHolder.getWorld().getTileEntity(this.coverHolder.getPos().offset(this.attachedSide));
            if (tileEntity == null) {
                generalStatus(myStorageNetwork, KeepInStockInfo.NO_MACHINE);
                return;
            }
            final IItemHandler itemHandler = tileEntity == null ? null : tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, this.attachedSide.getOpposite());
            if (itemHandler == null) {
                generalStatus(myStorageNetwork, KeepInStockInfo.NO_MACHINE);
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
                generalStatus(myStorageNetwork, KeepInStockInfo.NO_MACHINE);
                return;
            }
            if (workable instanceof AbstractRecipeLogic  == false) {
                generalStatus(myStorageNetwork, KeepInStockInfo.NO_MACHINE);
                return;
            }
            final AbstractRecipeLogic recipeLogic = (AbstractRecipeLogic) workable;

            // First cleanup the outputs
            this.itemsLeftToTransferLastSecond -= cleanUpOutputs(itemHandler, myStorageNetwork, this.itemsLeftToTransferLastSecond);
            // Have we finished for this cycle?
            if (this.itemsLeftToTransferLastSecond <= 0) {
                generalStatus(myStorageNetwork, KeepInStockInfo.CLEAN_OUTPUTS);
                return;
            }

            // TODO: Need to keep track of ongoing requests when doing keep in stock
            // For now, don't do keep in stock when the machine is busy
            if (workable.isActive()) {
                generalStatus(myStorageNetwork, KeepInStockInfo.BUSY);
                return;
            }

            // Or there is something in the inventory
            // Not including known nonconsumed items
            for (int slot=0; slot < itemHandler.getSlots(); ++slot) {
                final ItemStack stack = itemHandler.getStackInSlot(slot);
                if (stack.isEmpty() || isIgnoredStack(stack)) {
                    continue;
                }
                generalStatus(myStorageNetwork, KeepInStockInfo.INVENTORY);
                return;
            }

            // Now go through the keep in stock
            boolean doneSomething = false;
            for (KeepInStockInfo keepInStockInfo : keepInStockInfos) {
                final ItemStack requested = canProcess(myStorageNetwork, keepInStockInfo);
                if (requested == null) {
                    continue;
                }
                // Already done something?
                if (doneSomething) {
                    keepInStockInfo.setStatus(KeepInStockInfo.BUSY);
                    continue;
                }
                // Not enough in stock, find a recipe
                final RecipeMap<?> recipeMap = recipeLogic.recipeMap;
                final List<Recipe> recipeList = RecipeMapCache.getRecipeMapCache(recipeMap).getRecipes(requested);
                keepInStockInfo.setStatus(KeepInStockInfo.NO_RECIPE);
                for (Recipe recipe : recipeList) {
                    if (tryRecipe(requested, recipe, myStorageNetwork, keepInStockInfo, itemHandler, itemsLeftToTransferLastSecond)) {
                        doneSomething = true;
                        break;
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

    protected boolean tryRecipe(final ItemStack requested, final Recipe recipe, final IStorageNetwork sourceInventory, final KeepInStockInfo keepInStockInfo, final IItemHandler targetInventory, int maxTransferAmount) {
        // Wrong tier
        if (recipe.getEUt() > this.EUt) {
            keepInStockInfo.setStatus(KeepInStockInfo.WRONG_TIER);
            return false;
        }

        final List<CountableIngredient> ingredients = recipe.getInputs();

        // First simulate the movement to make sure we can do it
        int transfered = tryIngredients(ingredients, keepInStockInfo, sourceInventory, targetInventory, maxTransferAmount, true);
        // Seems to work so do it for real
        if (transfered > 0) {
            // TODO don't recalculate again, use the ingredients we found when simulating
            keepInStockInfo.setStatus(KeepInStockInfo.PROCESSING);
            itemsLeftToTransferLastSecond -= tryIngredients(ingredients, keepInStockInfo, sourceInventory, targetInventory, maxTransferAmount, false);
            return true;
        }
        return false;
    }

    protected int tryIngredients(final List<CountableIngredient> ingredients, final KeepInStockInfo keepInStockInfo, final IStorageNetwork sourceInventory, final IItemHandler targetInventory, final int maxTransferAmount, boolean simulate) {
        int itemsLeftToTransfer = maxTransferAmount;

        for (CountableIngredient ingredient : ingredients) {
            final int amount = ingredient.getCount();
            // Ingredient is not consumed in recipe
            if (amount == 0) {
                continue;
            }
            // Can't do it this cycle
            if (amount > itemsLeftToTransfer) {
                keepInStockInfo.setStatus(KeepInStockInfo.TOO_BIG);
                return 0;
            }
            final int transfered = tryMatchingStacks(ingredient.getIngredient().getMatchingStacks(), amount, keepInStockInfo, sourceInventory, targetInventory, simulate);
            // Didn't work
            if (transfered <= 0) {
                return 0;
            }

            itemsLeftToTransfer -= transfered;
        }
        // All the ingredients worked, if there were any
        return maxTransferAmount - itemsLeftToTransfer;
    }

    protected int tryMatchingStacks(final ItemStack[] matchingStacks, final int amount, final KeepInStockInfo keepInStockInfo, final IStorageNetwork sourceInventory, final IItemHandler targetInventory, boolean simulate) {
        for (ItemStack itemStack : matchingStacks) {
            final ItemStackKey key = new ItemStackKey(itemStack);
            final int extracted = sourceInventory.extractItem(key, amount, simulate);
            // Not enough of this ingredient
            if (extracted != amount) {
                if (simulate) {
                    keepInStockInfo.setStatus(KeepInStockInfo.NO_INGREDIENTS);
                }
                continue;
            }
            final ItemStack sourceStack = key.getItemStack();
            sourceStack.setCount(extracted);
            final ItemStack remainder = ItemHandlerHelper.insertItemStacked(targetInventory, sourceStack, simulate);
            // Not accepting this ingredient
            if (remainder.getCount() != 0) {
                keepInStockInfo.setStatus(KeepInStockInfo.NO_ACCEPT);
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
        return "cover.stock.title";
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
        primaryGroup.addWidget(new SimpleTextWidget(88, 30, "cover.conveyor.transfer_rate", 0xFFFFFF, () -> Integer.toString(this.transferRate)));

        final ScrollableListWidget scrollPanel = new ScrollableListWidget(5, 50, 160, 120);
        for (int i = 0; i < this.keepInStockInfos.size(); ++i) {
            KeepInStockInfo keepInStockInfo = this.keepInStockInfos.get(i);
            final WidgetGroup widgetGroup = new WidgetGroup(new Position(0, 0), new Size(140, 20));
            widgetGroup.addWidget(new PhantomSlotWidget(keepInStockInfo, 0, 0, 0).setBackgroundTexture(GuiTextures.SLOT));
            widgetGroup.addWidget(new ImageWidget(25, 0, 120, 20, GuiTextures.DISPLAY));
            widgetGroup.addWidget(new AdvancedTextWidget(30, 5, keepInStockInfo::displayStatus, 0xFFFFFF));
            scrollPanel.addWidget(widgetGroup);
        }
        primaryGroup.addWidget(scrollPanel);

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
        tagCompound.setInteger("TransferRate", this.transferRate);
        tagCompound.setBoolean("WorkingAllowed", this.isWorkingAllowed);

        final NBTTagCompound keepInStocks = new NBTTagCompound();
        for (int i = 0; i < this.keepInStockInfos.size(); ++i) {
            final KeepInStockInfo keepInStockInfo = this.keepInStockInfos.get(i);
            final NBTTagCompound keepInStock = keepInStockInfo.serializeNBT();
            if (!keepInStockInfo.getItemStack().isEmpty()) {
                keepInStocks.setTag(Integer.toString(i), keepInStock);
            }
        }
        tagCompound.setTag("KeepInStock", keepInStocks);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.transferRate = tagCompound.getInteger("TransferRate");
        this.isWorkingAllowed = tagCompound.getBoolean("WorkingAllowed");
        final NBTTagCompound KeepInStocks = tagCompound.getCompoundTag("KeepInStock");
        for (int i = 0; i < this.keepInStockInfos.size(); ++i) {
            final NBTTagCompound keepInStock = KeepInStocks.getCompoundTag(Integer.toString(i));
            if (keepInStock != null) {
                this.keepInStockInfos.get(i).deserializeNBT(keepInStock);
            } else {
                this.keepInStockInfos.set(i, new KeepInStockInfo(slotLimit));
            }
        }
    }
}
