package codechicken.nei;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;

import net.minecraftforge.oredict.OreDictionary;

public class NEIServerUtils {
	public static boolean areStacksSameType(ItemStack stack1, ItemStack stack2) {
		return stack1 != null && stack2 != null
				&& stack1.getItem() == stack2.getItem()
				&& stack1.getMetadata() == stack2.getMetadata();
	}

	public static boolean areStacksSameTypeCrafting(ItemStack stack1, ItemStack stack2) {
		return stack1 != null && stack2 != null
				&& stack1.getItem() == stack2.getItem()
				&& (stack1.getMetadata() == stack2.getMetadata()
						|| stack1.getMetadata() == OreDictionary.WILDCARD_VALUE
						|| stack2.getMetadata() == OreDictionary.WILDCARD_VALUE
						|| stack1.getItem().isDamageable());
	}

	public static boolean areStacksSameTypeCraftingWithNBT(ItemStack stack1, ItemStack stack2) {
		return areStacksSameTypeCrafting(stack1, stack2) && ItemStack.areItemStackTagsEqual(stack1, stack2);
	}

	@SuppressWarnings("unchecked")
	public static ItemStack[] extractRecipeItems(Object obj) {
		if (obj == null) {
			return new ItemStack[0];
		}
		if (obj instanceof ItemStack) {
			return new ItemStack[] { (ItemStack) obj };
		}
		if (obj instanceof ItemStack[]) {
			return (ItemStack[]) obj;
		}
		if (obj instanceof List) {
			List<?> list = (List<?>) obj;
			if (list.isEmpty()) {
				return new ItemStack[0];
			}
			if (list.get(0) instanceof ItemStack || list.get(0) == null) {
				return list.toArray(new ItemStack[list.size()]);
			}
			List<ItemStack> flattened = new ArrayList<ItemStack>();
			for (int i = 0; i < list.size(); i++) {
				ItemStack[] extracted = extractRecipeItems(list.get(i));
				for (int j = 0; j < extracted.length; j++) {
					flattened.add(extracted[j]);
				}
			}
			return flattened.toArray(new ItemStack[flattened.size()]);
		}
		if (obj instanceof String) {
			List<ItemStack> ores = OreDictionary.getOres((String) obj);
			return ores.toArray(new ItemStack[ores.size()]);
		}
		List<ItemStack> expanded = astryxion.nei.discovery.IngredientExpander.expand(obj);
		if (!expanded.isEmpty()) {
			return expanded.toArray(new ItemStack[expanded.size()]);
		}
		return new ItemStack[0];
	}

	public static int getHarvestLevel(ItemStack stack) {
		return -1;
	}

	public static ArrayList<int[]> getEnchantments(ItemStack itemstack) {
		return new ArrayList<int[]>();
	}

	public static boolean isRaining(net.minecraft.world.World world) {
		return world != null && world.isRaining();
	}

	public static int getDimension(net.minecraft.world.World world) {
		return world == null ? 0 : world.provider.dimensionId;
	}
}
