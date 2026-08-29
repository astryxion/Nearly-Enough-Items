package codechicken.lib.inventory;

import net.minecraft.item.ItemStack;

/**
 * Minimal CodeChickenLib inventory helpers used by DefaultOverlayHandler.
 */
public class InventoryUtils {
	public static ItemStack copyStack(ItemStack stack, int quantity) {
		if (stack == null) {
			return null;
		}
		ItemStack copy = stack.copy();
		copy.stackSize = quantity;
		return copy;
	}

	public static boolean canStack(ItemStack stack1, ItemStack stack2) {
		return stack1 == null || stack2 == null
				|| (stack1.getItem() == stack2.getItem()
						&& (!stack2.getHasSubtypes() || stack2.getMetadata() == stack1.getMetadata())
						&& ItemStack.areItemStackTagsEqual(stack2, stack1)
						&& stack1.isStackable());
	}
}
