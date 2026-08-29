package codechicken.nei.bridge;

import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import astryxion.nei.Internal;
import astryxion.nei.api.gui.IDrawable;
import astryxion.nei.api.gui.IGuiItemStackGroup;
import astryxion.nei.api.gui.IRecipeLayout;
import astryxion.nei.api.recipe.IRecipeCategory;
import astryxion.nei.api.recipe.IRecipeWrapper;
import astryxion.nei.gui.ingredients.GuiItemStackGroup;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.IRecipeHandler;
import codechicken.nei.recipe.TemplateRecipeHandler;

public class NeiRecipeCategory implements IRecipeCategory {
	private final String uid;
	private final String title;
	private final IDrawable background;
	private final IRecipeHandler prototype;

	public NeiRecipeCategory(IRecipeHandler prototype) {
		this.prototype = prototype;
		this.uid = uidFor(prototype);
		String name;
		try {
			name = prototype.getRecipeName();
		} catch (Throwable t) {
			name = prototype.getClass().getSimpleName();
		}
		this.title = name == null ? prototype.getClass().getSimpleName() : name;
		this.background = Internal.getHelpers().getGuiHelper().createBlankDrawable(166, 65);
	}

	public static String uidFor(IRecipeHandler handler) {
		return "nei." + handler.getClass().getName();
	}

	public IRecipeHandler getPrototype() {
		return prototype;
	}

	@Nonnull
	@Override
	public String getUid() {
		return uid;
	}

	@Nonnull
	@Override
	public String getTitle() {
		return title;
	}

	@Nonnull
	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public void drawExtras(Minecraft minecraft) {
		try {
			prototype.drawBackground(0);
		} catch (Throwable ignored) {
		}
	}

	@Override
	public void drawAnimations(Minecraft minecraft) {
	}

	@Override
	public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull IRecipeWrapper recipeWrapper) {
		if (!(recipeWrapper instanceof NeiAdaptedRecipe)) {
			return;
		}
		NeiAdaptedRecipe adapted = (NeiAdaptedRecipe) recipeWrapper;
		IGuiItemStackGroup stacks = recipeLayout.getItemStacks();
		int slot = 0;
		slot = initStacks(stacks, adapted.getHandler().getIngredientStacks(adapted.getRecipeIndex()), true, slot);
		PositionedStack result = adapted.getHandler().getResultStack(adapted.getRecipeIndex());
		if (result != null) {
			slot = initStacks(stacks, java.util.Collections.singletonList(result), false, slot);
		}
		initStacks(stacks, adapted.getHandler().getOtherStacks(adapted.getRecipeIndex()), true, slot);
	}

	private static int initStacks(IGuiItemStackGroup stacks, List<PositionedStack> positioned, boolean input, int startSlot) {
		if (positioned == null) {
			return startSlot;
		}
		int slot = startSlot;
		for (int i = 0; i < positioned.size(); i++) {
			PositionedStack stack = positioned.get(i);
			if (stack == null) {
				continue;
			}
			if (stacks instanceof GuiItemStackGroup) {
				((GuiItemStackGroup) stacks).init(slot, input, stack.relx, stack.rely, 0);
			} else {
				stacks.init(slot, input, stack.relx, stack.rely);
			}
			List<net.minecraft.item.ItemStack> items = stack.getItemList();
			if (!items.isEmpty()) {
				stacks.set(slot, items);
			}
			slot++;
		}
		return slot;
	}

	public static ResourceLocation textureOf(IRecipeHandler handler) {
		if (handler instanceof TemplateRecipeHandler) {
			try {
				String path = ((TemplateRecipeHandler) handler).getGuiTexture();
				if (path != null) {
					return new ResourceLocation(path);
				}
			} catch (Throwable ignored) {
			}
		}
		return new ResourceLocation("minecraft", "textures/gui/container/crafting_table.png");
	}
}
