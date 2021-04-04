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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.stack.ItemAndMetadata;
import gregtech.api.util.GTUtility;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;

public class RecipeMapCache {

    private static final Map<String, RecipeMapCache> CACHE = Maps.newConcurrentMap();

    private final RecipeMap<?> recipeMap;

    private final Map<ItemAndMetadata, List<Recipe>> recipesByItem = Maps.newHashMap(); 

    public static RecipeMapCache getRecipeMapCache(final RecipeMap<?> recipeMap) {
        RecipeMapCache result = CACHE.get(recipeMap.unlocalizedName);
        if (result != null) {
            return result;
        }

        result = new RecipeMapCache(recipeMap);
        CACHE.put(recipeMap.unlocalizedName, result);
        return result;
    }

    private RecipeMapCache(final RecipeMap<?> recipeMap) {
        this.recipeMap = recipeMap;
        // Special Cases
        if (recipeMap == RecipeMaps.FURNACE_RECIPES) {
            generateFurnace();
        }
        generate();
    }

    public List<Recipe> getRecipes(final ItemStack itemStack) {
        final ItemAndMetadata itemAndMetadata = new ItemAndMetadata(itemStack);
        return this.recipesByItem.getOrDefault(itemAndMetadata, Collections.emptyList());
    }

    private void addRecipe(final ItemAndMetadata itemAndMetadata, final Recipe recipe) {
        final List<Recipe> recipes = this.recipesByItem.computeIfAbsent(itemAndMetadata, key -> Lists.newArrayList());
        recipes.add(recipe);
    }

    private void generate() {
        this.recipeMap.getRecipeList().forEach(recipe -> recipe.getOutputs().forEach(itemStack -> {
            final ItemAndMetadata itemAndMetadata = new ItemAndMetadata(itemStack);
            addRecipe(itemAndMetadata, recipe);
        }));
    }

    private void generateFurnace() {
        FurnaceRecipes.instance().getSmeltingList().forEach((input, output) -> {
            if (input != null && output != null) {
                final Recipe recipe = RecipeMaps.FURNACE_RECIPES.recipeBuilder()
                        .inputs(GTUtility.copyAmount(1, input))
                        .outputs(output)
                        .duration(128)
                        .EUt(4)
                        .build()
                        .getResult();
                final ItemAndMetadata itemAndMetadata = new ItemAndMetadata(output);
                addRecipe(itemAndMetadata, recipe);
            }
        });
    }
}
