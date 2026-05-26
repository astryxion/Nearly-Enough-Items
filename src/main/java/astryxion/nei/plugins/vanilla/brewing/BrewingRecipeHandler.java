package astryxion.nei.plugins.vanilla.brewing;

import javax.annotation.Nonnull;

import astryxion.nei.api.recipe.IRecipeHandler;
import astryxion.nei.api.recipe.IRecipeWrapper;
import astryxion.nei.api.recipe.VanillaRecipeCategoryUid;

public class BrewingRecipeHandler implements IRecipeHandler<BrewingRecipeWrapper> {
	@Nonnull
	@Override
	public Class<BrewingRecipeWrapper> getRecipeClass() {
		return BrewingRecipeWrapper.class;
	}

	@Nonnull
	@Override
	public String getRecipeCategoryUid() {
		return VanillaRecipeCategoryUid.BREWING;
	}

	@Nonnull
	@Override
	public IRecipeWrapper getRecipeWrapper(@Nonnull BrewingRecipeWrapper recipe) {
		return recipe;
	}

	@Override
	public boolean isRecipeValid(@Nonnull BrewingRecipeWrapper recipe) {
		return recipe.getInputs().size() == 4 && recipe.getOutputs().size() == 1;
	}
}
