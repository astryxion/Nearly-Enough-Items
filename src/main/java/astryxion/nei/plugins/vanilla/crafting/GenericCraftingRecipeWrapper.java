package astryxion.nei.plugins.vanilla.crafting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

import astryxion.nei.api.recipe.wrapper.ICraftingRecipeWrapper;
import astryxion.nei.api.recipe.wrapper.IShapedCraftingRecipeWrapper;
import astryxion.nei.discovery.GenericCraftingRecipe;
import astryxion.nei.plugins.vanilla.VanillaRecipeWrapper;

public class GenericCraftingRecipeWrapper extends VanillaRecipeWrapper implements IShapedCraftingRecipeWrapper, ICraftingRecipeWrapper {
	@Nonnull
	private final GenericCraftingRecipe recipe;

	public GenericCraftingRecipeWrapper(@Nonnull GenericCraftingRecipe recipe) {
		this.recipe = recipe;
		List<Object> inputs = recipe.getInputs();
		for (int i = 0; i < inputs.size(); i++) {
			Object input = inputs.get(i);
			if (input instanceof ItemStack) {
				ItemStack stack = (ItemStack) input;
				if (stack.stackSize != 1) {
					stack.stackSize = 1;
				}
			}
		}
	}

	@Nonnull
	@Override
	public List getInputs() {
		return new ArrayList<Object>(recipe.getInputs());
	}

	@Nonnull
	@Override
	public List<ItemStack> getOutputs() {
		return Collections.singletonList(recipe.getOutput());
	}

	@Override
	public int getWidth() {
		return recipe.isShaped() ? recipe.getWidth() : 0;
	}

	@Override
	public int getHeight() {
		return recipe.isShaped() ? recipe.getHeight() : 0;
	}
}
