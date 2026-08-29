package codechicken.nei.bridge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

import astryxion.nei.gui.RecipeLayout;
import astryxion.nei.gui.ingredients.GuiIngredient;
import codechicken.nei.PositionedStack;
import codechicken.nei.api.IOverlayHandler;
import codechicken.nei.api.IRecipeOverlayRenderer;
import codechicken.nei.recipe.GuiRecipe;
import codechicken.nei.recipe.IRecipeHandler;

/**
 * Thin IRecipeHandler that exposes JEI crafting-grid inputs as NEI
 * PositionedStacks so DefaultOverlayHandler can fill custom tables.
 */
public class OverlayRecipeAdapter implements IRecipeHandler {
	private static final int NEI_CRAFT_OFFSET_X = 25;
	private static final int NEI_CRAFT_OFFSET_Y = 6;

	private final List<PositionedStack> ingredients;

	public OverlayRecipeAdapter(List<PositionedStack> ingredients) {
		this.ingredients = ingredients;
	}

	public static OverlayRecipeAdapter fromCraftingLayout(RecipeLayout layout) {
		List<PositionedStack> stacks = new ArrayList<PositionedStack>();
		if (layout == null) {
			return new OverlayRecipeAdapter(stacks);
		}
		Map<Integer, GuiIngredient<ItemStack>> slots = layout.getItemStacks().getGuiIngredients();
		for (Map.Entry<Integer, GuiIngredient<ItemStack>> entry : slots.entrySet()) {
			GuiIngredient<ItemStack> ingredient = entry.getValue();
			if (ingredient == null || !ingredient.isInput()) {
				continue;
			}
			List<ItemStack> items = ingredient.getAll();
			if (items == null || items.isEmpty()) {
				continue;
			}
			int grid = entry.getKey() - 1;
			if (grid < 0 || grid > 8) {
				continue;
			}
			int x = grid % 3;
			int y = grid / 3;
			stacks.add(new PositionedStack(items, NEI_CRAFT_OFFSET_X + x * 18, NEI_CRAFT_OFFSET_Y + y * 18));
		}
		return new OverlayRecipeAdapter(stacks);
	}

	@Override
	public String getRecipeName() {
		return "";
	}

	@Override
	public int numRecipes() {
		return 1;
	}

	@Override
	public void drawBackground(int recipe) {
	}

	@Override
	public void drawForeground(int recipe) {
	}

	@Override
	public List<PositionedStack> getIngredientStacks(int recipe) {
		return ingredients;
	}

	@Override
	public List<PositionedStack> getOtherStacks(int recipe) {
		return Collections.emptyList();
	}

	@Override
	public PositionedStack getResultStack(int recipe) {
		return null;
	}

	@Override
	public void onUpdate() {
	}

	@Override
	public boolean hasOverlay(GuiContainer gui, Container container, int recipe) {
		return true;
	}

	@Override
	public IRecipeOverlayRenderer getOverlayRenderer(GuiContainer gui, int recipe) {
		return null;
	}

	@Override
	public IOverlayHandler getOverlayHandler(GuiContainer gui, int recipe) {
		return null;
	}

	@Override
	public int recipiesPerPage() {
		return 1;
	}

	@Override
	public List<String> handleTooltip(GuiRecipe gui, List<String> currenttip, int recipe) {
		return currenttip;
	}

	@Override
	public List<String> handleItemTooltip(GuiRecipe gui, ItemStack stack, List<String> currenttip, int recipe) {
		return currenttip;
	}

	@Override
	public boolean keyTyped(GuiRecipe gui, char keyChar, int keyCode, int recipe) {
		return false;
	}

	@Override
	public boolean mouseClicked(GuiRecipe gui, int button, int recipe) {
		return false;
	}
}
