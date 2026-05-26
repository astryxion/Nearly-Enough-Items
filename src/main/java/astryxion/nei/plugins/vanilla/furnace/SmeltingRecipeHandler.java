package astryxion.nei.plugins.vanilla.furnace;

import javax.annotation.Nonnull;

import astryxion.nei.api.recipe.IRecipeHandler;
import astryxion.nei.api.recipe.IRecipeWrapper;
import astryxion.nei.api.recipe.VanillaRecipeCategoryUid;

public class SmeltingRecipeHandler implements IRecipeHandler<SmeltingRecipe> {

	@Override
	@Nonnull
	public Class<SmeltingRecipe> getRecipeClass() {
		return SmeltingRecipe.class;
	}

	@Nonnull
	@Override
	public String getRecipeCategoryUid() {
		return VanillaRecipeCategoryUid.SMELTING;
	}

	@Override
	@Nonnull
	public IRecipeWrapper getRecipeWrapper(@Nonnull SmeltingRecipe recipe) {
		return recipe;
	}

	@Override
	public boolean isRecipeValid(@Nonnull SmeltingRecipe recipe) {
		return recipe.getInputs().size() != 0 && recipe.getOutputs().size() > 0;
	}

}
