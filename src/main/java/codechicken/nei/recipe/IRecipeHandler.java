package codechicken.nei.recipe;

import java.util.List;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

import codechicken.nei.PositionedStack;
import codechicken.nei.api.IOverlayHandler;
import codechicken.nei.api.IRecipeOverlayRenderer;

/**
 * Do not implement this. Implement {@link ICraftingHandler} or {@link IUsageHandler}.
 */
public interface IRecipeHandler {
	String getRecipeName();

	int numRecipes();

	void drawBackground(int recipe);

	void drawForeground(int recipe);

	List<PositionedStack> getIngredientStacks(int recipe);

	List<PositionedStack> getOtherStacks(int recipe);

	PositionedStack getResultStack(int recipe);

	void onUpdate();

	boolean hasOverlay(GuiContainer gui, Container container, int recipe);

	IRecipeOverlayRenderer getOverlayRenderer(GuiContainer gui, int recipe);

	IOverlayHandler getOverlayHandler(GuiContainer gui, int recipe);

	int recipiesPerPage();

	List<String> handleTooltip(GuiRecipe gui, List<String> currenttip, int recipe);

	List<String> handleItemTooltip(GuiRecipe gui, ItemStack stack, List<String> currenttip, int recipe);

	boolean keyTyped(GuiRecipe gui, char keyChar, int keyCode, int recipe);

	boolean mouseClicked(GuiRecipe gui, int button, int recipe);
}
