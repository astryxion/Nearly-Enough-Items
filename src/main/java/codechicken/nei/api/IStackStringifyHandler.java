package codechicken.nei.api;

import net.minecraft.item.ItemStack;

public interface IStackStringifyHandler {
	String stringify(ItemStack stack);

	ItemStack unstringify(String encoded);
}
