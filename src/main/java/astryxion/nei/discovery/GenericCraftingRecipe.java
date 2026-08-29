package astryxion.nei.discovery;

import java.util.Collections;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;

/**
 * Normalized crafting recipe produced from an unknown {@link IRecipe}
 * implementation. Fed into the existing JEI crafting category.
 */
public class GenericCraftingRecipe {
	private final IRecipe original;
	private final List<Object> inputs;
	private final ItemStack output;
	private final int width;
	private final int height;

	public GenericCraftingRecipe(IRecipe original, List<Object> inputs, ItemStack output, int width, int height) {
		this.original = original;
		this.inputs = inputs;
		this.output = output;
		this.width = width;
		this.height = height;
	}

	public IRecipe getOriginal() {
		return original;
	}

	public List<Object> getInputs() {
		return Collections.unmodifiableList(inputs);
	}

	public ItemStack getOutput() {
		return output;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public boolean isShaped() {
		return width > 0 && height > 0;
	}
}
