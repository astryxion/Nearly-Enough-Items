package astryxion.nei.plugins.nei.description;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;

import astryxion.nei.api.IGuiHelper;
import astryxion.nei.api.gui.IDrawable;
import astryxion.nei.api.gui.IGuiItemStackGroup;
import astryxion.nei.api.gui.IRecipeLayout;
import astryxion.nei.api.recipe.IRecipeCategory;
import astryxion.nei.api.recipe.IRecipeWrapper;
import astryxion.nei.api.recipe.VanillaRecipeCategoryUid;
import astryxion.nei.util.Translator;

public class ItemDescriptionRecipeCategory implements IRecipeCategory {
	public static final int recipeWidth = 160;
	public static final int recipeHeight = 125;
	@Nonnull
	private final IDrawable background;
	@Nonnull
	private final String localizedName;

	public ItemDescriptionRecipeCategory(IGuiHelper guiHelper) {
		background = guiHelper.createBlankDrawable(recipeWidth, recipeHeight);
		localizedName = Translator.translateToLocal("gui.nei.category.itemDescription");
	}

	@Nonnull
	@Override
	public String getUid() {
		return VanillaRecipeCategoryUid.DESCRIPTION;
	}

	@Nonnull
	@Override
	public String getTitle() {
		return localizedName;
	}

	@Nonnull
	@Override
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

		int xPos = (recipeWidth - 18) / 2;
		guiItemStacks.init(0, false, xPos, 0);
		guiItemStacks.setFromRecipe(0, recipeWrapper.getOutputs());
	}
}
