package codechicken.nei.recipe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.item.ItemStack;

public class RecipeCatalysts {
	private static final Map<String, List<CatalystInfo>> catalysts = new HashMap<String, List<CatalystInfo>>();

	public static String getRecipeID(IRecipeHandler handler) {
		return handler == null ? "" : handler.getClass().getName();
	}

	public static void addRecipeCatalyst(String handlerID, CatalystInfo info) {
		if (handlerID == null || info == null) {
			return;
		}
		List<CatalystInfo> list = catalysts.get(handlerID);
		if (list == null) {
			list = new ArrayList<CatalystInfo>();
			catalysts.put(handlerID, list);
		}
		list.add(info);
	}

	public static void removeRecipeCatalyst(String handlerID, ItemStack stack) {
		List<CatalystInfo> list = catalysts.get(handlerID);
		if (list == null) {
			return;
		}
		for (int i = list.size() - 1; i >= 0; i--) {
			if (ItemStack.areItemStacksEqual(list.get(i).stack, stack)) {
				list.remove(i);
			}
		}
	}

	public static List<CatalystInfo> getRecipeCatalysts(String handlerID) {
		List<CatalystInfo> list = catalysts.get(handlerID);
		return list == null ? new ArrayList<CatalystInfo>() : list;
	}
}
