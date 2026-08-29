package codechicken.nei.recipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.item.ItemStack;

import codechicken.nei.NEIClientConfig;
import codechicken.nei.NEIClientUtils;
import codechicken.nei.PositionedStack;

public class ShapelessRecipeHandler extends ShapedRecipeHandler {
	public int[][] stackorder = new int[][] {
			{ 0, 0 }, { 1, 0 }, { 0, 1 }, { 1, 1 }, { 0, 2 }, { 1, 2 }, { 2, 0 }, { 2, 1 }, { 2, 2 }
	};

	public class CachedShapelessRecipe extends CachedRecipe {
		public ArrayList<PositionedStack> ingredients;
		public PositionedStack result;

		public CachedShapelessRecipe() {
			ingredients = new ArrayList<PositionedStack>();
		}

		public CachedShapelessRecipe(ItemStack output) {
			this();
			setResult(output);
		}

		public CachedShapelessRecipe(Object[] input, ItemStack output) {
			this(Arrays.asList(input), output);
		}

		public CachedShapelessRecipe(List<?> input, ItemStack output) {
			this(output);
			setIngredients(input);
		}

		public void setIngredients(List<?> items) {
			ingredients.clear();
			if (items == null) {
				return;
			}
			int itemsSize = items.size();
			if (itemsSize > stackorder.length) {
				NEIClientConfig.logger.error("RECIPE BUG: Too many items (" + itemsSize + ") for " + String.valueOf(this.result));
				itemsSize = stackorder.length;
			}
			for (int ingred = 0; ingred < itemsSize; ingred++) {
				PositionedStack stack = new PositionedStack(
						items.get(ingred),
						25 + stackorder[ingred][0] * 18,
						6 + stackorder[ingred][1] * 18);
				stack.setMaxSize(1);
				ingredients.add(stack);
			}
		}

		public void setResult(ItemStack output) {
			result = new PositionedStack(output, 119, 24);
		}

		@Override
		public List<PositionedStack> getIngredients() {
			return ingredients;
		}

		@Override
		public PositionedStack getResult() {
			return result;
		}
	}

	@Override
	public String getRecipeName() {
		return NEIClientUtils.translate("recipe.shapeless");
	}
}
