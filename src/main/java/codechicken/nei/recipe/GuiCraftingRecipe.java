package codechicken.nei.recipe;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.item.ItemStack;

import codechicken.nei.bridge.NeiCompatLog;
import codechicken.nei.bridge.NeiRecipeBridge;

public class GuiCraftingRecipe extends GuiRecipe {
	public static ArrayList<ICraftingHandler> craftinghandlers = new ArrayList<ICraftingHandler>();
	private static final Set<String> existingHandlers = new HashSet<String>();

	private GuiCraftingRecipe() {
		super(null);
	}

	public static boolean openRecipeGui(String outputId, Object... results) {
		return NeiRecipeBridge.openCraftingRecipes(outputId, results);
	}

	public static void registerRecipeHandler(ICraftingHandler handler) {
		if (handler == null) {
			return;
		}
		String handlerId = handler.getClass().getName();
		synchronized (existingHandlers) {
			if (existingHandlers.contains(handlerId)) {
				return;
			}
			existingHandlers.add(handlerId);
		}
		synchronized (craftinghandlers) {
			craftinghandlers.add(handler);
		}
		NeiCompatLog.api("registerRecipeHandler", handlerId);
		NeiRecipeBridge.registerCraftingHandler(handler);
	}

	public ArrayList<ICraftingHandler> getCurrentRecipeHandlers() {
		return craftinghandlers;
	}

	public static boolean hasHandlerFor(ItemStack stack) {
		return stack != null;
	}
}
