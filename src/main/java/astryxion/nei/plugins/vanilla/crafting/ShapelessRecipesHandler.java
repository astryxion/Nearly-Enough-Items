package astryxion.nei.plugins.vanilla.crafting;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapelessRecipes;

import astryxion.nei.api.recipe.IRecipeHandler;
import astryxion.nei.api.recipe.IRecipeWrapper;
import astryxion.nei.api.recipe.VanillaRecipeCategoryUid;

public class ShapelessRecipesHandler implements IRecipeHandler<ShapelessRecipes> {

	@Override
	@Nonnull
	public Class<ShapelessRecipes> getRecipeClass() {
		return ShapelessRecipes.class;
	}

	@Nonnull
	@Override
	public String getRecipeCategoryUid() {
		return VanillaRecipeCategoryUid.CRAFTING;
	}

	@Override
	@Nonnull
	public IRecipeWrapper getRecipeWrapper(@Nonnull ShapelessRecipes recipe) {
		return new ShapelessRecipesWrapper(recipe);
	}

	@Override
	public boolean isRecipeValid(@Nonnull ShapelessRecipes recipe) {
		if (recipe.getRecipeOutput() == null) {
			return false;
		}
		int inputCount = 0;
		for (Object input : recipe.recipeItems) {
			if (input instanceof ItemStack) {
				inputCount++;
			} else {
				return false;
			}
		}
		return inputCount > 0;
	}
}
