package codechicken.nei.recipe;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;

import codechicken.nei.api.IStackStringifyHandler;

public class StackInfo {
	public static final List<IStackStringifyHandler> stackStringifyHandlers = new ArrayList<IStackStringifyHandler>();

	public static String getItemStackGUID(ItemStack stack) {
		return stack == null ? "" : String.valueOf(stack);
	}

	public static boolean equalItemAndNBT(ItemStack a, ItemStack b, boolean ignoreSize) {
		if (a == b) {
			return true;
		}
		if (a == null || b == null) {
			return false;
		}
		return a.getItem() == b.getItem()
				&& a.getMetadata() == b.getMetadata()
				&& ItemStack.areItemStackTagsEqual(a, b);
	}
}
