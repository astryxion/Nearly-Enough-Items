package astryxion.nei.plugins.vanilla.crafting;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import astryxion.nei.api.IGuiHelper;
import astryxion.nei.api.gui.ICraftingGridHelper;
import astryxion.nei.api.gui.IDrawable;
import astryxion.nei.api.gui.IGuiItemStackGroup;
import astryxion.nei.api.gui.IRecipeLayout;
import astryxion.nei.api.recipe.IRecipeCategory;
import astryxion.nei.api.recipe.IRecipeWrapper;
import astryxion.nei.api.recipe.VanillaRecipeCategoryUid;
import astryxion.nei.api.recipe.wrapper.ICraftingRecipeWrapper;
import astryxion.nei.api.recipe.wrapper.IShapedCraftingRecipeWrapper;
import astryxion.nei.util.Log;
import astryxion.nei.util.Translator;

public class CraftingRecipeCategory implements IRecipeCategory {

	private static final int craftOutputSlot = 0;
	private static final int craftInputSlot1 = 1;

	@Nonnull
	private final IDrawable background;
	@Nonnull
	private final String localizedName;
	@Nonnull
	private final ICraftingGridHelper craftingGridHelper;

	public CraftingRecipeCategory(IGuiHelper guiHelper) {
		ResourceLocation location = new ResourceLocation("minecraft", "textures/gui/container/crafting_table.png");
		background = guiHelper.createDrawable(location, 29, 16, 116, 54);
		localizedName = Translator.translateToLocal("gui.nei.category.craftingTable");
		craftingGridHelper = guiHelper.createCraftingGridHelper(craftInputSlot1, craftOutputSlot);
	}

	@Override
	@Nonnull
	public String getUid() {
		return VanillaRecipeCategoryUid.CRAFTING;
	}

	@Nonnull
	@Override
	public String getTitle() {
		return localizedName;
	}

	@Override
	@Nonnull
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public void drawExtras(Minecraft minecraft) {

	}

	@Override
	public void drawAnimations(Minecraft minecraft) {

	}

	@Override
	public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull IRecipeWrapper recipeWrapper) {
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();

		guiItemStacks.init(craftOutputSlot, false, 94, 18);

		for (int y = 0; y < 3; ++y) {
			for (int x = 0; x < 3; ++x) {
				int index = craftInputSlot1 + x + (y * 3);
				guiItemStacks.init(index, true, x * 18, y * 18);
			}
		}

		if (recipeWrapper instanceof IShapedCraftingRecipeWrapper) {
			IShapedCraftingRecipeWrapper wrapper = (IShapedCraftingRecipeWrapper) recipeWrapper;
			craftingGridHelper.setInput(guiItemStacks, wrapper.getInputs(), wrapper.getWidth(), wrapper.getHeight());
			craftingGridHelper.setOutput(guiItemStacks, wrapper.getOutputs());
		} else if (recipeWrapper instanceof ICraftingRecipeWrapper) {
			ICraftingRecipeWrapper wrapper = (ICraftingRecipeWrapper) recipeWrapper;
			craftingGridHelper.setInput(guiItemStacks, wrapper.getInputs());
			craftingGridHelper.setOutput(guiItemStacks, wrapper.getOutputs());
		} else {
			Log.error("RecipeWrapper is not a known crafting wrapper type: {}", recipeWrapper);
		}
	}

}
