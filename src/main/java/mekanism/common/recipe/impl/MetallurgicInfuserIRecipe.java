package mekanism.common.recipe.impl;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.MetallurgicInfuserRecipe;
import mekanism.api.recipes.basic.BasicMetallurgicInfuserRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.InfusionStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

@NothingNullByDefault
public class MetallurgicInfuserIRecipe extends BasicMetallurgicInfuserRecipe implements ItemStackOutputInternal {

    public MetallurgicInfuserIRecipe(ItemStackIngredient itemInput, InfusionStackIngredient infusionInput, ItemStack output) {
        super(itemInput, infusionInput, output);
    }

    @Override
    public RecipeType<MetallurgicInfuserRecipe> getType() {
        return MekanismRecipeType.METALLURGIC_INFUSING.get();
    }

    @Override
    public RecipeSerializer<MetallurgicInfuserIRecipe> getSerializer() {
        return MekanismRecipeSerializers.METALLURGIC_INFUSING.get();
    }

    @Override
    public String getGroup() {
        return MekanismBlocks.METALLURGIC_INFUSER.getName();
    }

    @Override
    public ItemStack getToastSymbol() {
        return MekanismBlocks.METALLURGIC_INFUSER.getItemStack();
    }

    @Override
    public ItemStack getOutputRaw() {
        return output;
    }
}