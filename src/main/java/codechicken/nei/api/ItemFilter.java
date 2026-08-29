package codechicken.nei.api;

import net.minecraft.item.ItemStack;

public interface ItemFilter {

	public static interface ItemFilterProvider {
		ItemFilter getFilter();
	}

	boolean matches(ItemStack item);
}
