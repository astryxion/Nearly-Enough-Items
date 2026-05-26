package astryxion.nei.plugins.vanilla.crafting;

import javax.annotation.Nonnull;
import java.util.List;

import net.minecraftforge.oredict.ShapedOreRecipe;

import astryxion.nei.api.recipe.IRecipeHandler;
import astryxion.nei.api.recipe.IRecipeWrapper;
import astryxion.nei.api.recipe.VanillaRecipeCategoryUid;

public class ShapedOreRecipeHandler implements IRecipeHandler<ShapedOreRecipe> {

	@Override
	@Nonnull
	public Class<ShapedOreRecipe> getRecipeClass() {
		return ShapedOreRecipe.class;
	}

	@Nonnull
	@Override
	public String getRecipeCategoryUid() {
		return VanillaRecipeCategoryUid.CRAFTING;
	}

	@Override
	@Nonnull
	public IRecipeWrapper getRecipeWrapper(@Nonnull ShapedOreRecipe recipe) {
		return new ShapedOreRecipeWrapper(recipe);
	}

	@Override
	public boolean isRecipeValid(@Nonnull ShapedOreRecipe recipe) {
		if (recipe.getRecipeOutput() == null) {
			return false;
		}
		int inputCount = 0;
		for (Object input : recipe.getInput()) {
			if (input instanceof List) {
				if (((List) input).size() == 0) {
					return false;
				}
			}
			if (input != null) {
				inputCount++;
			}
		}
		return inputCount > 0;
	}
}
