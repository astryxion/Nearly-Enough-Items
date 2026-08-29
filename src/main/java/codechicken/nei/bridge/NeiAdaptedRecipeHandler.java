package codechicken.nei.bridge;

import javax.annotation.Nonnull;

import astryxion.nei.api.recipe.IRecipeHandler;
import astryxion.nei.api.recipe.IRecipeWrapper;

public class NeiAdaptedRecipeHandler implements IRecipeHandler<NeiAdaptedRecipe> {
	@Override
	@Nonnull
	public Class<NeiAdaptedRecipe> getRecipeClass() {
		return NeiAdaptedRecipe.class;
	}

	@Nonnull
	@Override
	public String getRecipeCategoryUid() {
		return "nei.adapted";
	}

	@Override
	@Nonnull
	public IRecipeWrapper getRecipeWrapper(@Nonnull NeiAdaptedRecipe recipe) {
		return recipe;
	}

	@Override
	public boolean isRecipeValid(@Nonnull NeiAdaptedRecipe recipe) {
		return recipe.getHandler() != null;
	}
}
