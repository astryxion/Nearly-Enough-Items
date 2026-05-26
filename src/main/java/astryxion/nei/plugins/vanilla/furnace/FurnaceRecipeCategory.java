package astryxion.nei.plugins.vanilla.furnace;

import javax.annotation.Nonnull;

import net.minecraft.util.ResourceLocation;

import astryxion.nei.api.IGuiHelper;
import astryxion.nei.api.gui.IDrawableAnimated;
import astryxion.nei.api.gui.IDrawableStatic;
import astryxion.nei.api.recipe.IRecipeCategory;

public abstract class FurnaceRecipeCategory implements IRecipeCategory {
	protected static final int inputSlot = 0;
	protected static final int fuelSlot = 1;
	protected static final int outputSlot = 2;

	protected final ResourceLocation backgroundLocation;
	@Nonnull
	protected final IDrawableAnimated flame;
	@Nonnull
	protected final IDrawableAnimated arrow;

	public FurnaceRecipeCategory(IGuiHelper guiHelper) {
		backgroundLocation = new ResourceLocation("minecraft", "textures/gui/container/furnace.png");

		IDrawableStatic flameDrawable = guiHelper.createDrawable(backgroundLocation, 176, 0, 14, 14);
		flame = guiHelper.createAnimatedDrawable(flameDrawable, 300, IDrawableAnimated.StartDirection.TOP, true);

		IDrawableStatic arrowDrawable = guiHelper.createDrawable(backgroundLocation, 176, 14, 24, 17);
		this.arrow = guiHelper.createAnimatedDrawable(arrowDrawable, 200, IDrawableAnimated.StartDirection.LEFT, false);
	}
}
