package astryxion.nei.plugins.vanilla.crafting;

import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

import astryxion.nei.api.recipe.IRecipeHandler;
import astryxion.nei.api.recipe.IRecipeWrapper;
import astryxion.nei.api.recipe.VanillaRecipeCategoryUid;
import astryxion.nei.discovery.GenericCraftingRecipe;

public class GenericCraftingRecipeHandler implements IRecipeHandler<GenericCraftingRecipe> {
	@Override
	@Nonnull
	public Class<GenericCraftingRecipe> getRecipeClass() {
		return GenericCraftingRecipe.class;
	}

	@Nonnull
	@Override
	public String getRecipeCategoryUid() {
		return VanillaRecipeCategoryUid.CRAFTING;
	}

	@Override
	@Nonnull
	public IRecipeWrapper getRecipeWrapper(@Nonnull GenericCraftingRecipe recipe) {
		return new GenericCraftingRecipeWrapper(recipe);
	}

	@Override
	public boolean isRecipeValid(@Nonnull GenericCraftingRecipe recipe) {
		if (recipe.getOutput() == null || recipe.getOutput().getItem() == null) {
			return false;
		}
		List<Object> inputs = recipe.getInputs();
		int inputCount = 0;
		for (int i = 0; i < inputs.size(); i++) {
			Object input = inputs.get(i);
			if (input instanceof List) {
				if (((List<?>) input).isEmpty()) {
					return false;
				}
			}
			if (input instanceof ItemStack && ((ItemStack) input).getItem() == null) {
				continue;
			}
			if (input != null) {
				inputCount++;
			}
		}
		return inputCount > 0;
	}
}
