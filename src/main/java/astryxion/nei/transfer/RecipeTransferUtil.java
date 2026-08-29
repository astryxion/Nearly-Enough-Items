package astryxion.nei.transfer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

import astryxion.nei.Internal;
import astryxion.nei.api.recipe.VanillaRecipeCategoryUid;
import astryxion.nei.api.recipe.transfer.IRecipeTransferError;
import astryxion.nei.api.recipe.transfer.IRecipeTransferHandler;
import astryxion.nei.gui.RecipeLayout;
import astryxion.nei.util.Log;
import codechicken.nei.api.IOverlayHandler;
import codechicken.nei.bridge.NeiAdaptedRecipe;
import codechicken.nei.bridge.NeiCompatLog;
import codechicken.nei.bridge.OverlayRecipeAdapter;
import codechicken.nei.recipe.IRecipeHandler;
import codechicken.nei.recipe.RecipeInfo;

public class RecipeTransferUtil {
	public static IRecipeTransferError getTransferRecipeError(@Nonnull Container container, @Nonnull RecipeLayout recipeLayout, @Nonnull EntityPlayer player) {
		return transferRecipe(container, recipeLayout, player, false, false);
	}

	public static boolean transferRecipe(@Nonnull Container container, @Nonnull RecipeLayout recipeLayout, @Nonnull EntityPlayer player, boolean maxTransfer) {
		IRecipeTransferError error = transferRecipe(container, recipeLayout, player, maxTransfer, true);
		return error == null;
	}

	@Nullable
	private static IRecipeTransferError transferRecipe(@Nonnull Container container, @Nonnull RecipeLayout recipeLayout, @Nonnull EntityPlayer player, boolean maxTransfer, boolean doTransfer) {
		IRecipeTransferHandler transferHandler = Internal.getRecipeRegistry().getRecipeTransferHandler(container, recipeLayout.getRecipeCategory());
		if (transferHandler != null) {
			return transferHandler.transferRecipe(container, recipeLayout, player, maxTransfer, doTransfer);
		}
		if (tryNeiOverlay(recipeLayout, maxTransfer, doTransfer)) {
			return null;
		}
		if (doTransfer) {
			Log.error("No Recipe Transfer handler for container {}", container.getClass());
		}
		return RecipeTransferErrorInternal.instance;
	}

	private static boolean tryNeiOverlay(RecipeLayout recipeLayout, boolean maxTransfer, boolean doTransfer) {
		Minecraft mc = Minecraft.getMinecraft();
		if (mc == null || !(mc.currentScreen instanceof GuiContainer)) {
			return false;
		}
		GuiContainer gui = (GuiContainer) mc.currentScreen;
		IOverlayHandler overlay = null;
		IRecipeHandler handler = null;
		int recipeIndex = 0;

		if (recipeLayout.getRecipeWrapper() instanceof NeiAdaptedRecipe) {
			NeiAdaptedRecipe adapted = (NeiAdaptedRecipe) recipeLayout.getRecipeWrapper();
			handler = adapted.getHandler();
			recipeIndex = adapted.getRecipeIndex();
			try {
				overlay = handler.getOverlayHandler(gui, recipeIndex);
			} catch (Throwable t) {
				NeiCompatLog.warn("overlay handler lookup failed for {}", handler.getClass().getName());
			}
		}

		if (overlay == null && VanillaRecipeCategoryUid.CRAFTING.equals(recipeLayout.getRecipeCategory().getUid())) {
			overlay = RecipeInfo.getOverlayHandler(gui, "crafting");
			if (overlay != null) {
				handler = OverlayRecipeAdapter.fromCraftingLayout(recipeLayout);
				recipeIndex = 0;
			}
		}

		if (overlay == null || handler == null) {
			return false;
		}
		if (doTransfer) {
			try {
				overlay.overlayRecipe(gui, handler, recipeIndex, maxTransfer);
				NeiCompatLog.api("overlayRecipe", overlay.getClass().getName(), gui.getClass().getName());
			} catch (Throwable t) {
				NeiCompatLog.error("overlayRecipe failed: {}", t.toString());
				return false;
			}
		}
		return true;
	}
}
