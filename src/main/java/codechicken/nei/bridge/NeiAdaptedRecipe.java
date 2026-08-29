package codechicken.nei.bridge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import astryxion.nei.api.recipe.BlankRecipeWrapper;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.IRecipeHandler;

/**
 * JEI recipe object produced from one NEI handler recipe slot.
 */
public class NeiAdaptedRecipe extends BlankRecipeWrapper {
	private final IRecipeHandler handler;
	private final int recipeIndex;
	private final String categoryUid;
	private final List<Object> inputs;
	private final List<ItemStack> outputs;

	public NeiAdaptedRecipe(IRecipeHandler handler, int recipeIndex, String categoryUid) {
		this.handler = handler;
		this.recipeIndex = recipeIndex;
		this.categoryUid = categoryUid;
		this.inputs = snapshotInputs(handler, recipeIndex);
		this.outputs = snapshotOutputs(handler, recipeIndex);
	}

	public IRecipeHandler getHandler() {
		return handler;
	}

	public int getRecipeIndex() {
		return recipeIndex;
	}

	public String getCategoryUid() {
		return categoryUid;
	}

	@Override
	public List getInputs() {
		return inputs;
	}

	@Override
	public List getOutputs() {
		return outputs;
	}

	@Override
	public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight) {
		try {
			handler.drawForeground(recipeIndex);
		} catch (Throwable ignored) {
		}
	}

	@Override
	public void drawAnimations(Minecraft minecraft, int recipeWidth, int recipeHeight) {
		try {
			handler.onUpdate();
		} catch (Throwable ignored) {
		}
	}

	public List<PositionedStack> getAllStacks() {
		List<PositionedStack> stacks = new ArrayList<PositionedStack>();
		addAll(stacks, handler.getIngredientStacks(recipeIndex));
		PositionedStack result = handler.getResultStack(recipeIndex);
		if (result != null) {
			stacks.add(result);
		}
		addAll(stacks, handler.getOtherStacks(recipeIndex));
		return stacks;
	}

	private static void addAll(List<PositionedStack> dest, List<PositionedStack> src) {
		if (src == null) {
			return;
		}
		for (int i = 0; i < src.size(); i++) {
			if (src.get(i) != null) {
				dest.add(src.get(i));
			}
		}
	}

	private static List<Object> snapshotInputs(IRecipeHandler handler, int recipeIndex) {
		List<Object> inputs = new ArrayList<Object>();
		try {
			addStacks(inputs, handler.getIngredientStacks(recipeIndex));
		} catch (Throwable t) {
			NeiCompatLog.warn("getIngredientStacks failed for {} recipe {}", handler.getClass().getName(), Integer.valueOf(recipeIndex), t);
		}
		try {
			addStacks(inputs, handler.getOtherStacks(recipeIndex));
		} catch (Throwable t) {
			NeiCompatLog.warn("getOtherStacks failed for {} recipe {}", handler.getClass().getName(), Integer.valueOf(recipeIndex), t);
		}
		return inputs;
	}

	private static List<ItemStack> snapshotOutputs(IRecipeHandler handler, int recipeIndex) {
		List<ItemStack> outputs = new ArrayList<ItemStack>();
		PositionedStack result = handler.getResultStack(recipeIndex);
		if (result != null) {
			outputs.addAll(result.getItemList());
		}
		return outputs.isEmpty() ? Collections.<ItemStack>emptyList() : outputs;
	}

	private static void addStacks(List<Object> dest, List<PositionedStack> stacks) {
		if (stacks == null) {
			return;
		}
		for (int i = 0; i < stacks.size(); i++) {
			PositionedStack stack = stacks.get(i);
			if (stack == null) {
				continue;
			}
			List<ItemStack> items = stack.getItemList();
			if (items.size() == 1) {
				dest.add(items.get(0));
			} else if (!items.isEmpty()) {
				dest.add(items);
			}
		}
	}
}
