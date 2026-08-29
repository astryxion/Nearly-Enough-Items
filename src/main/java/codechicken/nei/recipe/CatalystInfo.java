package codechicken.nei.recipe;

import net.minecraft.item.ItemStack;

public class CatalystInfo {
	public final ItemStack stack;
	public final int priority;

	public CatalystInfo(ItemStack stack, int priority) {
		this.stack = stack;
		this.priority = priority;
	}

	public ItemStack getStack() {
		return stack;
	}

	public int getPriority() {
		return priority;
	}
}
