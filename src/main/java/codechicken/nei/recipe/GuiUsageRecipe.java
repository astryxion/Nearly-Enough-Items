package codechicken.nei.recipe;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import codechicken.nei.bridge.NeiCompatLog;
import codechicken.nei.bridge.NeiRecipeBridge;

public class GuiUsageRecipe extends GuiRecipe {
	public static ArrayList<IUsageHandler> usagehandlers = new ArrayList<IUsageHandler>();
	private static final Set<String> existingHandlers = new HashSet<String>();

	private GuiUsageRecipe() {
		super(null);
	}

	public static boolean openRecipeGui(String inputId, Object... ingredients) {
		return NeiRecipeBridge.openUsageRecipes(inputId, ingredients);
	}

	public static void registerUsageHandler(IUsageHandler handler) {
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
		synchronized (usagehandlers) {
			usagehandlers.add(handler);
		}
		NeiCompatLog.api("registerUsageHandler", handlerId);
		NeiRecipeBridge.registerUsageHandler(handler);
	}
}
