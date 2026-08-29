package codechicken.nei.recipe;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapedRecipes;

import codechicken.nei.NEIClientUtils;
import codechicken.nei.PositionedStack;

/**
 * Present so addons that extend the vanilla NEI crafting handler can load.
 * Vanilla crafting itself is discovered by JEI, not this class.
 */
public class ShapedRecipeHandler extends TemplateRecipeHandler {

	public class CachedShapedRecipe extends CachedRecipe {
		public ArrayList<PositionedStack> ingredients;
		public PositionedStack result;

		public CachedShapedRecipe(int width, int height, Object[] items, ItemStack out) {
			result = new PositionedStack(out, 119, 24);
			ingredients = new ArrayList<PositionedStack>();
			setIngredients(width, height, items);
		}

		public CachedShapedRecipe(ShapedRecipes recipe) {
			this(recipe.recipeWidth, recipe.recipeHeight, recipe.recipeItems, recipe.getRecipeOutput());
		}

		public void setIngredients(int width, int height, Object[] items) {
			if (items == null) {
				return;
			}
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					int index = y * width + x;
					if (index >= items.length || items[index] == null) {
						continue;
					}
					PositionedStack stack = new PositionedStack(items[index], 25 + x * 18, 6 + y * 18, false);
					stack.setMaxSize(1);
					ingredients.add(stack);
				}
			}
		}

		@Override
		public List<PositionedStack> getIngredients() {
			return ingredients;
		}

		@Override
		public PositionedStack getResult() {
			return result;
		}

		public void computeVisuals() {
			for (int i = 0; i < ingredients.size(); i++) {
				ingredients.get(i).generatePermutations();
			}
		}
	}

	@Override
	public String getRecipeName() {
		return NEIClientUtils.translate("recipe.shaped");
	}

	@Override
	public String getGuiTexture() {
		return "textures/gui/container/crafting_table.png";
	}

	@Override
	public String getOverlayIdentifier() {
		return "crafting";
	}

	@Override
	public Class<? extends net.minecraft.client.gui.inventory.GuiContainer> getGuiClass() {
		return GuiCrafting.class;
	}
}
